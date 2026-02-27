package com.middleton.studiosnap.core.presentation.util

import androidx.compose.runtime.Composable
import com.middleton.studiosnap.core.domain.model.UiText
import org.jetbrains.compose.resources.stringResource

/**
 * Extension function to convert UiText to String in Composable context.
 * Allows seamless usage of UiText in presentation layer.
 */
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> stringResource(resId)
}