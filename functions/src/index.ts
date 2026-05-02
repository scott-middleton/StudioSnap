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

interface PendingEntry { ts: number; status: "pending" | "confirmed" }

function isValidEntry(val: unknown): val is PendingEntry {
  if (typeof val !== "object" || val === null) return false;
  const obj = val as Record<string, unknown>;
  return typeof obj.ts === "number" && (obj.status === "pending" || obj.status === "confirmed");
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
 * Finds and consumes the most recent confirmed (refundable) pending
 * deduction for a user. Returns the idempotencyKey of the consumed
 * deduction (needed for RevenueCat idempotent refund), or null if
 * none found. Also cleans up any expired entries.
 */
async function consumeLatestPendingDeduction(uid: string): Promise<string | null> {
  const db = admin.firestore();
  const ref = db.collection("pendingDeductions").doc(uid);
  const now = Date.now();

  return db.runTransaction(async (tx) => {
    const doc = await tx.get(ref);
    if (!doc.exists) return null;

    const data = doc.data() ?? {};

    // Find the most recent confirmed, non-expired entry
    let latestKey: string | null = null;
    let latestTimestamp = 0;
    const expiredKeys: string[] = [];

    for (const [key, val] of Object.entries(data)) {
      if (!isValidEntry(val)) continue;
      if (now - val.ts > PENDING_DEDUCTION_TTL_MS) {
        expiredKeys.push(key);
      } else if (val.status === "confirmed" && val.ts > latestTimestamp) {
        latestKey = key;
        latestTimestamp = val.ts;
      }
    }

    if (!latestKey) {
      // No valid entries — just clean up expired ones if any
      if (expiredKeys.length > 0) {
        const cleanup: Record<string, ReturnType<typeof admin.firestore.FieldValue.delete>> = {};
        for (const key of expiredKeys) {
          cleanup[key] = admin.firestore.FieldValue.delete();
        }
        tx.update(ref, cleanup);
      }
      return null;
    }

    // Remove the consumed key and any expired entries
    const updates: Record<string, ReturnType<typeof admin.firestore.FieldValue.delete>> = {
      [latestKey]: admin.firestore.FieldValue.delete(),
    };
    for (const key of expiredKeys) {
      updates[key] = admin.firestore.FieldValue.delete();
    }
    tx.update(ref, updates);
    return latestKey;
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

  const {version, input} = request.data;
  if (!version || !input) {
    throw new HttpsError("invalid-argument", "Missing version or input");
  }

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

  return await response.json();
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

// ─── RevenueCat: Refund Generation Credit (callable) ─────────────────────

export const refundGenerationCredit = onCall(async (request) => {
  const uid = requireAuth(request);
  await enforceRateLimit(uid, "refundGenerationCredit");

  const deductionKey = await consumeLatestPendingDeduction(uid);
  if (!deductionKey) {
    throw new HttpsError("failed-precondition", "No matching deduction to refund");
  }

  // Use a refund-specific key for RevenueCat idempotency so the refund
  // transaction is distinct from the original deduction transaction.
  try {
    const refundKey = `refund-${deductionKey}`;
    const balance = await addCreditsInternal(uid, refundKey);
    return {balance};
  } catch (error) {
    // Restore the pending deduction so the client can retry the refund.
    try {
      await confirmPendingDeduction(uid, deductionKey);
    } catch (restoreError) {
      console.error(`refundGenerationCredit: failed to restore pending deduction — ${restoreError}`);
    }
    if (error instanceof HttpsError) throw error;
    throw new HttpsError("internal", `Refund failed: ${error}`);
  }
});

// ─── Free Generation: Check if already used (read-only) ─────────────────

export const checkFreeGenerationUsed = onCall(async (request) => {
  const uid = requireAuth(request);
  const db = admin.firestore();
  const doc = await db.collection("users").doc(uid).get();
  const used = doc.exists && doc.data()?.hasUsedFreeGeneration === true;
  return {used};
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

// ─── Free Generation: Atomic claim (server-side gate) ────────────────────

export const claimFreeGeneration = onCall(async (request) => {
  const uid = requireAuth(request);
  const db = admin.firestore();
  const userRef = db.collection("users").doc(uid);

  // Atomically check and set hasUsedFreeGeneration in a transaction.
  // Returns { claimed: true } if this is the first claim, { claimed: false } if already used.
  const claimed = await db.runTransaction(async (tx) => {
    const doc = await tx.get(userRef);
    if (doc.exists && doc.data()?.hasUsedFreeGeneration === true) {
      return false;
    }
    tx.set(userRef, {hasUsedFreeGeneration: true}, {merge: true});
    return true;
  });

  return {claimed};
});
