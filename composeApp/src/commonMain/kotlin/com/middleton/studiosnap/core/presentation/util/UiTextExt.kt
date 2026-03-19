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

/**
 * Non-composable conversion for use in ViewModels.
 * StringResource variants return an empty string — only use where the value
 * is guaranteed to be DynamicString (e.g. strings persisted from the database).
 */
fun UiText.asDisplayString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> ""
}