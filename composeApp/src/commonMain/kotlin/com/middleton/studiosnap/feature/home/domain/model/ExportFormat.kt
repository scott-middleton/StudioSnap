package com.middleton.studiosnap.feature.home.domain.model

/**
 * Marketplace-optimised export aspect ratios.
 * [apiValue] is the value passed to the Flux Kontext `aspect_ratio` parameter.
 */
enum class ExportFormat(val apiValue: String) {
    ETSY_SQUARE("1:1"),
    EBAY_LANDSCAPE("16:9"),
    VINTED_PORTRAIT("4:5"),
    ORIGINAL("match_input_image");

    companion object {
        val DEFAULT = ORIGINAL
    }
}
