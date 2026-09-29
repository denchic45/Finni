package com.hackathon.finni.features.tetris

import kotlin.random.Random

const val TETRIS_WIDTH = 10
const val TETRIS_HEIGHT = 20
const val TETRIS_TARGET_LINES = 10

data class TetrisCell(val x: Int, val y: Int)

enum class Tetromino(val color: Int, val rotations: List<List<TetrisCell>>) {
    I(1, listOf(listOf(TetrisCell(0, 1), TetrisCell(1, 1), TetrisCell(2, 1), TetrisCell(3, 1)), listOf(TetrisCell(2, 0), TetrisCell(2, 1), TetrisCell(2, 2), TetrisCell(2, 3)))),
    O(2, listOf(listOf(TetrisCell(1, 0), TetrisCell(2, 0), TetrisCell(1, 1), TetrisCell(2, 1)))),
    T(3, listOf(listOf(TetrisCell(1, 0), TetrisCell(0, 1), TetrisCell(1, 1), TetrisCell(2, 1)), listOf(TetrisCell(1, 0), TetrisCell(1, 1), TetrisCell(2, 1), TetrisCell(1, 2)), listOf(TetrisCell(0, 1), TetrisCell(1, 1), TetrisCell(2, 1), TetrisCell(1, 2)), listOf(TetrisCell(1, 0), TetrisCell(0, 1), TetrisCell(1, 1), TetrisCell(1, 2)))),
    L(4, listOf(listOf(TetrisCell(0, 0), TetrisCell(0, 1), TetrisCell(1, 1), TetrisCell(2, 1)), listOf(TetrisCell(1, 0), TetrisCell(2, 0), TetrisCell(1, 1), TetrisCell(1, 2)), listOf(TetrisCell(0, 1), TetrisCell(1, 1), TetrisCell(2, 1), TetrisCell(2, 2)), listOf(TetrisCell(1, 0), TetrisCell(1, 1), TetrisCell(0, 2), TetrisCell(1, 2)))),
    J(5, listOf(listOf(TetrisCell(2, 0), TetrisCell(0, 1), TetrisCell(1, 1), TetrisCell(2, 1)), listOf(TetrisCell(1, 0), TetrisCell(1, 1), TetrisCell(1, 2), TetrisCell(2, 2)), listOf(TetrisCell(0, 1), TetrisCell(1, 1), TetrisCell(2, 1), TetrisCell(0, 2)), listOf(TetrisCell(0, 0), TetrisCell(1, 0), TetrisCell(1, 1), TetrisCell(1, 2)))),
    S(6, listOf(listOf(TetrisCell(1, 0), TetrisCell(2, 0), TetrisCell(0, 1), TetrisCell(1, 1)), listOf(TetrisCell(1, 0), TetrisCell(1, 1), TetrisCell(2, 1), TetrisCell(2, 2)))),
    Z(7, listOf(listOf(TetrisCell(0, 0), TetrisCell(1, 0), TetrisCell(1, 1), TetrisCell(2, 1)), listOf(TetrisCell(2, 0), TetrisCell(1, 1), TetrisCell(2, 1), TetrisCell(1, 2))));
}

data class ActiveTetromino(val type: Tetromino, val rotation: Int = 0, val x: Int = 3, val y: Int = 0) {
    fun cells(): List<TetrisCell> = type.rotations[rotation].map { TetrisCell(x + it.x, y + it.y) }
}

enum class TetrisStatus { PLAYING, PAUSED, WON, LOST }

data class TetrisGame(
    val board: List<Int> = List(TETRIS_WIDTH * TETRIS_HEIGHT) { 0 },
    val active: ActiveTetromino = ActiveTetromino(Tetromino.T),
    val lines: Int = 0,
    val score: Int = 0,
    val status: TetrisStatus = TetrisStatus.PLAYING
)

object TetrisEngine {
    fun newGame(type: Tetromino = randomTetromino()): TetrisGame = TetrisGame(active = ActiveTetromino(type))

    fun tick(game: TetrisGame): TetrisGame = moveDown(game)

    fun moveLeft(game: TetrisGame): TetrisGame = move(game, -1, 0)

    fun moveRight(game: TetrisGame): TetrisGame = move(game, 1, 0)

    fun rotate(game: TetrisGame): TetrisGame {
        if (game.status != TetrisStatus.PLAYING) return game
        val rotations = game.active.type.rotations.size
        val rotated = game.active.copy(rotation = (game.active.rotation + 1) % rotations)
        return if (canPlace(game.board, rotated)) game.copy(active = rotated) else game
    }

    fun hardDrop(game: TetrisGame): TetrisGame {
        if (game.status != TetrisStatus.PLAYING) return game
        var dropped = game
        while (canPlace(dropped.board, dropped.active.copy(y = dropped.active.y + 1))) {
            dropped = dropped.copy(active = dropped.active.copy(y = dropped.active.y + 1))
        }
        return lock(dropped)
    }

    fun moveDown(game: TetrisGame): TetrisGame {
        if (game.status != TetrisStatus.PLAYING) return game
        val lower = game.active.copy(y = game.active.y + 1)
        return if (canPlace(game.board, lower)) game.copy(active = lower) else lock(game)
    }

    fun canPlace(board: List<Int>, piece: ActiveTetromino): Boolean = piece.cells().all { cell ->
        cell.x in 0 until TETRIS_WIDTH && cell.y in 0 until TETRIS_HEIGHT && board[cell.y * TETRIS_WIDTH + cell.x] == 0
    }

    private fun move(game: TetrisGame, dx: Int, dy: Int): TetrisGame {
        if (game.status != TetrisStatus.PLAYING) return game
        val moved = game.active.copy(x = game.active.x + dx, y = game.active.y + dy)
        return if (canPlace(game.board, moved)) game.copy(active = moved) else game
    }

    private fun lock(game: TetrisGame): TetrisGame {
        val board = game.board.toMutableList()
        game.active.cells().forEach { cell -> board[cell.y * TETRIS_WIDTH + cell.x] = game.active.type.color }
        val rows = board.chunked(TETRIS_WIDTH)
        val keptRows = rows.filter { row -> row.any { it == 0 } }
        val cleared = TETRIS_HEIGHT - keptRows.size
        val clearedBoard = List(cleared) { List(TETRIS_WIDTH) { 0 } }.flatten() + keptRows.flatten()
        val lines = game.lines + cleared
        val score = game.score + when (cleared) { 1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0 }
        if (lines >= TETRIS_TARGET_LINES) return game.copy(board = clearedBoard, lines = lines, score = score, status = TetrisStatus.WON)

        val next = ActiveTetromino(randomTetromino())
        return if (canPlace(clearedBoard, next)) game.copy(board = clearedBoard, active = next, lines = lines, score = score) else
            game.copy(board = clearedBoard, lines = lines, score = score, status = TetrisStatus.LOST)
    }

    private fun randomTetromino(): Tetromino = Tetromino.entries[Random.nextInt(Tetromino.entries.size)]
}
