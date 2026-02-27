package com.middleton.studiosnap.core.domain.model

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(val resId: org.jetbrains.compose.resources.StringResource) : UiText
}