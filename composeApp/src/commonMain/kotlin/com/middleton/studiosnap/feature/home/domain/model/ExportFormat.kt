package com.middleton.studiosnap.feature.home.domain.model

/**
 * Marketplace-optimised export aspect ratios.
 * [apiValue] is the value passed to the Flux Kontext `aspect_ratio` parameter.
 */
enum class ExportFormat(val apiValue: String) {
    ORIGINAL("match_input_image"),
    ETSY_SQUARE("1:1"),
    EBAY_SQUARE("1:1"),
    VINTED_PORTRAIT("4:5");

    companion object {
        val DEFAULT = ORIGINAL
    }
}
