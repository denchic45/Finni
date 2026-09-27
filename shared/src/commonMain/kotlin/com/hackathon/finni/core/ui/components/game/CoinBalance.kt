package com.hackathon.finni.core.ui.components.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hackathon.finni.core.ui.components.CoinBalance as CoreCoinBalance

/**
 * Делегат к [com.hackathon.finni.core.ui.components.CoinBalance]
 * для доступности в пакете компонентов игры.
 */
@Composable
fun CoinBalance(
    coins: Int,
    modifier: Modifier = Modifier,
    height: Dp = 46.dp,
    onClick: (() -> Unit)? = null
) {
    CoreCoinBalance(
        coins = coins,
        modifier = modifier,
        height = height,
        onClick = onClick
    )
}
