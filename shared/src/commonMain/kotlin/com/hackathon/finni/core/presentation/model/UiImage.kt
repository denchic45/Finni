package com.hackathon.finni.core.presentation.model

import org.jetbrains.compose.resources.DrawableResource

sealed interface UiImage {

    data class Resource(val res: DrawableResource) : UiImage

    data class Named(val name: String) : UiImage
}