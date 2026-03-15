package com.middleton.studiosnap.feature.home.presentation

import androidx.compose.runtime.Composable
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.swatch_botanical
import studiosnap.composeapp.generated.resources.swatch_dark
import studiosnap.composeapp.generated.resources.swatch_marble
import studiosnap.composeapp.generated.resources.swatch_wood
import studiosnap.composeapp.generated.resources.category_all
import studiosnap.composeapp.generated.resources.category_clothing
import studiosnap.composeapp.generated.resources.category_cosmetics
import studiosnap.composeapp.generated.resources.category_food
import studiosnap.composeapp.generated.resources.category_homeware
import studiosnap.composeapp.generated.resources.category_jewellery
import studiosnap.composeapp.generated.resources.category_other
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
 * Resolves style nameKey (e.g. "style_clean_white") to a localised display name.
 * Shared across HomeScreen and StylePickerScreen.
 */
@Composable
internal fun resolveStyleName(nameKey: String): String {
    return when (nameKey) {
        "style_clean_white" -> stringResource(Res.string.style_clean_white)
        "style_warm_linen" -> stringResource(Res.string.style_warm_linen)
        "style_marble_luxe" -> stringResource(Res.string.style_marble_luxe)
        "style_morning_kitchen" -> stringResource(Res.string.style_morning_kitchen)
        "style_botanical_garden" -> stringResource(Res.string.style_botanical_garden)
        "style_concrete_minimal" -> stringResource(Res.string.style_concrete_minimal)
        "style_sunset_glow" -> stringResource(Res.string.style_sunset_glow)
        "style_beach_vibes" -> stringResource(Res.string.style_beach_vibes)
        "style_dark_moody" -> stringResource(Res.string.style_dark_moody)
        "style_pastel_dream" -> stringResource(Res.string.style_pastel_dream)
        "style_rustic_wood" -> stringResource(Res.string.style_rustic_wood)
        "style_christmas" -> stringResource(Res.string.style_christmas)
        "style_terrazzo" -> stringResource(Res.string.style_terrazzo)
        "style_silk_velvet" -> stringResource(Res.string.style_silk_velvet)
        "style_paper_craft" -> stringResource(Res.string.style_paper_craft)
        "style_spring_garden" -> stringResource(Res.string.style_spring_garden)
        "style_gradient_studio" -> stringResource(Res.string.style_gradient_studio)
        "style_neon_pop" -> stringResource(Res.string.style_neon_pop)
        else -> nameKey.removePrefix("style_").replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }
}

/**
 * Resolves a style's thumbnailResName to its DrawableResource.
 * Returns null if no matching drawable exists (fallback handled by caller).
 */
internal fun resolveStyleThumbnail(thumbnailResName: String): DrawableResource? {
    return when (thumbnailResName) {
        "swatch_botanical" -> Res.drawable.swatch_botanical
        "swatch_marble" -> Res.drawable.swatch_marble
        "swatch_wood" -> Res.drawable.swatch_wood
        "swatch_dark" -> Res.drawable.swatch_dark
        else -> null
    }
}

@Composable
internal fun categoryDisplayName(category: StyleCategory): String {
    return stringResource(
        when (category) {
            StyleCategory.ALL -> Res.string.category_all
            StyleCategory.CLOTHING -> Res.string.category_clothing
            StyleCategory.JEWELLERY -> Res.string.category_jewellery
            StyleCategory.HOMEWARE -> Res.string.category_homeware
            StyleCategory.COSMETICS -> Res.string.category_cosmetics
            StyleCategory.FOOD -> Res.string.category_food
            StyleCategory.OTHER -> Res.string.category_other
        }
    )
}
