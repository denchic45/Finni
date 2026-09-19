package com.hackathon.finni.core.ui.extension

import androidx.compose.runtime.Composable
import com.hackathon.finni.core.presentation.model.UiText
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

suspend fun UiText.getString(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> getString(res, *args.toTypedArray())
    is UiText.Plural -> getPluralString(res, quantity, *args.toTypedArray())
}

@Composable
fun UiText.getStringResource(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> stringResource(res, *args.toTypedArray())
    is UiText.Plural -> pluralStringResource(res, quantity, *args.toTypedArray())
}