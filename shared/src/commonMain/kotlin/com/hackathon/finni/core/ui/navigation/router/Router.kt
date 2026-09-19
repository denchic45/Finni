package com.hackathon.finni.core.ui.navigation.router

import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


class Router(
    initialTab: TopLevelRoute,
    initialStack: List<Destination> = listOf(initialTab)
) : Navigator, TabNavigator {

    val resultChannels = ConcurrentMap<Any, Channel<NavigationResult>>()

    fun getChannelFor(destination: Destination): Channel<NavigationResult> {
        return resultChannels.getOrPut(destination) {
            Channel(Channel.UNLIMITED)
        }
    }

    fun sendResult(
        result: NavigationResult,
        to: Destination? = state.value.currentStack.lastOrNull()
    ): Boolean {
        return if (to != null) {
            getChannelFor(to).trySend(result).isSuccess
        } else false
    }


    fun popWithResult(result: NavigationResult) {
        val currentDestination = state.value.currentStack.lastOrNull()
        if (currentDestination != null) {
            getChannelFor(currentDestination).trySend(result)
        }
        pop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend inline fun <reified T : NavigationResult> receiveResult(
        from: Destination = state.value.currentStack.last()
    ): T? = channelFlow {
        val privateChannel = getChannelFor(from)

        val job = launch {
            state.first { navState -> navState.flattenedBackStack.none { it === from } }
            kotlinx.coroutines.yield()
            // Чистим за собой карту, когда экран стерт из стека
            resultChannels.remove(from)
            close()
        }

        privateChannel.consumeAsFlow()
            .filterIsInstance<T>()
            .collect { result ->
                send(result)
                job.cancel()
                resultChannels.remove(from)
                close()
            }
    }.firstOrNull()

    private val _state: MutableStateFlow<NavigationState<Destination>> = MutableStateFlow(
        NavigationState(
            tabStacks = linkedMapOf(initialTab::class to initialStack),
            currentTab = initialTab::class
        )
    )
    val state = _state.asStateFlow()

    // Для событий скролла (когда жмем на активную вкладку)
    private val _scrollToTopEvent = Channel<KClass<out TopLevelRoute>>(Channel.BUFFERED)
    val scrollToTopEvent = _scrollToTopEvent.receiveAsFlow()


    fun switchTab(tab: TopLevelRoute) {
        val tabKlass = tab::class
        val currentState = _state.value
        if (currentState.currentTab == tabKlass) {
            _scrollToTopEvent.trySend(tabKlass)
            return
        }

        updateState {
            val stack = tabStacks[tabKlass] ?: listOf(tab)
            val newTabStacks = LinkedHashMap(tabStacks)
            newTabStacks.remove(tabKlass)
            newTabStacks[tabKlass] = stack // Bring to front
            copy(tabStacks = newTabStacks, currentTab = tabKlass)
        }
    }

    fun updateTabs(
        transformer: Map<KClass<out TopLevelRoute>, List<Destination>>.() -> Map<KClass<out TopLevelRoute>, List<Destination>>
    ) {
        updateState {
            copy(tabStacks = transformer(tabStacks))
        }
    }

    override fun navigate(transformer: List<Destination>.() -> List<Destination>): Boolean {
        return performNavigate(_state.value.currentTab, transformer)
    }

    override fun navigate(
        tabKlass: KClass<out TopLevelRoute>,
        transformer: List<Destination>.() -> List<Destination>
    ): Boolean = performNavigate(tabKlass, transformer)

    private fun performNavigate(
        tabKlass: KClass<out TopLevelRoute>,
        transformer: List<Destination>.() -> List<Destination>
    ): Boolean {
        val oldState = _state.value
        updateState {
            val newStack = transformer(tabStacks[tabKlass] ?: listOf())
            if (newStack.isNotEmpty()) {
                val newTabStacks = LinkedHashMap(tabStacks)
                newTabStacks.remove(tabKlass)
                newTabStacks[tabKlass] = newStack
                copy(tabStacks = newTabStacks, currentTab = tabKlass)
            } else {
                if (tabStacks.size > 1) {
                    val newStacks = LinkedHashMap(tabStacks)
                    newStacks.remove(tabKlass)
                    copy(
                        tabStacks = newStacks,
                        currentTab = newStacks.keys.last()
                    )
                } else this
            }
        }
        return oldState != _state.value
    }

    val NavigationState<Destination>.currentStack: List<Destination>
        get() = tabStacks.getValue(currentTab)


    private inline fun updateState(block: NavigationState<Destination>.() -> NavigationState<Destination>) {
        _state.update { it.block() }
    }
}