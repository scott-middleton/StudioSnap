import {onCall, HttpsError} from "firebase-functions/v2/https";

const REPLICATE_BASE_URL = "https://api.replicate.com/v1";
const REVENUECAT_BASE_URL = "https://api.revenuecat.com/v2";
const REVENUECAT_PROJECT_ID = "proj30aee59a";

const CURRENCY_CODE = "credits";

const MAX_RETRIES = 3;
const RETRY_BASE_DELAY_MS = 1000;

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

export const createModelPrediction = onCall({enforceAppCheck: true}, async (request) => {
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

export const createVersionPrediction = onCall({enforceAppCheck: true}, async (request) => {
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

export const getPrediction = onCall({enforceAppCheck: true}, async (request) => {
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

export const fetchUserCredits = onCall({enforceAppCheck: true}, async (request) => {
  const secretKey = process.env.REVENUECAT_SECRET_KEY;
  if (!secretKey) {
    throw new HttpsError("internal", "RevenueCat secret key not configured");
  }

  const {customerId} = request.data;
  if (!customerId) {
    throw new HttpsError("invalid-argument", "Missing customerId");
  }

  const url = `${REVENUECAT_BASE_URL}/projects/${REVENUECAT_PROJECT_ID}/customers/${customerId}/virtual_currencies`;
  console.log(`fetchUserCredits: querying customerId=${customerId}, url=${url}`);
  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${secretKey}`,
    },
  });

  if (response.status === 404) {
    console.log(`fetchUserCredits: 404 for customerId=${customerId}`);
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

// ─── RevenueCat: Deduct Credits ───────────────────────────────────────────

export const deductCredits = onCall({enforceAppCheck: true}, async (request) => {
  const secretKey = process.env.REVENUECAT_SECRET_KEY;
  if (!secretKey) {
    throw new HttpsError("internal", "RevenueCat secret key not configured");
  }

  const {customerId, amount, idempotencyKey} = request.data;
  if (!customerId || !amount) {
    throw new HttpsError("invalid-argument", "Missing customerId or amount");
  }

  const url = `${REVENUECAT_BASE_URL}/projects/${REVENUECAT_PROJECT_ID}/customers/${customerId}/virtual_currencies/transactions`;
  const response = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${secretKey}`,
      "Content-Type": "application/json",
      "Idempotency-Key":
        idempotencyKey || `${customerId}-${Date.now()}-${amount}`,
    },
    body: JSON.stringify({
      adjustments: {[CURRENCY_CODE]: -amount},
    }),
  });

  if (response.status === 422) {
    throw new HttpsError("failed-precondition", "Insufficient credits");
  }

  if (!response.ok) {
    const errorBody = await response.text();
    console.error(
      `deductCredits failed: HTTP ${response.status} — ${errorBody}`
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
  return {balance: credits?.balance ?? 0};
});

// ─── RevenueCat: Add Credits (Refund) ─────────────────────────────────────

export const addCredits = onCall({enforceAppCheck: true}, async (request) => {
  const secretKey = process.env.REVENUECAT_SECRET_KEY;
  if (!secretKey) {
    throw new HttpsError("internal", "RevenueCat secret key not configured");
  }

  const {customerId, amount, idempotencyKey} = request.data;
  if (!customerId || !amount) {
    throw new HttpsError("invalid-argument", "Missing customerId or amount");
  }

  const url = `${REVENUECAT_BASE_URL}/projects/${REVENUECAT_PROJECT_ID}/customers/${customerId}/virtual_currencies/transactions`;
  const response = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${secretKey}`,
      "Content-Type": "application/json",
      "Idempotency-Key":
        idempotencyKey || `${customerId}-refund-${Date.now()}-${amount}`,
    },
    body: JSON.stringify({
      adjustments: {[CURRENCY_CODE]: amount},
    }),
  });

  if (!response.ok) {
    const errorBody = await response.text();
    console.error(
      `addCredits failed: HTTP ${response.status} — ${errorBody}`
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
  return {balance: credits?.balance ?? 0};
});
