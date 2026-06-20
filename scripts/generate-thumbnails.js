#!/usr/bin/env node
/**
 * generate-thumbnails.js
 *
 * Generates 512×512 WebP background-scene thumbnails for new StudioSnap styles
 * using Replicate's Flux 1.1 Pro text-to-image model. No product image required —
 * each thumbnail shows the empty scene/surface where a product would be placed.
 *
 * Usage:
 *   node scripts/generate-thumbnails.js --key=r8_xxxx
 *
 * Options:
 *   --key      Replicate API key (required)
 *   --only     Comma-separated style IDs to (re)generate, e.g. --only=artisan_bakery,cafe_morning
 *   --force    Re-generate even if the output file already exists
 *
 * Requirements (install once):
 *   npm install sharp
 *
 * Model: black-forest-labs/flux-1.1-pro
 * Output saved to: composeApp/src/commonMain/composeResources/drawable/style_{id}.webp
 *
 * SECURITY: Never commit your Replicate API key. Pass it at runtime via --key=.
 */

"use strict";

const fs = require("fs");
const path = require("path");
const https = require("https");

// ── Output dir ────────────────────────────────────────────────────────────────

const OUTPUT_DIR = path.resolve(
  __dirname,
  "../composeApp/src/commonMain/composeResources/drawable"
);

// ── Thumbnail style definitions ───────────────────────────────────────────────
//
// Visual language from existing thumbnails:
//   - Surface-based: low-angle shot, empty foreground surface sharply in focus
//     (fills bottom 35–45%), atmospheric blurred background scene behind it.
//     The foreground must be completely clear — no products, no objects on top.
//   - Texture-based: material fills the entire square frame edge-to-edge.
//
// Suffix appended to every prompt:
//   "Empty surface, no products, no people. Square format, product photography
//    background, 512x512, photorealistic."

const THUMBNAIL_PROMPTS = [
  // ── Food ──────────────────────────────────────────────────────────────────
  {
    id: "artisan_bakery",
    prompt:
      "Low-angle product photography background. Rustic weathered wooden cutting board surface sharply in focus in the foreground, completely empty and clear. Warm golden window light from the left casting long shadows. Blurred background of a cozy bakery kitchen: warm amber tones, soft flour dust, pastry brush, dried herbs hanging. No products on the surface.",
  },
  {
    id: "cafe_morning",
    prompt:
      "Low-angle product photography background. White ceramic countertop surface sharply in focus in the foreground, completely empty and clear. Soft morning light filtering through a café window. Blurred background of a cozy coffee shop: cream and grey tones, blurred coffee mug, newspaper, pastry. No products on the surface.",
  },
  {
    id: "restaurant_plating",
    prompt:
      "Low-angle product photography background. Clean white slate serving board surface sharply in focus in the foreground, completely empty and clear. Professional restaurant overhead lighting, dramatic and clean. Blurred background of a fine dining restaurant: dark tones, blurred cutlery, white tablecloth, negative space. No products on the surface.",
  },
  {
    id: "health_bowl",
    prompt:
      "Low-angle product photography background. White marble kitchen countertop surface sharply in focus in the foreground, completely empty and clear. Bright midday natural light from a window. Blurred background: fresh green herbs, spinach leaves, a glass of water, clean wellness kitchen aesthetic. No products on the surface.",
  },
  {
    id: "street_food_grit",
    prompt:
      "Low-angle product photography background. Dark grey rough concrete surface sharply in focus in the foreground, completely empty and clear. Vibrant warm accent lighting from the side. Blurred background: wooden serving board texture, casual street food stall energy, warm bokeh lights. No products on the surface.",
  },
  {
    id: "spice_market",
    prompt:
      "Low-angle product photography background. Natural light cream linen surface sharply in focus in the foreground, completely empty and clear. Warm golden diffused light. Blurred background: scattered whole spices — star anise, cardamom, dried chiles — burlap texture, small brass scoops. No products on the surface.",
  },
  {
    id: "chocolate_indulgence",
    prompt:
      "Low-angle product photography background. Dark rich mahogany wooden surface sharply in focus in the foreground, completely empty and clear. Warm dramatic side lighting. Blurred background: scattered cocoa powder, cocoa beans, gold leaf accents, deep burgundy velvet fabric. Luxurious indulgent mood. No products on the surface.",
  },

  // ── Jewellery / Cosmetics ─────────────────────────────────────────────────
  {
    id: "minimalist_studio",
    prompt:
      "Low-angle product photography background. Perfectly clean light grey seamless studio surface sharply in focus in the foreground, completely empty and clear. Bright soft even studio lighting with a single directional rim light from the side. Pure white seamless background with absolute negative space. Ultra-modern minimal aesthetic. No products on the surface.",
  },
  {
    id: "vintage_antique",
    prompt:
      "Low-angle product photography background. Aged parchment paper on an antique dark wooden surface, sharply in focus in the foreground, completely empty and clear. Warm candlelight glow. Blurred background: vintage lace fabric, aged leather books, muted earth tones, nostalgic atmosphere. No products on the surface.",
  },
  {
    id: "golden_hour_glow",
    prompt:
      "Low-angle product photography background. Smooth neutral stone surface sharply in focus in the foreground, completely empty and clear. Bathed in warm golden late-afternoon sunlight streaming from the side. Blurred background: soft natural bokeh with rich gold and amber tones, dreamy romantic atmosphere. No products on the surface.",
  },
  {
    id: "dewy_skincare",
    prompt:
      "Low-angle product photography background. Wet white glossy surface with scattered water droplets, sharply in focus in the foreground, completely empty and clear. Bright fresh natural morning light. Blurred background: glass dropper bottles, botanical extracts, clean minimalist spa setting. No products on the surface.",
  },
  {
    id: "natural_organic",
    prompt:
      "Low-angle product photography background. Natural woven rattan surface sharply in focus in the foreground, completely empty and clear. Soft diffused natural daylight. Blurred background: dried flowers, fresh herbs, small glass jars of oil, earthy warm tones, clean ethical aesthetic. No products on the surface.",
  },

  // ── Electronics ───────────────────────────────────────────────────────────
  {
    id: "tech_sleek",
    prompt:
      "Low-angle product photography background. Dark gunmetal metallic surface sharply in focus in the foreground, completely empty and clear. Cool directional studio lighting creating clean hard-edged shadows. Blurred background: subtle carbon fibre texture, dark grey tones, futuristic minimal tech aesthetic. No products on the surface.",
  },
  {
    id: "unboxing_moment",
    prompt:
      "Low-angle product photography background. Cream white surface with premium tissue paper sharply in focus in the foreground, completely empty and clear. Soft warm studio lighting. Blurred background: white gift box, satin ribbon, luxury unboxing elements, celebration and gift aesthetic. No products on the surface.",
  },

  // ── Seasonal ──────────────────────────────────────────────────────────────
  {
    id: "valentines_romance",
    prompt:
      "Low-angle product photography background. Soft rose-pink velvet surface sharply in focus in the foreground, completely empty and clear. Romantic warm candlelight. Blurred background: fresh red roses, scattered rose petals, love letter with wax seal, luxury ribbon, intimate Valentine's atmosphere. No products on the surface.",
  },
  {
    id: "autumn_harvest",
    prompt:
      "Low-angle product photography background. Rich burnt orange and dark wood surface sharply in focus in the foreground, completely empty and clear. Warm golden hour light. Blurred background: fall foliage, dried autumn leaves, small pumpkins, cozy harvest atmosphere. No products on the surface.",
  },
  {
    id: "halloween_spooky",
    prompt:
      "Low-angle product photography background. Dark moody black surface sharply in focus in the foreground, completely empty and clear. Dramatic lighting with deep shadows and a muted orange accent glow from the side. Blurred background: faint pumpkins, spider webs, melting candles, playful spooky Halloween atmosphere. No products on the surface.",
  },

  // ── Pets ──────────────────────────────────────────────────────────────────
  {
    id: "pet_lifestyle",
    prompt:
      "Low-angle product photography background. Warm light oak wooden floor surface sharply in focus in the foreground, completely empty and clear. Soft natural afternoon window light. Blurred background: cozy knitted blanket, scattered pet toys (ball, rope toy), warm homely living room, pet lifestyle aesthetic. No products on the surface.",
  },
];

// Appended to every prompt to enforce square empty-scene format
const PROMPT_SUFFIX =
  " Empty surface with no objects on it. Product photography background scene only. Photorealistic, high quality, square composition.";

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

// ── Replicate API ─────────────────────────────────────────────────────────────

async function createPrediction(apiKey, prompt) {
  const body = JSON.stringify({
    input: {
      prompt: prompt + PROMPT_SUFFIX,
      aspect_ratio: "1:1",
      output_format: "jpg",
      output_quality: 90,
      safety_tolerance: 2,
    },
  });

  const { status, body: data } = await httpsRequest(
    "https://api.replicate.com/v1/models/black-forest-labs/flux-1.1-pro/predictions",
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json",
        "Content-Length": Buffer.byteLength(body),
        Prefer: "wait",
      },
    },
    body
  );

  if (status !== 201 && status !== 200) {
    throw new Error(`Create prediction failed (${status}): ${JSON.stringify(data)}`);
  }
  return data;
}

async function pollPrediction(apiKey, predictionId, timeoutMs = 120_000) {
  const start = Date.now();
  while (Date.now() - start < timeoutMs) {
    await sleep(2000);
    const { status, body: data } = await httpsRequest(
      `https://api.replicate.com/v1/predictions/${predictionId}`,
      {
        method: "GET",
        headers: { Authorization: `Bearer ${apiKey}` },
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
  // Handle redirects
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

// ── Main ──────────────────────────────────────────────────────────────────────

async function main() {
  const args = parseArgs();

  if (!args.key) {
    console.error("Error: --key=<replicate-api-key> is required");
    console.error("Usage: node scripts/generate-thumbnails.js --key=r8_xxxx");
    process.exit(1);
  }

  // Check sharp is installed
  let sharp;
  try {
    sharp = require("sharp");
  } catch {
    console.error('Error: "sharp" npm package not found. Run: npm install sharp');
    process.exit(1);
  }

  // Determine which styles to process
  let targets = THUMBNAIL_PROMPTS;
  if (args.only) {
    const ids = new Set(args.only.split(",").map((s) => s.trim()));
    targets = THUMBNAIL_PROMPTS.filter((s) => ids.has(s.id));
    if (targets.length === 0) {
      console.error(`No matching styles found for --only=${args.only}`);
      process.exit(1);
    }
  }

  console.log(`\n🎨 StudioSnap Thumbnail Generator`);
  console.log(`   Model: black-forest-labs/flux-1.1-pro`);
  console.log(`   Output: ${OUTPUT_DIR}`);
  console.log(`   Styles to generate: ${targets.length}`);
  console.log(`   (Text-to-image — no product photo needed)\n`);

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

    process.stdout.write(`⏳ ${style.id}`);
    try {
      const prediction = await createPrediction(args.key, style.prompt);

      let imageUrl;
      if (prediction.status === "succeeded") {
        // Prefer: wait returned synchronously
        imageUrl = Array.isArray(prediction.output)
          ? prediction.output[0]
          : prediction.output;
      } else {
        // Poll for completion
        process.stdout.write(" polling");
        const output = await pollPrediction(args.key, prediction.id);
        imageUrl = Array.isArray(output) ? output[0] : output;
      }

      process.stdout.write(" downloading");
      const imageBytes = await downloadImage(imageUrl);

      // Resize to 512×512 WebP, cropping to centre
      await sharp(imageBytes)
        .resize(512, 512, { fit: "cover", position: "centre" })
        .webp({ quality: 85 })
        .toFile(outputPath);

      console.log(` ✅  →  style_${style.id}.webp`);
      succeeded++;
    } catch (err) {
      console.log(`\n   ❌ ${err.message}`);
      failed++;
    }
  }

  console.log(
    `\n✅ Done — ${succeeded} generated, ${skipped} skipped, ${failed} failed`
  );

  if (succeeded > 0) {
    console.log(`\nNext steps:`);
    console.log(`  1. Update StyleRepositoryImpl.kt:`);
    console.log(
      `     Replace Res.drawable.style_placeholder with Res.drawable.style_<id>`
    );
    console.log(`     for each of the ${succeeded} generated style(s).`);
    console.log(`  2. ./gradlew composeApp:compileDebugKotlinAndroid`);
    console.log(`  3. ./gradlew composeApp:recordPaparazziDebug`);
  }
}

main().catch((err) => {
  console.error("\nFatal error:", err);
  process.exit(1);
});
