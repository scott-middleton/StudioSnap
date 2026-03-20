package com.middleton.studiosnap.core.domain.model

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StringResource(
        val resId: org.jetbrains.compose.resources.StringResource,
        val args: Array<Any> = emptyArray()
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false
            return resId == other.resId && args.contentEquals(other.args)
        }

        override fun hashCode(): Int = 31 * resId.hashCode() + args.contentHashCode()
    }
}