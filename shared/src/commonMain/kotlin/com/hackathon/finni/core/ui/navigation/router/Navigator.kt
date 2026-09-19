package com.hackathon.finni.core.ui.navigation.router

interface Navigator {
     fun navigate(transformer: List<Destination>.() -> List<Destination>): Boolean
}
