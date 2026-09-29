package com.hackathon.finni.features.tetris

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TetrisGameTest {
    @Test
    fun pieceDoesNotMovePastLeftWall() {
        val game = TetrisEngine.newGame(Tetromino.O).copy(active = ActiveTetromino(Tetromino.O, x = -1))
        assertEquals(game, TetrisEngine.moveLeft(game))
    }

    @Test
    fun clearingTenthLineWinsTheGame() {
        val board = MutableList(TETRIS_WIDTH * TETRIS_HEIGHT) { 0 }
        repeat(6) { x -> board[(TETRIS_HEIGHT - 1) * TETRIS_WIDTH + x] = 1 }
        val game = TetrisGame(
            board = board,
            active = ActiveTetromino(Tetromino.I, x = 6, y = TETRIS_HEIGHT - 2),
            lines = TETRIS_TARGET_LINES - 1
        )

        val result = TetrisEngine.moveDown(game)

        assertEquals(TetrisStatus.WON, result.status)
        assertEquals(TETRIS_TARGET_LINES, result.lines)
        assertTrue(result.board.take(TETRIS_WIDTH).all { it == 0 })
    }
}
