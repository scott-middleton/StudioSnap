#!/usr/bin/env node
/**
 * generate-onboarding-examples.js
 *
 * Generates real before/after onboarding example pairs by running sourced
 * "amateur" product photos through the actual production model StudioSnap
 * uses in-app (black-forest-labs/flux-kontext-dev), with the same prompts
 * the app ships with. Output is real generated output, not a mockup.
 *
 * Usage:
 *   node scripts/generate-onboarding-examples.js --key=r8_xxxx
 *
 * Options:
 *   --key         Replicate API key (required)
 *   --input-dir   Directory containing the source "before" photos
 *                 (default: the scratchpad folder used to source them)
 *   --only        Comma-separated pairing ids to (re)generate, e.g. --only=scarf_warm_linen
 *   --force       Re-generate even if the output file already exists
 *
 * Output: scripts/onboarding-examples-output/<id>_before.jpg
 *         scripts/onboarding-examples-output/<id>_after.jpg
 *
 * SECURITY: Never commit your Replicate API key. Pass it at runtime via --key=.
 */

"use strict";

const fs = require("fs");
const path = require("path");
const https = require("https");

// ── Paths ─────────────────────────────────────────────────────────────────────

const DEFAULT_INPUT_DIR =
  "/private/tmp/claude-501/-Users-scottmiddleton-StudioProjects-mobile-apps-StudioSnap/cacc72d4-bd2b-403e-9913-828489b6eb3e/scratchpad/product_photos";

const OUTPUT_DIR = path.resolve(__dirname, "onboarding-examples-output");

// ── Pairings ──────────────────────────────────────────────────────────────────
//
// Each pairing feeds one sourced "before" photo into the real production
// model using a verbatim style prompt copied from StyleRepositoryImpl.kt.
// Chosen to diversify onboarding beyond the existing mug-only examples,
// spanning CLOTHING, JEWELLERY and HOMEWARE.

const PAIRINGS = [
  {
    id: "scarf_warm_linen",
    inputFile: "wm_scarf.jpg",
    category: "CLOTHING",
    styleId: "warm_linen",
    kontextPrompt:
      "Place the product on a natural cream linen fabric surface with warm soft window light coming from the left. Shallow depth of field background. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "jewelry_silk_velvet",
    inputFile: "wm_jewelry_arp.jpg",
    category: "JEWELLERY",
    styleId: "silk_velvet",
    kontextPrompt:
      "Place the product on draped deep burgundy velvet fabric with soft directional lighting highlighting the fabric folds, rich and opulent feeling. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "candle_rustic_wood",
    inputFile: "wm_candle_jars.jpg",
    category: "HOMEWARE",
    styleId: "rustic_wood",
    kontextPrompt:
      "Place the product on a weathered dark reclaimed wood surface with warm ambient lighting, rustic farmhouse background slightly blurred. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "mug_terrazzo",
    inputFile: "wm_mug_victor.jpg",
    category: "HOMEWARE",
    styleId: "terrazzo",
    kontextPrompt:
      "Place the product on a white terrazzo surface with colorful stone chips, bright even overhead lighting, modern design-forward aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "soap_natural_organic",
    inputFile: "soap_bars.jpg",
    category: "COSMETICS",
    styleId: "natural_organic",
    kontextPrompt:
      "Place the product on a natural woven surface surrounded by organic botanicals—dried flowers, fresh herbs, small glass jars of oils—soft diffused natural daylight, earthy warm tones, clean ethical aesthetic. Keep the product exactly as-is, change only the background.",
  },
  // Retries: original mug_terrazzo/scarf_warm_linen pairings under-delivered
  // (terrazzo read as confetti, warm_linen barely changed anything). Trying
  // more distinctive styles so the before/after contrast actually reads.
  {
    id: "mug_marble_luxe",
    inputFile: "wm_mug_victor.jpg",
    category: "HOMEWARE",
    styleId: "marble_luxe",
    kontextPrompt:
      "Place the product on a white Carrara marble surface with soft overhead lighting and subtle gold accents in the blurred background. Elegant and minimal. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "scarf_sunset_glow",
    inputFile: "wm_scarf.jpg",
    category: "CLOTHING",
    styleId: "sunset_glow",
    kontextPrompt:
      "Place the product bathed in warm golden hour sunset light with a soft bokeh background of warm orange and pink tones. Dreamy and romantic atmosphere. Keep the product exactly as-is, change only the background.",
  },
  {
    // sunset_glow backlit the black scarf into an unrecognizable silhouette.
    // concrete_minimal lights from above, not behind, so the knit texture
    // should stay visible.
    id: "scarf_concrete_minimal",
    inputFile: "wm_scarf.jpg",
    category: "CLOTHING",
    styleId: "concrete_minimal",
    kontextPrompt:
      "Place the product on a raw concrete surface with minimalist industrial background, directional warm spotlight from above, deep shadows. Keep the product exactly as-is, change only the background.",
  },
];

// Matches BuildKontextPromptUseCase's shadow suffix, applied by default since
// that's how the app behaves with the shadow toggle on (the common case).
const SHADOW_SUFFIX = " Add a natural soft shadow beneath the product.";

// Same model version pinned in GenerationRepositoryImpl.kt
const KONTEXT_MODEL_VERSION =
  "85723d503c17da3f9fd9cecfb9987a8bf60ef747fd8f68a25d7636f88260eb59";

// Matches ExportFormat.VINTED_PORTRAIT — closest supported ratio to the
// onboarding card's 3:4 display aspect.
const ASPECT_RATIO = "4:5";

// Matches GenerationQuality.HIGH (the app's default)
const NUM_INFERENCE_STEPS = 30;
const GUIDANCE = 3.5;

// ── Arg parsing ───────────────────────────────────────────────────────────────

function parseArgs() {
  const args = {};
  process.argv.slice(2).forEach((arg) => {
    const [key, ...rest] = arg.replace(/^--/, "").split("=");
    args[key] = rest.join("=") || true;
  });
  return args;
}

// ── HTTP helpers ──────────────────────────────────────────────────────────────

function httpsRequest(url, options, body) {
  return new Promise((resolve, reject) => {
    const req = https.request(url, options, (res) => {
      const chunks = [];
      res.on("data", (chunk) => chunks.push(chunk));
      res.on("end", () => {
        const raw = Buffer.concat(chunks);
        if (options._binary) {
          resolve({ status: res.statusCode, body: raw });
        } else {
          try {
            resolve({ status: res.statusCode, body: JSON.parse(raw.toString()) });
          } catch {
            resolve({ status: res.statusCode, body: raw.toString() });
          }
        }
      });
    });
    req.on("error", reject);
    if (body) req.write(body);
    req.end();
  });
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function downloadImage(url) {
  return new Promise((resolve, reject) => {
    function fetch(url) {
      const parsed = new URL(url);
      const options = {
        hostname: parsed.hostname,
        path: parsed.pathname + parsed.search,
        method: "GET",
        _binary: true,
      };
      const req = https.request(options, (res) => {
        if (res.statusCode === 301 || res.statusCode === 302) {
          fetch(res.headers.location);
          return;
        }
        const chunks = [];
        res.on("data", (chunk) => chunks.push(chunk));
        res.on("end", () => resolve(Buffer.concat(chunks)));
      });
      req.on("error", reject);
      req.end();
    }
    fetch(url);
  });
}

// ── Replicate API (mirrors functions/src/index.ts: versioned predictions,
//    "Token" auth header — NOT "Bearer") ─────────────────────────────────────

async function createPrediction(apiKey, version, input) {
  const body = JSON.stringify({ version, input });

  const { status, body: data } = await httpsRequest(
    "https://api.replicate.com/v1/predictions",
    {
      method: "POST",
      headers: {
        Authorization: `Token ${apiKey}`,
        "Content-Type": "application/json",
        "Content-Length": Buffer.byteLength(body),
      },
    },
    body
  );

  if (status !== 201 && status !== 200) {
    throw new Error(`Create prediction failed (${status}): ${JSON.stringify(data)}`);
  }
  return data;
}

async function pollPrediction(apiKey, predictionId, timeoutMs = 180_000) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    await sleep(2000);
    const { status, body: data } = await httpsRequest(
      `https://api.replicate.com/v1/predictions/${predictionId}`,
      {
        method: "GET",
        headers: { Authorization: `Token ${apiKey}` },
      }
    );
    if (status !== 200) {
      throw new Error(`Poll failed (${status}): ${JSON.stringify(data)}`);
    }
    if (data.status === "succeeded") return data.output;
    if (data.status === "failed" || data.status === "canceled") {
      throw new Error(`Prediction ${data.status}: ${data.error}`);
    }
    process.stdout.write(".");
  }
  throw new Error("Timed out waiting for prediction");
}

// ── Main ──────────────────────────────────────────────────────────────────────

async function main() {
  const args = parseArgs();

  if (!args.key) {
    console.error("Error: --key=<replicate-api-key> is required");
    console.error("Usage: node scripts/generate-onboarding-examples.js --key=r8_xxxx");
    process.exit(1);
  }

  const inputDir = args["input-dir"] || DEFAULT_INPUT_DIR;

  let targets = PAIRINGS;
  if (args.only) {
    const ids = new Set(args.only.split(",").map((s) => s.trim()));
    targets = PAIRINGS.filter((p) => ids.has(p.id));
    if (targets.length === 0) {
      console.error(`No matching pairings found for --only=${args.only}`);
      process.exit(1);
    }
  }

  fs.mkdirSync(OUTPUT_DIR, { recursive: true });

  console.log(`\nStudioSnap Onboarding Example Generator`);
  console.log(`   Model: black-forest-labs/flux-kontext-dev (version ${KONTEXT_MODEL_VERSION.slice(0, 12)}...)`);
  console.log(`   Input dir: ${inputDir}`);
  console.log(`   Output dir: ${OUTPUT_DIR}`);
  console.log(`   Pairings to generate: ${targets.length}\n`);

  let succeeded = 0;
  let skipped = 0;
  let failed = 0;

  for (const pairing of targets) {
    const inputPath = path.join(inputDir, pairing.inputFile);
    const beforeOutPath = path.join(OUTPUT_DIR, `${pairing.id}_before.jpg`);
    const afterOutPath = path.join(OUTPUT_DIR, `${pairing.id}_after.jpg`);

    if (fs.existsSync(afterOutPath) && !args.force) {
      console.log(`skip  ${pairing.id} — already exists (use --force to regenerate)`);
      skipped++;
      continue;
    }

    if (!fs.existsSync(inputPath)) {
      console.log(`fail  ${pairing.id} — input photo not found: ${inputPath}`);
      failed++;
      continue;
    }

    process.stdout.write(`wait  ${pairing.id} (${pairing.category} / ${pairing.styleId})`);
    try {
      const imageBytes = fs.readFileSync(inputPath);
      const dataUri = `data:image/jpeg;base64,${imageBytes.toString("base64")}`;

      const input = {
        prompt: pairing.kontextPrompt + SHADOW_SUFFIX,
        input_image: dataUri,
        output_format: "jpg",
        aspect_ratio: ASPECT_RATIO,
        guidance: GUIDANCE,
        num_inference_steps: NUM_INFERENCE_STEPS,
      };

      const prediction = await createPrediction(args.key, KONTEXT_MODEL_VERSION, input);

      let outputUrl;
      if (prediction.status === "succeeded") {
        outputUrl = Array.isArray(prediction.output) ? prediction.output[0] : prediction.output;
      } else {
        process.stdout.write(" polling");
        const output = await pollPrediction(args.key, prediction.id);
        outputUrl = Array.isArray(output) ? output[0] : output;
      }

      process.stdout.write(" downloading");
      const afterBytes = await downloadImage(outputUrl);

      fs.copyFileSync(inputPath, beforeOutPath);
      fs.writeFileSync(afterOutPath, afterBytes);

      console.log(`  done  -> ${pairing.id}_before.jpg / ${pairing.id}_after.jpg`);
      succeeded++;
    } catch (err) {
      console.log(`\n   FAILED: ${err.message}`);
      failed++;
    }
  }

  console.log(`\nDone — ${succeeded} generated, ${skipped} skipped, ${failed} failed`);

  if (succeeded > 0) {
    console.log(`\nReview the before/after pairs in ${OUTPUT_DIR}, then pick which`);
    console.log(`ones to wire into OnboardingBeforeAfterPage.kt / a new onboarding page.`);
  }
}

main().catch((err) => {
  console.error("\nFatal error:", err);
  process.exit(1);
});
