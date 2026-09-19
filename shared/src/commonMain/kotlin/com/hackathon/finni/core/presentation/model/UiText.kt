package com.hackathon.finni.core.presentation.model

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

sealed interface UiText {

    data class Dynamic(val value: String) : UiText

    data class Resource(
        val res: StringResource,
        val args: List<Any> = emptyList()
    ) : UiText {
        constructor(
            res: StringResource,
            vararg formatArgs: Any
        ) : this(
            res = res,
            args = formatArgs.toList()
        )
    }

    data class Plural(
        val res: PluralStringResource,
        val quantity: Int,
        val args: List<Any> = emptyList()
    ) : UiText {
        constructor(
            res: PluralStringResource,
            quantity: Int,
            vararg formatArgs: Any
        ) : this(
            res = res,
            quantity = quantity,
            args = formatArgs.toList()
        )
    }
}
