package com.middleton.studiosnap.feature.home.data.repository

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.home.domain.model.SeasonalWindow
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory.*
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import kotlinx.datetime.Month
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.style_artisan_bakery
import studiosnap.composeapp.generated.resources.style_autumn_harvest
import studiosnap.composeapp.generated.resources.style_beach_vibes
import studiosnap.composeapp.generated.resources.style_botanical_garden
import studiosnap.composeapp.generated.resources.style_cafe_morning
import studiosnap.composeapp.generated.resources.style_chocolate_indulgence
import studiosnap.composeapp.generated.resources.style_christmas
import studiosnap.composeapp.generated.resources.style_clean_white
import studiosnap.composeapp.generated.resources.style_concrete_minimal
import studiosnap.composeapp.generated.resources.style_dark_moody
import studiosnap.composeapp.generated.resources.style_dewy_skincare
import studiosnap.composeapp.generated.resources.style_golden_hour_glow
import studiosnap.composeapp.generated.resources.style_gradient_studio
import studiosnap.composeapp.generated.resources.style_halloween_spooky
import studiosnap.composeapp.generated.resources.style_health_bowl
import studiosnap.composeapp.generated.resources.style_marble_luxe
import studiosnap.composeapp.generated.resources.style_minimalist_studio
import studiosnap.composeapp.generated.resources.style_morning_kitchen
import studiosnap.composeapp.generated.resources.style_natural_organic
import studiosnap.composeapp.generated.resources.style_neon_pop
import studiosnap.composeapp.generated.resources.style_paper_craft
import studiosnap.composeapp.generated.resources.style_pastel_dream
import studiosnap.composeapp.generated.resources.style_pet_lifestyle
import studiosnap.composeapp.generated.resources.style_placeholder
import studiosnap.composeapp.generated.resources.style_restaurant_plating
import studiosnap.composeapp.generated.resources.style_rustic_wood
import studiosnap.composeapp.generated.resources.style_silk_velvet
import studiosnap.composeapp.generated.resources.style_spice_market
import studiosnap.composeapp.generated.resources.style_spring_garden
import studiosnap.composeapp.generated.resources.style_street_food_grit
import studiosnap.composeapp.generated.resources.style_sunset_glow
import studiosnap.composeapp.generated.resources.style_tech_sleek
import studiosnap.composeapp.generated.resources.style_terrazzo
import studiosnap.composeapp.generated.resources.style_unboxing_moment
import studiosnap.composeapp.generated.resources.style_valentines_romance
import studiosnap.composeapp.generated.resources.style_vintage_antique
import studiosnap.composeapp.generated.resources.style_warm_linen

/**
 * In-memory style library with hardcoded Kontext prompts.
 * Prompts are intentionally English-only — Flux Kontext is a text-guided model
 * that works best with English prompts regardless of the user's locale.
 *
 * To add a new style:
 * 1. Add a 512x512 WebP thumbnail to composeResources/drawable/
 * 2. Add a new Style entry below with the DrawableResource reference
 * 3. Add a string resource for the display name in strings.xml
 *
 * Styles with [style_placeholder] thumbnails are awaiting generated images.
 * Run scripts/generate-thumbnails.js to produce them.
 */
class StyleRepositoryImpl : StyleRepository {

    private val styles: List<Style> = buildStyleLibrary()

    override fun getAllStyles(): List<Style> = styles

    override fun getStylesByCategory(category: StyleCategory): List<Style> {
        if (category == ALL) return styles
        return styles.filter { category in it.categories }
    }

    override fun getStyleById(id: String): Style? {
        return styles.find { it.id == id }
    }

    companion object {
        private fun buildStyleLibrary(): List<Style> = listOf(

            // ── Evergreen / Universal ─────────────────────────────────────────────

            Style(
                id = "clean_white",
                displayName = UiText.StringResource(Res.string.style_clean_white),
                categories = setOf(ALL),
                thumbnail = Res.drawable.style_clean_white,
                kontextPrompt = "Place the product on a pure white seamless background with soft even studio lighting and a subtle ground shadow. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "gradient_studio",
                displayName = UiText.StringResource(Res.string.style_gradient_studio),
                categories = setOf(ALL),
                thumbnail = Res.drawable.style_gradient_studio,
                kontextPrompt = "Place the product on a reflective surface with a smooth gradient background transitioning from light grey to medium grey, professional product photography studio lighting. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "minimalist_studio",
                displayName = UiText.StringResource(Res.string.style_minimalist_studio),
                categories = setOf(JEWELLERY, COSMETICS, ELECTRONICS),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a perfectly clean light grey surface, bright soft even studio lighting with a single directional rim light, pure white seamless background, absolute negative space, ultra-modern minimal aesthetic. Keep the product exactly as-is, change only the background."
            ),

            // ── Clothing ──────────────────────────────────────────────────────────

            Style(
                id = "warm_linen",
                displayName = UiText.StringResource(Res.string.style_warm_linen),
                categories = setOf(CLOTHING, JEWELLERY, HOMEWARE),
                thumbnail = Res.drawable.style_warm_linen,
                kontextPrompt = "Place the product on a natural cream linen fabric surface with warm soft window light coming from the left. Shallow depth of field background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "concrete_minimal",
                displayName = UiText.StringResource(Res.string.style_concrete_minimal),
                categories = setOf(CLOTHING, HOMEWARE, OTHER),
                thumbnail = Res.drawable.style_concrete_minimal,
                kontextPrompt = "Place the product on a raw concrete surface with minimalist industrial background, directional warm spotlight from above, deep shadows. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "sunset_glow",
                displayName = UiText.StringResource(Res.string.style_sunset_glow),
                categories = setOf(CLOTHING, JEWELLERY),
                thumbnail = Res.drawable.style_sunset_glow,
                kontextPrompt = "Place the product bathed in warm golden hour sunset light with a soft bokeh background of warm orange and pink tones. Dreamy and romantic atmosphere. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "pastel_dream",
                displayName = UiText.StringResource(Res.string.style_pastel_dream),
                categories = setOf(CLOTHING, COSMETICS, JEWELLERY),
                thumbnail = Res.drawable.style_pastel_dream,
                kontextPrompt = "Place the product on a soft pastel pink surface with a gradient background blending light pink to lavender, diffused even lighting, gentle and feminine mood. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "golden_hour_glow",
                displayName = UiText.StringResource(Res.string.style_golden_hour_glow),
                categories = setOf(JEWELLERY, COSMETICS, CLOTHING),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product bathed in warm golden late-afternoon sunlight on a neutral stone surface, soft natural bokeh background with gold and amber tones, romantic dreamy lighting. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "neon_pop",
                displayName = UiText.StringResource(Res.string.style_neon_pop),
                categories = setOf(CLOTHING, ELECTRONICS, OTHER),
                thumbnail = Res.drawable.style_neon_pop,
                kontextPrompt = "Place the product against a dark background with vibrant neon light accents in pink and blue, urban night aesthetic, reflective surface with neon color reflections. Keep the product exactly as-is, change only the background."
            ),

            // ── Jewellery & Cosmetics ─────────────────────────────────────────────

            Style(
                id = "marble_luxe",
                displayName = UiText.StringResource(Res.string.style_marble_luxe),
                categories = setOf(JEWELLERY, COSMETICS, HOMEWARE),
                thumbnail = Res.drawable.style_marble_luxe,
                kontextPrompt = "Place the product on a white Carrara marble surface with soft overhead lighting and subtle gold accents in the blurred background. Elegant and minimal. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "silk_velvet",
                displayName = UiText.StringResource(Res.string.style_silk_velvet),
                categories = setOf(JEWELLERY, COSMETICS),
                thumbnail = Res.drawable.style_silk_velvet,
                kontextPrompt = "Place the product on draped deep burgundy velvet fabric with soft directional lighting highlighting the fabric folds, rich and opulent feeling. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "terrazzo",
                displayName = UiText.StringResource(Res.string.style_terrazzo),
                categories = setOf(HOMEWARE, COSMETICS, JEWELLERY),
                thumbnail = Res.drawable.style_terrazzo,
                kontextPrompt = "Place the product on a white terrazzo surface with colorful stone chips, bright even overhead lighting, modern design-forward aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "dewy_skincare",
                displayName = UiText.StringResource(Res.string.style_dewy_skincare),
                categories = setOf(COSMETICS),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a wet reflective white surface with water droplets, bright fresh natural morning light, glass dropper bottles and botanical extracts slightly visible in background, clean minimalist spa aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "natural_organic",
                displayName = UiText.StringResource(Res.string.style_natural_organic),
                categories = setOf(COSMETICS, FOOD),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a natural woven surface surrounded by organic botanicals—dried flowers, fresh herbs, small glass jars of oils—soft diffused natural daylight, earthy warm tones, clean ethical aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "vintage_antique",
                displayName = UiText.StringResource(Res.string.style_vintage_antique),
                categories = setOf(JEWELLERY, HOMEWARE, BOOKS_STATIONERY),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on aged parchment paper with an antique wooden surface beneath, warm candlelight, subtle vintage lace or aged fabric nearby, muted earth tones, nostalgic timeless atmosphere. Keep the product exactly as-is, change only the background."
            ),

            // ── Homeware ──────────────────────────────────────────────────────────

            Style(
                id = "morning_kitchen",
                displayName = UiText.StringResource(Res.string.style_morning_kitchen),
                categories = setOf(FOOD, HOMEWARE),
                thumbnail = Res.drawable.style_morning_kitchen,
                kontextPrompt = "Place the product on a light oak wooden kitchen counter with morning sunlight streaming through a window, soft shadows, green herbs and a coffee cup blurred in the background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "rustic_wood",
                displayName = UiText.StringResource(Res.string.style_rustic_wood),
                categories = setOf(FOOD, HOMEWARE, OTHER),
                thumbnail = Res.drawable.style_rustic_wood,
                kontextPrompt = "Place the product on a weathered dark reclaimed wood surface with warm ambient lighting, rustic farmhouse background slightly blurred. Keep the product exactly as-is, change only the background."
            ),

            // ── Food ──────────────────────────────────────────────────────────────

            Style(
                id = "dark_moody",
                displayName = UiText.StringResource(Res.string.style_dark_moody),
                categories = setOf(JEWELLERY, COSMETICS, FOOD),
                thumbnail = Res.drawable.style_dark_moody,
                kontextPrompt = "Place the product on a dark slate surface with dramatic low-key lighting from one side, deep rich shadows, luxurious dark background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "artisan_bakery",
                displayName = UiText.StringResource(Res.string.style_artisan_bakery),
                categories = setOf(FOOD),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a rustic wooden cutting board with a warm linen cloth draped beside it, soft golden window light from the left casting long shadows, scattered flour, a pastry brush, and blurred fresh herbs in the background. Artisan bakery aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "cafe_morning",
                displayName = UiText.StringResource(Res.string.style_cafe_morning),
                categories = setOf(FOOD),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a white ceramic surface beside a ceramic mug of black coffee, soft morning light filtering through a café window, neutral cream and grey tones, blurred newspaper and pastry in the background. Cozy coffee shop ambiance. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "restaurant_plating",
                displayName = UiText.StringResource(Res.string.style_restaurant_plating),
                categories = setOf(FOOD),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a clean white plate or slate serving board with professional restaurant overhead lighting, artistic negative space, subtle garnish elements and blurred utensils in the background. Fine dining presentation. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "health_bowl",
                displayName = UiText.StringResource(Res.string.style_health_bowl),
                categories = setOf(FOOD),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a white marble countertop with bright midday natural light, fresh green elements (herbs, spinach leaves) nearby, a glass of water and fitness towel slightly visible in background, clean wellness aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "street_food_grit",
                displayName = UiText.StringResource(Res.string.style_street_food_grit),
                categories = setOf(FOOD),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a dark grey concrete surface with vibrant warm accent lighting, wooden serving board texture nearby, casual street food energy, food truck aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "spice_market",
                displayName = UiText.StringResource(Res.string.style_spice_market),
                categories = setOf(FOOD),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a light neutral linen surface with scattered whole spices—star anise, cardamom, dried chiles—nearby, warm golden diffused light, burlap texture and small brass scoops in soft focus background. Artisanal spice market aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "chocolate_indulgence",
                displayName = UiText.StringResource(Res.string.style_chocolate_indulgence),
                categories = setOf(FOOD),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a dark rich wooden surface with scattered cocoa powder, cocoa beans, and gold leaf accents, warm dramatic side lighting, blurred chocolate pieces and deep burgundy velvet fabric in background. Luxurious indulgent mood. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.JANUARY, Month.FEBRUARY)
            ),

            // ── Electronics ───────────────────────────────────────────────────────

            Style(
                id = "tech_sleek",
                displayName = UiText.StringResource(Res.string.style_tech_sleek),
                categories = setOf(ELECTRONICS),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a dark gunmetal metallic surface with clean hard-edged shadows from cool directional studio lighting, subtle glass or carbon fibre texture in background, futuristic minimal tech aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "unboxing_moment",
                displayName = UiText.StringResource(Res.string.style_unboxing_moment),
                categories = setOf(ELECTRONICS, TOYS_KIDS),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a cream surface surrounded by premium unboxing elements—tissue paper, satin ribbon, clean white gift box—soft warm studio lighting, excitement and luxury gift aesthetic. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.OCTOBER, Month.DECEMBER)
            ),

            // ── Pets ──────────────────────────────────────────────────────────────

            Style(
                id = "pet_lifestyle",
                displayName = UiText.StringResource(Res.string.style_pet_lifestyle),
                categories = setOf(PETS),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a warm light oak wooden floor with a cozy knitted blanket nearby, soft natural afternoon window light, scattered pet toys (ball, rope toy) blurred in background, warm homely pet lifestyle aesthetic. Keep the product exactly as-is, change only the background."
            ),

            // ── Seasonal ──────────────────────────────────────────────────────────

            Style(
                id = "botanical_garden",
                displayName = UiText.StringResource(Res.string.style_botanical_garden),
                categories = setOf(COSMETICS, JEWELLERY),
                thumbnail = Res.drawable.style_botanical_garden,
                kontextPrompt = "Place the product surrounded by lush green tropical leaves and small white flowers, natural dappled sunlight, organic and fresh feeling. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "spring_garden",
                displayName = UiText.StringResource(Res.string.style_spring_garden),
                categories = setOf(CLOTHING, COSMETICS),
                thumbnail = Res.drawable.style_spring_garden,
                kontextPrompt = "Place the product on a light surface with fresh spring flowers including tulips and daffodils softly arranged nearby, bright cheerful natural daylight, fresh and vibrant mood. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.FEBRUARY, Month.APRIL)
            ),
            Style(
                id = "beach_vibes",
                displayName = UiText.StringResource(Res.string.style_beach_vibes),
                categories = setOf(CLOTHING, JEWELLERY, COSMETICS),
                thumbnail = Res.drawable.style_beach_vibes,
                kontextPrompt = "Place the product on light sandy beach surface with turquoise ocean blurred in the background, bright natural sunlight, summer holiday feeling. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.MAY, Month.AUGUST)
            ),
            Style(
                id = "paper_craft",
                displayName = UiText.StringResource(Res.string.style_paper_craft),
                categories = setOf(OTHER, HOMEWARE, BOOKS_STATIONERY),
                thumbnail = Res.drawable.style_paper_craft,
                kontextPrompt = "Place the product on a clean white surface surrounded by kraft paper, twine string, and small dried flowers, flat lay style from above, natural daylight. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "valentines_romance",
                displayName = UiText.StringResource(Res.string.style_valentines_romance),
                categories = setOf(JEWELLERY, COSMETICS, CLOTHING),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a soft rose-pink velvet surface with romantic candlelight, fresh red roses softly arranged nearby, scattered rose petals, warm intimate lighting, love letter and luxury ribbon in background. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.JANUARY, Month.FEBRUARY)
            ),
            Style(
                id = "autumn_harvest",
                displayName = UiText.StringResource(Res.string.style_autumn_harvest),
                categories = setOf(FOOD, HOMEWARE, CLOTHING),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a rich burgundy or burnt orange surface with fall foliage, dried leaves, and small pumpkins softly blurred nearby, warm golden hour light, cozy autumn harvest mood. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.AUGUST, Month.OCTOBER)
            ),
            Style(
                id = "halloween_spooky",
                displayName = UiText.StringResource(Res.string.style_halloween_spooky),
                categories = setOf(OTHER, TOYS_KIDS, CLOTHING),
                thumbnail = Res.drawable.style_placeholder,
                kontextPrompt = "Place the product on a dark moody surface with subtle Halloween elements—faint pumpkins, spider webs, melting candles—dramatic lighting with deep shadows and muted orange accent glow, playful spooky atmosphere. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.SEPTEMBER, Month.OCTOBER)
            ),
            Style(
                id = "christmas",
                displayName = UiText.StringResource(Res.string.style_christmas),
                categories = setOf(ALL),
                thumbnail = Res.drawable.style_christmas,
                kontextPrompt = "Place the product on a surface with subtle Christmas decoration, soft warm fairy lights bokeh in background, pine branches and red berries nearby, cozy holiday atmosphere. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.OCTOBER, Month.DECEMBER)
            )
        )
    }
}
