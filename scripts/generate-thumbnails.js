#!/usr/bin/env node
/**
 * generate-thumbnails.js
 *
 * Generates 512×512 WebP thumbnails for new StudioSnap styles via Replicate's
 * Flux Kontext API, then saves them to composeResources/drawable/.
 *
 * Usage:
 *   node scripts/generate-thumbnails.js --key=r8_xxxx --input=scripts/sample-product.jpg
 *
 * Options:
 *   --key      Replicate API key (required)
 *   --input    Path to a sample product image (required)
 *   --only     Comma-separated list of style IDs to regenerate (optional; defaults to all)
 *   --force    Re-generate even if the output file already exists
 *
 * Requirements (install once):
 *   npm install sharp
 *
 * The script uses the same Flux Kontext model version the app uses:
 *   black-forest-labs/flux-kontext-pro
 *   version: 85723d503c17da3f9fd9cecfb9987a8bf60ef747fd8f68a25d7636f88260eb59
 *
 * SECURITY: Never commit your Replicate API key. Pass it at runtime via --key=.
 */

"use strict";

const fs = require("fs");
const path = require("path");
const https = require("https");
const { execSync } = require("child_process");

// ── Config ────────────────────────────────────────────────────────────────────

const MODEL_VERSION =
  "85723d503c17da3f9fd9cecfb9987a8bf60ef747fd8f68a25d7636f88260eb59";

const OUTPUT_DIR = path.resolve(
  __dirname,
  "../composeApp/src/commonMain/composeResources/drawable"
);

// ── Style definitions ─────────────────────────────────────────────────────────
// These are the 18 new styles that need generated thumbnails.
// Existing styles already have real thumbnails and are excluded.

const STYLES = [
  {
    id: "artisan_bakery",
    prompt:
      "Place the product on a rustic wooden cutting board with a warm linen cloth draped beside it, soft golden window light from the left casting long shadows, scattered flour, a pastry brush, and blurred fresh herbs in the background. Artisan bakery aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "cafe_morning",
    prompt:
      "Place the product on a white ceramic surface beside a ceramic mug of black coffee, soft morning light filtering through a café window, neutral cream and grey tones, blurred newspaper and pastry in the background. Cozy coffee shop ambiance. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "restaurant_plating",
    prompt:
      "Place the product on a clean white plate or slate serving board with professional restaurant overhead lighting, artistic negative space, subtle garnish elements and blurred utensils in the background. Fine dining presentation. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "health_bowl",
    prompt:
      "Place the product on a white marble countertop with bright midday natural light, fresh green elements (herbs, spinach leaves) nearby, a glass of water and fitness towel slightly visible in background, clean wellness aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "street_food_grit",
    prompt:
      "Place the product on a dark grey concrete surface with vibrant warm accent lighting, wooden serving board texture nearby, casual street food energy, food truck aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "spice_market",
    prompt:
      "Place the product on a light neutral linen surface with scattered whole spices—star anise, cardamom, dried chiles—nearby, warm golden diffused light, burlap texture and small brass scoops in soft focus background. Artisanal spice market aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "chocolate_indulgence",
    prompt:
      "Place the product on a dark rich wooden surface with scattered cocoa powder, cocoa beans, and gold leaf accents, warm dramatic side lighting, blurred chocolate pieces and deep burgundy velvet fabric in background. Luxurious indulgent mood. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "minimalist_studio",
    prompt:
      "Place the product on a perfectly clean light grey surface, bright soft even studio lighting with a single directional rim light, pure white seamless background, absolute negative space, ultra-modern minimal aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "vintage_antique",
    prompt:
      "Place the product on aged parchment paper with an antique wooden surface beneath, warm candlelight, subtle vintage lace or aged fabric nearby, muted earth tones, nostalgic timeless atmosphere. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "golden_hour_glow",
    prompt:
      "Place the product bathed in warm golden late-afternoon sunlight on a neutral stone surface, soft natural bokeh background with gold and amber tones, romantic dreamy lighting. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "dewy_skincare",
    prompt:
      "Place the product on a wet reflective white surface with water droplets, bright fresh natural morning light, glass dropper bottles and botanical extracts slightly visible in background, clean minimalist spa aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "natural_organic",
    prompt:
      "Place the product on a natural woven surface surrounded by organic botanicals—dried flowers, fresh herbs, small glass jars of oils—soft diffused natural daylight, earthy warm tones, clean ethical aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "tech_sleek",
    prompt:
      "Place the product on a dark gunmetal metallic surface with clean hard-edged shadows from cool directional studio lighting, subtle glass or carbon fibre texture in background, futuristic minimal tech aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "unboxing_moment",
    prompt:
      "Place the product on a cream surface surrounded by premium unboxing elements—tissue paper, satin ribbon, clean white gift box—soft warm studio lighting, excitement and luxury gift aesthetic. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "valentines_romance",
    prompt:
      "Place the product on a soft rose-pink velvet surface with romantic candlelight, fresh red roses softly arranged nearby, scattered rose petals, warm intimate lighting, love letter and luxury ribbon in background. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "autumn_harvest",
    prompt:
      "Place the product on a rich burgundy or burnt orange surface with fall foliage, dried leaves, and small pumpkins softly blurred nearby, warm golden hour light, cozy autumn harvest mood. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "halloween_spooky",
    prompt:
      "Place the product on a dark moody surface with subtle Halloween elements—faint pumpkins, spider webs, melting candles—dramatic lighting with deep shadows and muted orange accent glow, playful spooky atmosphere. Keep the product exactly as-is, change only the background.",
  },
  {
    id: "pet_lifestyle",
    prompt:
      "Place the product on a warm light oak wooden floor with a cozy knitted blanket nearby, soft natural afternoon window light, scattered pet toys (ball, rope toy) blurred in background, warm homely pet lifestyle aesthetic. Keep the product exactly as-is, change only the background.",
  },
];

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
        const data = Buffer.concat(chunks);
        if (options._binary) {
          resolve({ status: res.statusCode, body: data });
        } else {
          try {
            resolve({ status: res.statusCode, body: JSON.parse(data.toString()) });
          } catch {
            resolve({ status: res.statusCode, body: data.toString() });
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

// ── Replicate API ─────────────────────────────────────────────────────────────

async function createPrediction(apiKey, imageDataUrl, prompt) {
  const body = JSON.stringify({
    version: MODEL_VERSION,
    input: {
      prompt,
      input_image: imageDataUrl,
      aspect_ratio: "1:1",
      guidance: 3.5,
      num_inference_steps: 30,
      output_format: "jpg",
    },
  });

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

  if (status !== 201) {
    throw new Error(`Create prediction failed (${status}): ${JSON.stringify(data)}`);
  }
  return data;
}

async function pollPrediction(apiKey, predictionId, timeoutMs = 120_000) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    await sleep(3000);
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

async function downloadImage(url) {
  const { status, body } = await httpsRequest(url, {
    method: "GET",
    _binary: true,
  });
  if (status !== 200) throw new Error(`Download failed (${status})`);
  return body;
}

// ── Main ──────────────────────────────────────────────────────────────────────

async function main() {
  const args = parseArgs();

  if (!args.key) {
    console.error("Error: --key=<replicate-api-key> is required");
    process.exit(1);
  }
  if (!args.input) {
    console.error("Error: --input=<path-to-sample-product-image> is required");
    process.exit(1);
  }
  if (!fs.existsSync(args.input)) {
    console.error(`Error: input file not found: ${args.input}`);
    process.exit(1);
  }

  // Check sharp is installed
  let sharp;
  try {
    sharp = require("sharp");
  } catch {
    console.error(
      'Error: "sharp" npm package not found. Run: npm install sharp'
    );
    process.exit(1);
  }

  // Determine which styles to process
  let targets = STYLES;
  if (args.only) {
    const ids = new Set(args.only.split(",").map((s) => s.trim()));
    targets = STYLES.filter((s) => ids.has(s.id));
    if (targets.length === 0) {
      console.error(`No matching styles found for --only=${args.only}`);
      process.exit(1);
    }
  }

  // Read and encode input image
  const inputBytes = fs.readFileSync(args.input);
  const ext = path.extname(args.input).slice(1).toLowerCase();
  const mimeType = ext === "jpg" || ext === "jpeg" ? "image/jpeg" : "image/png";
  const imageDataUrl = `data:${mimeType};base64,${inputBytes.toString("base64")}`;

  console.log(`\n🎨 StudioSnap Thumbnail Generator`);
  console.log(`   Input: ${args.input}`);
  console.log(`   Output: ${OUTPUT_DIR}`);
  console.log(`   Styles to generate: ${targets.length}\n`);

  let succeeded = 0;
  let skipped = 0;
  let failed = 0;

  for (const style of targets) {
    const outputPath = path.join(OUTPUT_DIR, `style_${style.id}.webp`);

    if (fs.existsSync(outputPath) && !args.force) {
      console.log(`⏭  ${style.id} — already exists (use --force to regenerate)`);
      skipped++;
      continue;
    }

    process.stdout.write(`⏳ ${style.id} — generating`);
    try {
      const prediction = await createPrediction(args.key, imageDataUrl, style.prompt);
      const output = await pollPrediction(args.key, prediction.id);

      // output is an array; take first element
      const imageUrl = Array.isArray(output) ? output[0] : output;
      const imageBytes = await downloadImage(imageUrl);

      // Resize to 512×512 WebP
      await sharp(imageBytes)
        .resize(512, 512, { fit: "cover", position: "centre" })
        .webp({ quality: 85 })
        .toFile(outputPath);

      console.log(` ✅`);
      succeeded++;
    } catch (err) {
      console.log(` ❌ ${err.message}`);
      failed++;
    }
  }

  console.log(`\n✅ Done — ${succeeded} generated, ${skipped} skipped, ${failed} failed`);

  if (succeeded > 0) {
    console.log(`\nNext steps:`);
    console.log(
      `  1. Update StyleRepositoryImpl.kt: replace style_placeholder references`
    );
    console.log(
      `     with Res.drawable.style_<id> for each generated thumbnail`
    );
    console.log(
      `  2. Run: ./gradlew composeApp:compileDebugKotlinAndroid`
    );
    console.log(`  3. Run: ./gradlew composeApp:recordPaparazziDebug`);
  }
}

main().catch((err) => {
  console.error("\nFatal error:", err);
  process.exit(1);
});
