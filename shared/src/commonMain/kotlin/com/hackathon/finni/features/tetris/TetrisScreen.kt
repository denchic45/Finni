package com.hackathon.finni.features.tetris

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hackathon.finni.core.ui.components.game.GameButton
import com.hackathon.finni.core.ui.components.game.GameButtonSize
import com.hackathon.finni.core.ui.components.game.GameButtonStyle
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TetrisScreen(viewModel: TetrisViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.game.status) {
        while (state.game.status == TetrisStatus.PLAYING) {
            delay(650)
            viewModel.onTick()
        }
    }
    Surface(Modifier.fillMaxSize(), color = Color(0xFFF7F5EE)) {
        if (!state.unlocked) {
            LockedTetris(onBack = viewModel::onBack)
        } else {
            TetrisContent(
                state = state,
                onBack = viewModel::onBack,
                onMoveLeft = viewModel::onMoveLeft,
                onMoveRight = viewModel::onMoveRight,
                onRotate = viewModel::onRotate,
                onDrop = viewModel::onHardDrop,
                onPause = viewModel::onPauseToggle,
                onNewGame = viewModel::onNewGame
            )
        }
    }
}

@Composable
private fun LockedTetris(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ТЕТРИС ЕЩЁ ЗАКРЫТ", style = MaterialTheme.typography.headlineSmall)
        Text("Пройди уровень 10, чтобы открыть мини-игру.", modifier = Modifier.padding(vertical = 16.dp))
        GameButton(onClick = onBack, text = "НА ГЛАВНЫЙ ЭКРАН", style = GameButtonStyle.Wood)
    }
}

@Composable
private fun TetrisContent(
    state: TetrisUiState,
    onBack: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onRotate: () -> Unit,
    onDrop: () -> Unit,
    onPause: () -> Unit,
    onNewGame: () -> Unit
) {
    val playing = state.game.status == TetrisStatus.PLAYING
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("ТЕТРИС", style = MaterialTheme.typography.headlineMedium)
        }
        Text("Линии: ${state.game.lines}/$TETRIS_TARGET_LINES   Очки: ${state.game.score}")
        TetrisBoard(state.game)
        when (state.game.status) {
            TetrisStatus.WON -> Text("ПОБЕДА! +3 МОНЕТЫ 🪙", style = MaterialTheme.typography.titleLarge, color = Color(0xFF238636))
            TetrisStatus.LOST -> Text("ИГРА ОКОНЧЕНА", style = MaterialTheme.typography.titleLarge, color = Color(0xFFC62828))
            TetrisStatus.PAUSED -> Text("ПАУЗА", style = MaterialTheme.typography.titleLarge)
            TetrisStatus.PLAYING -> Unit
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TetrisControl("←", onMoveLeft, playing)
            TetrisControl("↻", onRotate, playing)
            TetrisControl("→", onMoveRight, playing)
        }
        GameButton(
            onClick = onDrop,
            text = "↓  БЫСТРО ОПУСТИТЬ",
            style = GameButtonStyle.Primary,
            enabled = playing,
            modifier = Modifier.width(250.dp)
        )
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            GameButton(onClick = onBack, text = "ВЫЙТИ", style = GameButtonStyle.Wood, size = GameButtonSize.Medium)
            if (!playing) {
                GameButton(onClick = onNewGame, text = "НОВАЯ ИГРА", style = GameButtonStyle.Success, size = GameButtonSize.Medium)
            } else {
                GameButton(onClick = onPause, text = "ПАУЗА", style = GameButtonStyle.Purple, size = GameButtonSize.Medium)
            }
        }
    }
}

@Composable
private fun TetrisControl(text: String, onClick: () -> Unit, enabled: Boolean) {
    GameButton(onClick = onClick, text = text, style = GameButtonStyle.Primary, size = GameButtonSize.Medium, enabled = enabled, modifier = Modifier.width(58.dp))
}

@Composable
private fun TetrisBoard(game: TetrisGame) {
    val cells = game.board.toMutableList()
    game.active.cells().forEach { cell ->
        if (cell.x in 0 until TETRIS_WIDTH && cell.y in 0 until TETRIS_HEIGHT) cells[cell.y * TETRIS_WIDTH + cell.x] = game.active.type.color
    }
    Column(Modifier.border(3.dp, Color(0xFF3D2B1F)).background(Color(0xFF172234)).padding(2.dp)) {
        repeat(TETRIS_HEIGHT) { y ->
            Row {
                repeat(TETRIS_WIDTH) { x ->
                    Box(
                        Modifier.size(17.dp).padding(0.5.dp)
                            .background(tetrisColor(cells[y * TETRIS_WIDTH + x]))
                    )
                }
            }
        }
    }
}

private fun tetrisColor(value: Int): Color = when (value) {
    1 -> Color(0xFF37C8F5)
    2 -> Color(0xFFF7D154)
    3 -> Color(0xFFB47AE8)
    4 -> Color(0xFFFF9B4A)
    5 -> Color(0xFF4D7FE8)
    6 -> Color(0xFF52C878)
    7 -> Color(0xFFE65B64)
    else -> Color(0xFF26354A)
}
