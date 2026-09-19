package com.hackathon.finni.core.ui.navigation.router

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource


interface Destination : NavKey

interface NavDestination : Destination

interface Modal : Destination

@Immutable
interface TopLevelRoute : NavDestination