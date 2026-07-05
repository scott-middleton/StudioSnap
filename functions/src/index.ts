import {onCall, HttpsError} from "firebase-functions/v2/https";
import {user as authUser} from "firebase-functions/v1/auth";
import * as admin from "firebase-admin";

admin.initializeApp();

const REPLICATE_BASE_URL = "https://api.replicate.com/v1";
const REVENUECAT_BASE_URL = "https://api.revenuecat.com/v2";
const REVENUECAT_PROJECT_ID = "proj30aee59a";

const CURRENCY_CODE = "credits";

const MAX_RETRIES = 3;
const RETRY_BASE_DELAY_MS = 1000;

const RATE_LIMIT_MAX_CALLS = 100;
const RATE_LIMIT_WINDOW_MS = 60 * 60 * 1000; // 1 hour

/**
 * Extracts and validates the Firebase UID from the request.
 * Throws HttpsError if the caller is not authenticated.
 */
function requireAuth(request: {auth?: {uid: string}}): string {
  if (!request.auth?.uid) {
    throw new HttpsError("unauthenticated", "Authentication required");
  }
  return request.auth.uid;
}

/**
 * Per-user rate limiting using Firestore. Tracks call timestamps in a
 * rolling window and rejects if the limit is exceeded.
 */
async function enforceRateLimit(uid: string, functionName: string): Promise<void> {
  const db = admin.firestore();
  const ref = db.collection("rateLimits").doc(`${uid}_${functionName}`);
  const now = Date.now();
  const windowStart = now - RATE_LIMIT_WINDOW_MS;

  await db.runTransaction(async (tx) => {
    const doc = await tx.get(ref);
    const timestamps: number[] = doc.exists ? (doc.data()?.timestamps ?? []) : [];
    const recent = timestamps.filter((t: number) => t > windowStart);

    if (recent.length >= RATE_LIMIT_MAX_CALLS) {
      throw new HttpsError(
        "resource-exhausted",
        `Rate limit exceeded: max ${RATE_LIMIT_MAX_CALLS} calls per hour`
      );
    }

    recent.push(now);
    tx.set(ref, {timestamps: recent});
  });
}

const PENDING_DEDUCTION_TTL_MS = 60 * 60 * 1000; // 1 hour

interface PendingEntry { ts: number; status: "pending" | "confirmed"; predictionId?: string }

function isValidEntry(val: unknown): val is PendingEntry {
  if (typeof val !== "object" || val === null) return false;
  const obj = val as Record<string, unknown>;
  if (typeof obj.ts !== "number") return false;
  if (obj.status !== "pending" && obj.status !== "confirmed") return false;
  if (obj.predictionId !== undefined && typeof obj.predictionId !== "string") return false;
  return true;
}

/**
 * Records a pending credit deduction in Firestore with status "pending".
 * The entry must be confirmed via confirmPendingDeduction after the
 * RevenueCat deduction succeeds — only confirmed entries are refundable.
 */
async function setPendingDeduction(uid: string, idempotencyKey: string): Promise<void> {
  const db = admin.firestore();
  const entry: PendingEntry = {ts: Date.now(), status: "pending"};
  await db.collection("pendingDeductions").doc(uid).set(
    {[idempotencyKey]: entry},
    {merge: true}
  );
}

/**
 * Marks a pending deduction as confirmed. Called after the RevenueCat
 * deduction succeeds so the refund path knows real money was taken.
 */
async function confirmPendingDeduction(uid: string, idempotencyKey: string): Promise<void> {
  const db = admin.firestore();
  await db.collection("pendingDeductions").doc(uid).set(
    {[idempotencyKey]: {ts: Date.now(), status: "confirmed"}},
    {merge: true}
  );
}

/**
 * Removes a specific pending deduction by key. Used to clean up
 * after a failed deduction (where RevenueCat was never charged).
 */
async function removePendingDeduction(uid: string, idempotencyKey: string): Promise<void> {
  const db = admin.firestore();
  await db.collection("pendingDeductions").doc(uid).update({
    [idempotencyKey]: admin.firestore.FieldValue.delete(),
  });
}

/**
 * Atomically consumes a specific pending deduction by its idempotency key.
 * Only a confirmed, non-expired entry is consumable — returns null and
 * leaves Firestore untouched if the key is missing, still pending, or
 * expired. Also sweeps any expired entries found along the way so stale
 * data doesn't accumulate.
 */
async function consumePendingDeduction(uid: string, idempotencyKey: string): Promise<PendingEntry | null> {
  const db = admin.firestore();
  const ref = db.collection("pendingDeductions").doc(uid);
  const now = Date.now();

  return db.runTransaction(async (tx) => {
    const doc = await tx.get(ref);
    if (!doc.exists) return null;

    const data = doc.data() ?? {};
    const expiredKeys: string[] = [];
    for (const [key, val] of Object.entries(data)) {
      if (key === idempotencyKey) continue;
      if (isValidEntry(val) && now - val.ts > PENDING_DEDUCTION_TTL_MS) {
        expiredKeys.push(key);
      }
    }

    const target = data[idempotencyKey];
    const isConsumable =
      isValidEntry(target) &&
      target.status === "confirmed" &&
      now - target.ts <= PENDING_DEDUCTION_TTL_MS;

    if (expiredKeys.length > 0 || isConsumable) {
      const updates: Record<string, ReturnType<typeof admin.firestore.FieldValue.delete>> = {};
      for (const key of expiredKeys) {
        updates[key] = admin.firestore.FieldValue.delete();
      }
      if (isConsumable) {
        updates[idempotencyKey] = admin.firestore.FieldValue.delete();
      }
      tx.update(ref, updates);
    }

    return isConsumable ? (target as PendingEntry) : null;
  });
}

/**
 * Best-effort link from a pending deduction to the Replicate prediction
 * it paid for. Uses FieldPath (not a dotted string) because idempotency
 * keys contain hyphens, which would otherwise be misparsed as nested paths.
 * A failed attach degrades gracefully: refund falls back to the lenient
 * "no prediction created" rule.
 */
async function attachPredictionToDeduction(
  uid: string,
  deductionKey: string,
  predictionId: string
): Promise<void> {
  try {
    const db = admin.firestore();
    await db.collection("pendingDeductions").doc(uid).update(
      new admin.firestore.FieldPath(deductionKey, "predictionId"),
      predictionId
    );
  } catch (error) {
    console.error(`attachPredictionToDeduction: failed to attach — ${error}`);
  }
}

/**
 * Atomically validates that [deductionKey] references a confirmed,
 * non-expired, not-already-claimed deduction for [uid], then immediately
 * marks it "claimed" (predictionId: CLAIMED_PLACEHOLDER) in the same
 * transaction — before any Replicate call is made. This closes two variants
 * of the deduct -> generate -> refund exploit:
 *  1. Omitting deductionKey entirely, so refund falls through to the lenient
 *     "no prediction created" rule despite an image having been generated.
 *  2. Reusing one deductionKey across multiple createVersionPrediction
 *     calls to get N images for 1 credit (attach would otherwise silently
 *     overwrite the same field with the latest prediction id).
 * Throws HttpsError("failed-precondition") if the key is missing, still
 * pending, expired, or already claimed by an earlier prediction.
 */
const CLAIMED_PLACEHOLDER = "pending";

async function claimDeductionForPrediction(uid: string, deductionKey: string): Promise<void> {
  const db = admin.firestore();
  const ref = db.collection("pendingDeductions").doc(uid);
  const now = Date.now();

  await db.runTransaction(async (tx) => {
    const doc = await tx.get(ref);
    const data = doc.exists ? doc.data() ?? {} : {};
    const entry = data[deductionKey];

    const isClaimable =
      isValidEntry(entry) &&
      entry.status === "confirmed" &&
      now - entry.ts <= PENDING_DEDUCTION_TTL_MS &&
      entry.predictionId === undefined;

    if (!isClaimable) {
      throw new HttpsError(
        "failed-precondition",
        "deductionKey is missing, expired, or already used for a prediction"
      );
    }

    tx.update(ref, new admin.firestore.FieldPath(deductionKey, "predictionId"), CLAIMED_PLACEHOLDER);
  });
}

const GENERATION_CREDIT_COST = 1;

/**
 * Retries a fetch call with exponential backoff on 429 (rate limited).
 * Returns the Response on success, throws HttpsError on final failure.
 */
async function fetchWithRetry(
  url: string,
  options: RequestInit,
  operationName: string
): Promise<Response> {
  for (let attempt = 0; attempt < MAX_RETRIES; attempt++) {
    const response = await fetch(url, options);

    if (response.status === 429 && attempt < MAX_RETRIES - 1) {
      const retryAfter = response.headers.get("retry-after");
      const delayMs = retryAfter
        ? (parseInt(retryAfter, 10) + 2) * 1000
        : RETRY_BASE_DELAY_MS * Math.pow(2, attempt);
      console.warn(
        `${operationName}: rate limited (429), retrying in ${delayMs}ms (attempt ${attempt + 1}/${MAX_RETRIES})`
      );
      await new Promise((resolve) => setTimeout(resolve, delayMs));
      continue;
    }

    if (!response.ok) {
      const errorBody = await response.text();
      console.error(
        `${operationName} failed: HTTP ${response.status} — ${errorBody}`
      );

      if (response.status === 429) {
        throw new HttpsError(
          "resource-exhausted",
          `Rate limited: ${operationName}`
        );
      }

      throw new HttpsError(
        "internal",
        `${operationName} error ${response.status}`
      );
    }

    return response;
  }

  throw new HttpsError(
    "internal",
    `${operationName} failed after ${MAX_RETRIES} retries`
  );
}

// ─── Replicate: Create Model Prediction (Nano Banana) ─────────────────────

export const createModelPrediction = onCall(async (request) => {
  const uid = requireAuth(request);
  await enforceRateLimit(uid, "createModelPrediction");

  const apiToken = process.env.REPLICATE_API_TOKEN;
  if (!apiToken) {
    throw new HttpsError("internal", "Replicate API token not configured");
  }

  const {owner, name, input} = request.data;
  if (!owner || !name || !input) {
    throw new HttpsError("invalid-argument", "Missing owner, name, or input");
  }

  const response = await fetchWithRetry(
    `${REPLICATE_BASE_URL}/models/${owner}/${name}/predictions`,
    {
      method: "POST",
      headers: {
        Authorization: `Token ${apiToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({input}),
    },
    "createModelPrediction"
  );

  return await response.json();
});

// ─── Replicate: Create Version Prediction (Real-ESRGAN) ──────────────────

export const createVersionPrediction = onCall(async (request) => {
  const uid = requireAuth(request);
  await enforceRateLimit(uid, "createVersionPrediction");

  const apiToken = process.env.REPLICATE_API_TOKEN;
  if (!apiToken) {
    throw new HttpsError("internal", "Replicate API token not configured");
  }

  const {version, input, deductionKey} = request.data;
  if (!version || !input) {
    throw new HttpsError("invalid-argument", "Missing version or input");
  }
  if (!deductionKey || typeof deductionKey !== "string") {
    throw new HttpsError("invalid-argument", "Missing deductionKey");
  }

  // Validate + claim before spending the Replicate call: every legitimate
  // generation already has a real deduction (welcome credits are real
  // credits too), so there is no honest caller this rejects.
  await claimDeductionForPrediction(uid, deductionKey);

  const response = await fetchWithRetry(
    `${REPLICATE_BASE_URL}/predictions`,
    {
      method: "POST",
      headers: {
        Authorization: `Token ${apiToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({version, input}),
    },
    "createVersionPrediction"
  );

  const prediction = (await response.json()) as {id?: string};
  if (prediction?.id) {
    await attachPredictionToDeduction(uid, deductionKey, prediction.id);
  }
  return prediction;
});

// ─── Replicate: Get Prediction Status ─────────────────────────────────────

export const getPrediction = onCall(async (request) => {
  requireAuth(request);

  const apiToken = process.env.REPLICATE_API_TOKEN;
  if (!apiToken) {
    throw new HttpsError("internal", "Replicate API token not configured");
  }

  const {predictionId} = request.data;
  if (!predictionId) {
    throw new HttpsError("invalid-argument", "Missing predictionId");
  }

  const response = await fetchWithRetry(
    `${REPLICATE_BASE_URL}/predictions/${predictionId}`,
    {
      headers: {
        Authorization: `Token ${apiToken}`,
      },
    },
    "getPrediction"
  );

  return await response.json();
});

// ─── RevenueCat: Fetch User Credits ───────────────────────────────────────

export const fetchUserCredits = onCall(async (request) => {
  const uid = requireAuth(request);

  const secretKey = process.env.REVENUECAT_SECRET_KEY;
  if (!secretKey) {
    throw new HttpsError("internal", "RevenueCat secret key not configured");
  }

  const url = `${REVENUECAT_BASE_URL}/projects/${REVENUECAT_PROJECT_ID}/customers/${uid}/virtual_currencies`;
  console.log(`fetchUserCredits: querying uid=${uid}, url=${url}`);
  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${secretKey}`,
    },
  });

  if (response.status === 404) {
    console.log(`fetchUserCredits: 404 for uid=${uid}`);
    return {items: [], balance: 0};
  }

  if (!response.ok) {
    const errorBody = await response.text();
    console.error(
      `fetchUserCredits failed: HTTP ${response.status} — ${errorBody}`
    );
    throw new HttpsError(
      "internal",
      `RevenueCat API error ${response.status}`
    );
  }

  const body = (await response.json()) as {
    items: Array<{currency_code: string; balance: number}>;
  };
  console.log(`fetchUserCredits: response body=${JSON.stringify(body)}`);
  const credits = body.items?.find(
    (i: {currency_code: string}) => i.currency_code === CURRENCY_CODE
  );
  console.log(`fetchUserCredits: returning balance=${credits?.balance ?? 0}`);
  return {balance: credits?.balance ?? 0};
});

// ─── RevenueCat: Internal Credit Helpers (not callable from client) ───────

/**
 * Deducts exactly GENERATION_CREDIT_COST from the user's RevenueCat balance.
 * Throws HttpsError on failure. Returns the new balance.
 */
async function deductCreditsInternal(uid: string, idempotencyKey: string): Promise<number> {
  const secretKey = process.env.REVENUECAT_SECRET_KEY;
  if (!secretKey) {
    throw new HttpsError("internal", "RevenueCat secret key not configured");
  }

  const url = `${REVENUECAT_BASE_URL}/projects/${REVENUECAT_PROJECT_ID}/customers/${uid}/virtual_currencies/transactions`;
  const response = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${secretKey}`,
      "Content-Type": "application/json",
      "Idempotency-Key": idempotencyKey,
    },
    body: JSON.stringify({
      adjustments: {[CURRENCY_CODE]: -GENERATION_CREDIT_COST},
    }),
  });

  if (response.status === 422) {
    throw new HttpsError("failed-precondition", "Insufficient credits");
  }

  if (!response.ok) {
    const errorBody = await response.text();
    console.error(
      `deductCreditsInternal failed: HTTP ${response.status} — ${errorBody}`
    );
    throw new HttpsError(
      "internal",
      `RevenueCat API error ${response.status}`
    );
  }

  const body = (await response.json()) as {
    items: Array<{currency_code: string; balance: number}>;
  };
  const credits = body.items?.find(
    (i: {currency_code: string}) => i.currency_code === CURRENCY_CODE
  );
  return credits?.balance ?? 0;
}

/**
 * Refunds exactly GENERATION_CREDIT_COST to the user's RevenueCat balance.
 * Throws HttpsError on failure. Returns the new balance.
 */
async function addCreditsInternal(uid: string, idempotencyKey: string): Promise<number> {
  const secretKey = process.env.REVENUECAT_SECRET_KEY;
  if (!secretKey) {
    throw new HttpsError("internal", "RevenueCat secret key not configured");
  }

  const url = `${REVENUECAT_BASE_URL}/projects/${REVENUECAT_PROJECT_ID}/customers/${uid}/virtual_currencies/transactions`;
  const response = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${secretKey}`,
      "Content-Type": "application/json",
      "Idempotency-Key": idempotencyKey,
    },
    body: JSON.stringify({
      adjustments: {[CURRENCY_CODE]: GENERATION_CREDIT_COST},
    }),
  });

  if (!response.ok) {
    const errorBody = await response.text();
    console.error(
      `addCreditsInternal failed: HTTP ${response.status} — ${errorBody}`
    );
    throw new HttpsError(
      "internal",
      `RevenueCat API error ${response.status}`
    );
  }

  const body = (await response.json()) as {
    items: Array<{currency_code: string; balance: number}>;
  };
  const credits = body.items?.find(
    (i: {currency_code: string}) => i.currency_code === CURRENCY_CODE
  );
  return credits?.balance ?? 0;
}

// ─── RevenueCat: Deduct Generation Credit (callable) ─────────────────────

export const deductGenerationCredit = onCall(async (request) => {
  const uid = requireAuth(request);
  await enforceRateLimit(uid, "deductGenerationCredit");
  const {idempotencyKey} = request.data;
  if (!idempotencyKey || typeof idempotencyKey !== "string") {
    throw new HttpsError("invalid-argument", "Missing idempotencyKey");
  }
  // 1. Write a "pending" record — not yet refundable.
  // 2. Deduct from RevenueCat.
  // 3. Confirm the record — now refundable if the generation pipeline fails.
  // If step 2 fails, remove the pending record (no money was taken).
  try {
    await setPendingDeduction(uid, idempotencyKey);
    const balance = await deductCreditsInternal(uid, idempotencyKey);
    await confirmPendingDeduction(uid, idempotencyKey);
    return {balance};
  } catch (error) {
    try {
      await removePendingDeduction(uid, idempotencyKey);
    } catch (cleanupError) {
      console.error(`deductGenerationCredit: cleanup error — ${cleanupError}`);
    }
    if (error instanceof HttpsError) throw error;
    throw new HttpsError("internal", `Deduction failed: ${error}`);
  }
});

/**
 * Fetches a Replicate prediction's current state. Returns null if the
 * prediction can't be found (treated as "never created" by callers).
 */
async function fetchPredictionStatus(predictionId: string): Promise<string | null> {
  const apiToken = process.env.REPLICATE_API_TOKEN;
  if (!apiToken) {
    throw new HttpsError("internal", "Replicate API token not configured");
  }

  const response = await fetch(`${REPLICATE_BASE_URL}/predictions/${predictionId}`, {
    headers: {Authorization: `Token ${apiToken}`},
  });

  if (response.status === 404) return null;
  if (!response.ok) {
    const errorBody = await response.text();
    console.error(`fetchPredictionStatus failed: HTTP ${response.status} — ${errorBody}`);
    throw new HttpsError("internal", `Replicate API error ${response.status}`);
  }

  const body = (await response.json()) as {status: string};
  return body.status;
}

/** Best-effort cancel — a failed cancel does not block the refund. */
async function cancelPredictionBestEffort(predictionId: string): Promise<void> {
  try {
    const apiToken = process.env.REPLICATE_API_TOKEN;
    if (!apiToken) return;
    await fetch(`${REPLICATE_BASE_URL}/predictions/${predictionId}/cancel`, {
      method: "POST",
      headers: {Authorization: `Token ${apiToken}`},
    });
  } catch (error) {
    console.error(`cancelPredictionBestEffort: failed to cancel ${predictionId} — ${error}`);
  }
}

// ─── RevenueCat: Refund Generation Credit (callable) ─────────────────────

export const refundGenerationCredit = onCall(async (request) => {
  const uid = requireAuth(request);
  await enforceRateLimit(uid, "refundGenerationCredit");

  const {idempotencyKey} = request.data;
  if (!idempotencyKey || typeof idempotencyKey !== "string") {
    throw new HttpsError("invalid-argument", "Missing idempotencyKey");
  }

  const entry = await consumePendingDeduction(uid, idempotencyKey);
  if (!entry) {
    throw new HttpsError("failed-precondition", "No matching deduction to refund");
  }

  // Server-verify against Replicate before paying out: a prediction that
  // actually succeeded already delivered the image, so it is not refundable.
  // This closes the deduct -> generate -> refund exploit where a scripted
  // client calls refund unconditionally after a successful generation.
  // entry.predictionId === CLAIMED_PLACEHOLDER means createVersionPrediction
  // claimed the key but never got a real id back (Replicate call itself
  // failed) — there's no prediction to check, so skip straight to refund.
  if (entry.predictionId && entry.predictionId !== CLAIMED_PLACEHOLDER) {
    let status: string | null = null;
    try {
      status = await fetchPredictionStatus(entry.predictionId);
    } catch (error) {
      console.error(`refundGenerationCredit: prediction status check failed — ${error}`);
    }

    if (status === "succeeded") {
      await confirmPendingDeduction(uid, idempotencyKey);
      throw new HttpsError("failed-precondition", "Generation succeeded — not refundable");
    }
    if (status === "starting" || status === "processing") {
      // Client's poll timed out while Replicate was still working.
      // Best-effort cancel, then refund regardless — favors the honest
      // user over the rare case where the prediction finishes anyway.
      await cancelPredictionBestEffort(entry.predictionId);
    }
  }

  // Use a refund-specific key for RevenueCat idempotency so the refund
  // transaction is distinct from the original deduction transaction.
  try {
    const refundKey = `refund-${idempotencyKey}`;
    const balance = await addCreditsInternal(uid, refundKey);
    return {balance};
  } catch (error) {
    // Restore the pending deduction so the client can retry the refund.
    try {
      await confirmPendingDeduction(uid, idempotencyKey);
    } catch (restoreError) {
      console.error(`refundGenerationCredit: failed to restore pending deduction — ${restoreError}`);
    }
    if (error instanceof HttpsError) throw error;
    throw new HttpsError("internal", `Refund failed: ${error}`);
  }
});

// ─── Auth: Delete user data on account deletion ───────────────────────────

export const onUserDeleted = authUser().onDelete(async (user) => {
  const uid = user.uid;
  const db = admin.firestore();

  const rateLimitDocs = await db.collection("rateLimits")
    .where(admin.firestore.FieldPath.documentId(), ">=", `${uid}_`)
    .where(admin.firestore.FieldPath.documentId(), "<", `${uid}\``)
    .get();

  await Promise.all([
    db.collection("users").doc(uid).delete(),
    db.collection("pendingDeductions").doc(uid).delete(),
    ...rateLimitDocs.docs.map((doc) => doc.ref.delete()),
  ]);

  console.log(`onUserDeleted: cleaned up Firestore data for uid=${uid}`);
});

// ─── Welcome Credits: Atomic first-sign-in grant (server-side gate) ───────

export const claimWelcomeCredits = onCall(async (request) => {
  const uid = requireAuth(request);
  await enforceRateLimit(uid, "claimWelcomeCredits");
  const db = admin.firestore();
  const userRef = db.collection("users").doc(uid);

  // Atomically check and set welcomeCreditsGranted so concurrent calls
  // (e.g. splash + sign-in racing) grant at most once per user.
  const firstClaim = await db.runTransaction(async (tx) => {
    const doc = await tx.get(userRef);
    if (doc.exists && doc.data()?.welcomeCreditsGranted === true) {
      return false;
    }
    tx.set(userRef, {welcomeCreditsGranted: true}, {merge: true});
    return true;
  });

  if (!firstClaim) {
    return {granted: false};
  }

  try {
    const balance = await addCreditsInternal(uid, `welcome-${uid}`);
    return {granted: true, balance};
  } catch (error) {
    // Roll back the flag so the client can retry the grant later.
    try {
      await userRef.set({welcomeCreditsGranted: false}, {merge: true});
    } catch (rollbackError) {
      console.error(`claimWelcomeCredits: failed to roll back flag — ${rollbackError}`);
    }
    if (error instanceof HttpsError) throw error;
    throw new HttpsError("internal", `Welcome credit grant failed: ${error}`);
  }
});
