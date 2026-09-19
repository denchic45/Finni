package com.hackathon.finni.core.ui.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import com.hackathon.finni.core.presentation.model.UiImage
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.allDrawableResources
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource


@OptIn(ExperimentalResourceApi::class)
@Composable
fun UiImage.rememberPainter(): Painter = when (this) {
    is UiImage.Resource -> {
        painterResource(this.res)
    }
    is UiImage.Named -> {
        val resource = remember(this.name) {
            Res.allDrawableResources.getValue(this.name)
        }
        painterResource(resource)
    }
}