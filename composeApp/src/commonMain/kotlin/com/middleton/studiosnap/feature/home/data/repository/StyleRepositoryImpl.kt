package com.middleton.studiosnap.feature.home.data.repository

import com.middleton.studiosnap.feature.home.domain.model.SeasonalWindow
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory.*
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import kotlinx.datetime.Month

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
                nameKey = "style_clean_white",
                categories = setOf(ALL),
                thumbnailResName = "style_clean_white",
                kontextPrompt = "Place the product on a pure white seamless background with soft even studio lighting and a subtle ground shadow. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "warm_linen",
                nameKey = "style_warm_linen",
                categories = setOf(CLOTHING, JEWELLERY, HOMEWARE),
                thumbnailResName = "style_warm_linen",
                kontextPrompt = "Place the product on a natural cream linen fabric surface with warm soft window light coming from the left. Shallow depth of field background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "marble_luxe",
                nameKey = "style_marble_luxe",
                categories = setOf(JEWELLERY, COSMETICS),
                thumbnailResName = "style_marble_luxe",
                kontextPrompt = "Place the product on a white Carrara marble surface with soft overhead lighting and subtle gold accents in the blurred background. Elegant and minimal. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "morning_kitchen",
                nameKey = "style_morning_kitchen",
                categories = setOf(FOOD, HOMEWARE),
                thumbnailResName = "style_morning_kitchen",
                kontextPrompt = "Place the product on a light oak wooden kitchen counter with morning sunlight streaming through a window, soft shadows, green herbs and a coffee cup blurred in the background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "botanical_garden",
                nameKey = "style_botanical_garden",
                categories = setOf(CLOTHING, COSMETICS, JEWELLERY),
                thumbnailResName = "style_botanical_garden",
                kontextPrompt = "Place the product surrounded by lush green tropical leaves and small white flowers, natural dappled sunlight, organic and fresh feeling. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "concrete_minimal",
                nameKey = "style_concrete_minimal",
                categories = setOf(CLOTHING, HOMEWARE, OTHER),
                thumbnailResName = "style_concrete_minimal",
                kontextPrompt = "Place the product on a raw concrete surface with minimalist industrial background, directional warm spotlight from above, deep shadows. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "sunset_glow",
                nameKey = "style_sunset_glow",
                categories = setOf(CLOTHING, JEWELLERY),
                thumbnailResName = "style_sunset_glow",
                kontextPrompt = "Place the product bathed in warm golden hour sunset light with a soft bokeh background of warm orange and pink tones. Dreamy and romantic atmosphere. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "beach_vibes",
                nameKey = "style_beach_vibes",
                categories = setOf(CLOTHING, JEWELLERY, COSMETICS),
                thumbnailResName = "style_beach_vibes",
                kontextPrompt = "Place the product on light sandy beach surface with turquoise ocean blurred in the background, bright natural sunlight, summer holiday feeling. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.MAY, Month.AUGUST)
            ),
            Style(
                id = "dark_moody",
                nameKey = "style_dark_moody",
                categories = setOf(JEWELLERY, COSMETICS, FOOD),
                thumbnailResName = "style_dark_moody",
                kontextPrompt = "Place the product on a dark slate surface with dramatic low-key lighting from one side, deep rich shadows, luxurious dark background. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "pastel_dream",
                nameKey = "style_pastel_dream",
                categories = setOf(CLOTHING, COSMETICS, JEWELLERY),
                thumbnailResName = "style_pastel_dream",
                kontextPrompt = "Place the product on a soft pastel pink surface with a gradient background blending light pink to lavender, diffused even lighting, gentle and feminine mood. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "rustic_wood",
                nameKey = "style_rustic_wood",
                categories = setOf(FOOD, HOMEWARE, OTHER),
                thumbnailResName = "style_rustic_wood",
                kontextPrompt = "Place the product on a weathered dark reclaimed wood surface with warm ambient lighting, rustic farmhouse background slightly blurred. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "christmas",
                nameKey = "style_christmas",
                categories = setOf(ALL),
                thumbnailResName = "style_christmas",
                kontextPrompt = "Place the product on a surface with subtle Christmas decoration, soft warm fairy lights bokeh in background, pine branches and red berries nearby, cozy holiday atmosphere. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.OCTOBER, Month.DECEMBER)
            ),
            Style(
                id = "terrazzo",
                nameKey = "style_terrazzo",
                categories = setOf(HOMEWARE, COSMETICS, JEWELLERY),
                thumbnailResName = "style_terrazzo",
                kontextPrompt = "Place the product on a white terrazzo surface with colorful stone chips, bright even overhead lighting, modern design-forward aesthetic. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "silk_velvet",
                nameKey = "style_silk_velvet",
                categories = setOf(JEWELLERY, COSMETICS),
                thumbnailResName = "style_silk_velvet",
                kontextPrompt = "Place the product on draped deep burgundy velvet fabric with soft directional lighting highlighting the fabric folds, rich and opulent feeling. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "paper_craft",
                nameKey = "style_paper_craft",
                categories = setOf(OTHER, CLOTHING, HOMEWARE),
                thumbnailResName = "style_paper_craft",
                kontextPrompt = "Place the product on a clean white surface surrounded by kraft paper, twine string, and small dried flowers, flat lay style from above, natural daylight. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "spring_garden",
                nameKey = "style_spring_garden",
                categories = setOf(CLOTHING, COSMETICS, FOOD),
                thumbnailResName = "style_spring_garden",
                kontextPrompt = "Place the product on a light surface with fresh spring flowers including tulips and daffodils softly arranged nearby, bright cheerful natural daylight, fresh and vibrant mood. Keep the product exactly as-is, change only the background.",
                seasonal = SeasonalWindow(Month.FEBRUARY, Month.APRIL)
            ),
            Style(
                id = "gradient_studio",
                nameKey = "style_gradient_studio",
                categories = setOf(ALL),
                thumbnailResName = "style_gradient_studio",
                kontextPrompt = "Place the product on a reflective surface with a smooth gradient background transitioning from light grey to medium grey, professional product photography studio lighting. Keep the product exactly as-is, change only the background."
            ),
            Style(
                id = "neon_pop",
                nameKey = "style_neon_pop",
                categories = setOf(CLOTHING, OTHER),
                thumbnailResName = "style_neon_pop",
                kontextPrompt = "Place the product against a dark background with vibrant neon light accents in pink and blue, urban night aesthetic, reflective surface with neon color reflections. Keep the product exactly as-is, change only the background."
            )
        )
    }
}
