package com.capystick.notepad.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class Dynamic(val value: String) : UiText()
    data class Resource(@param: StringRes val resId: Int, val args: List<Any> = emptyList()) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is Dynamic -> value
            is Resource -> stringResource(resId, *args.toTypedArray())
        }
    }
}
