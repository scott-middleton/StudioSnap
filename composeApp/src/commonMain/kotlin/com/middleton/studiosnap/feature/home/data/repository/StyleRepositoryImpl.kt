package com.middleton.studiosnap.feature.home.data.repository

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.feature.home.domain.model.SeasonalWindow
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory.*
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import kotlinx.datetime.Month
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.style_beach_vibes
import studiosnap.composeapp.generated.resources.style_botanical_garden
import studiosnap.composeapp.generated.resources.style_christmas
import studiosnap.composeapp.generated.resources.style_clean_white
import studiosnap.composeapp.generated.resources.style_concrete_minimal
import studiosnap.composeapp.generated.resources.style_dark_moody
import studiosnap.composeapp.generated.resources.style_gradient_studio
import studiosnap.composeapp.generated.resources.style_marble_luxe
import studiosnap.composeapp.generated.resources.style_morning_kitchen
import studiosnap.composeapp.generated.resources.style_neon_pop
import studiosnap.composeapp.generated.resources.style_paper_craft
import studiosnap.composeapp.generated.resources.style_pastel_dream
import studiosnap.composeapp.generated.resources.style_rustic_wood
import studiosnap.composeapp.generated.resources.style_silk_velvet
import studiosnap.composeapp.generated.resources.style_spring_garden
import studiosnap.composeapp.generated.resources.style_sunset_glow
import studiosnap.composeapp.generated.resources.style_terrazzo
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
            Style(
                id = "clean_white",
                displayName = UiText.StringResource(Res.string.style_clean_white),
                categories = setOf(ALL),
                thumbnail = Res.drawable.style_clean_white,
                kontextPrompt = "Place the product on a pure white seamless background with soft even studio lighting and a subtle ground shadow. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "warm_linen",
                displayName = UiText.StringResource(Res.string.style_warm_linen),
                categories = setOf(CLOTHING, JEWELLERY, HOMEWARE),
                thumbnail = Res.drawable.style_warm_linen,
                kontextPrompt = "Place the product on a natural cream linen fabric surface with warm soft window light coming from the left. Shallow depth of field background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "marble_luxe",
                displayName = UiText.StringResource(Res.string.style_marble_luxe),
                categories = setOf(JEWELLERY, COSMETICS),
                thumbnail = Res.drawable.style_marble_luxe,
                kontextPrompt = "Place the product on a white Carrara marble surface with soft overhead lighting and subtle gold accents in the blurred background. Elegant and minimal. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "morning_kitchen",
                displayName = UiText.StringResource(Res.string.style_morning_kitchen),
                categories = setOf(FOOD, HOMEWARE),
                thumbnail = Res.drawable.style_morning_kitchen,
                kontextPrompt = "Place the product on a light oak wooden kitchen counter with morning sunlight streaming through a window, soft shadows, green herbs and a coffee cup blurred in the background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "botanical_garden",
                displayName = UiText.StringResource(Res.string.style_botanical_garden),
                categories = setOf(CLOTHING, COSMETICS, JEWELLERY),
                thumbnail = Res.drawable.style_botanical_garden,
                kontextPrompt = "Place the product surrounded by lush green tropical leaves and small white flowers, natural dappled sunlight, organic and fresh feeling. Keep the product exactly as-is, change only the background."
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
                id = "beach_vibes",
                displayName = UiText.StringResource(Res.string.style_beach_vibes),
                categories = setOf(CLOTHING, JEWELLERY, COSMETICS),
                thumbnail = Res.drawable.style_beach_vibes,
                kontextPrompt = "Place the product on light sandy beach surface with turquoise ocean blurred in the background, bright natural sunlight, summer holiday feeling. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.MAY, Month.AUGUST)
            ),
            Style(
                id = "dark_moody",
                displayName = UiText.StringResource(Res.string.style_dark_moody),
                categories = setOf(JEWELLERY, COSMETICS, FOOD),
                thumbnail = Res.drawable.style_dark_moody,
                kontextPrompt = "Place the product on a dark slate surface with dramatic low-key lighting from one side, deep rich shadows, luxurious dark background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "pastel_dream",
                displayName = UiText.StringResource(Res.string.style_pastel_dream),
                categories = setOf(CLOTHING, COSMETICS, JEWELLERY),
                thumbnail = Res.drawable.style_pastel_dream,
                kontextPrompt = "Place the product on a soft pastel pink surface with a gradient background blending light pink to lavender, diffused even lighting, gentle and feminine mood. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "rustic_wood",
                displayName = UiText.StringResource(Res.string.style_rustic_wood),
                categories = setOf(FOOD, HOMEWARE, OTHER),
                thumbnail = Res.drawable.style_rustic_wood,
                kontextPrompt = "Place the product on a weathered dark reclaimed wood surface with warm ambient lighting, rustic farmhouse background slightly blurred. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "christmas",
                displayName = UiText.StringResource(Res.string.style_christmas),
                categories = setOf(ALL),
                thumbnail = Res.drawable.style_christmas,
                kontextPrompt = "Place the product on a surface with subtle Christmas decoration, soft warm fairy lights bokeh in background, pine branches and red berries nearby, cozy holiday atmosphere. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.OCTOBER, Month.DECEMBER)
            ),
            Style(
                id = "terrazzo",
                displayName = UiText.StringResource(Res.string.style_terrazzo),
                categories = setOf(HOMEWARE, COSMETICS, JEWELLERY),
                thumbnail = Res.drawable.style_terrazzo,
                kontextPrompt = "Place the product on a white terrazzo surface with colorful stone chips, bright even overhead lighting, modern design-forward aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "silk_velvet",
                displayName = UiText.StringResource(Res.string.style_silk_velvet),
                categories = setOf(JEWELLERY, COSMETICS),
                thumbnail = Res.drawable.style_silk_velvet,
                kontextPrompt = "Place the product on draped deep burgundy velvet fabric with soft directional lighting highlighting the fabric folds, rich and opulent feeling. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "paper_craft",
                displayName = UiText.StringResource(Res.string.style_paper_craft),
                categories = setOf(OTHER, CLOTHING, HOMEWARE),
                thumbnail = Res.drawable.style_paper_craft,
                kontextPrompt = "Place the product on a clean white surface surrounded by kraft paper, twine string, and small dried flowers, flat lay style from above, natural daylight. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "spring_garden",
                displayName = UiText.StringResource(Res.string.style_spring_garden),
                categories = setOf(CLOTHING, COSMETICS, FOOD),
                thumbnail = Res.drawable.style_spring_garden,
                kontextPrompt = "Place the product on a light surface with fresh spring flowers including tulips and daffodils softly arranged nearby, bright cheerful natural daylight, fresh and vibrant mood. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.FEBRUARY, Month.APRIL)
            ),
            Style(
                id = "gradient_studio",
                displayName = UiText.StringResource(Res.string.style_gradient_studio),
                categories = setOf(ALL),
                thumbnail = Res.drawable.style_gradient_studio,
                kontextPrompt = "Place the product on a reflective surface with a smooth gradient background transitioning from light grey to medium grey, professional product photography studio lighting. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "neon_pop",
                displayName = UiText.StringResource(Res.string.style_neon_pop),
                categories = setOf(CLOTHING, OTHER),
                thumbnail = Res.drawable.style_neon_pop,
                kontextPrompt = "Place the product against a dark background with vibrant neon light accents in pink and blue, urban night aesthetic, reflective surface with neon color reflections. Keep the product exactly as-is, change only the background."
            )
        )
    }
}
