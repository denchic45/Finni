package com.hackathon.finni.core.ui.navigation.router

import kotlinx.serialization.Serializable

sealed interface NavigationResult

@Serializable
data class Confirmed(val confirmed: Boolean) : NavigationResult

@Serializable
data class LatestPhotoIndex(val index: Int) : NavigationResult

