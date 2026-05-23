package com.middleton.studiosnap.feature.home.presentation

import androidx.compose.runtime.Composable
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import org.jetbrains.compose.resources.stringResource
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.category_all
import studiosnap.composeapp.generated.resources.category_books
import studiosnap.composeapp.generated.resources.category_clothing
import studiosnap.composeapp.generated.resources.category_cosmetics
import studiosnap.composeapp.generated.resources.category_electronics
import studiosnap.composeapp.generated.resources.category_food
import studiosnap.composeapp.generated.resources.category_homeware
import studiosnap.composeapp.generated.resources.category_jewellery
import studiosnap.composeapp.generated.resources.category_other
import studiosnap.composeapp.generated.resources.category_pets
import studiosnap.composeapp.generated.resources.category_sports
import studiosnap.composeapp.generated.resources.category_toys

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
            StyleCategory.ELECTRONICS -> Res.string.category_electronics
            StyleCategory.PETS -> Res.string.category_pets
            StyleCategory.TOYS_KIDS -> Res.string.category_toys
            StyleCategory.BOOKS_STATIONERY -> Res.string.category_books
            StyleCategory.SPORTS_FITNESS -> Res.string.category_sports
            StyleCategory.OTHER -> Res.string.category_other
        }
    )
}
