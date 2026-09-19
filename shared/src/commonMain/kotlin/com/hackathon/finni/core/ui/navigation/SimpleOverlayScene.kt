package com.hackathon.finni.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope


internal class SimpleOverlayScene<T : Any>(
    override val key: Any,
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        entry.Content()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SimpleOverlayScene<*>

        return key == other.key &&
                previousEntries == other.previousEntries &&
                overlaidEntries == other.overlaidEntries &&
                entry == other.entry
    }

    override fun hashCode(): Int {
        return key.hashCode() * 31 +
                previousEntries.hashCode() * 31 +
                overlaidEntries.hashCode() * 31 +
                entry.hashCode() * 31
    }

    override fun toString(): String {
        return "OverlayScene(key=$key, entry=$entry, previousEntries=$previousEntries, overlaidEntries=$overlaidEntries)"
    }
}


class SimpleOverlaySceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(
        entries: List<NavEntry<T>>
    ): Scene<T>? {
        val lastEntry = entries.lastOrNull()

        return if (lastEntry?.metadata?.containsKey(OVERLAY_KEY) == true)
         SimpleOverlayScene(
                key = lastEntry.contentKey,
                entry = lastEntry,
                previousEntries = entries.dropLast(1),
                overlaidEntries = entries.dropLast(1)
            )
        else null
    }

    companion object {

        fun overlay(): Map<String, Any> = mapOf(OVERLAY_KEY to Unit)

        internal const val OVERLAY_KEY = "overlay"
    }
}
