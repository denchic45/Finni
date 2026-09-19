package com.hackathon.finni.features.noteeditor

import androidx.compose.ui.text.input.TextFieldValue
import com.hackathon.finni.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TextSnapshot(
    val title: TextFieldValue,
    val content: TextFieldValue
)

class NoteHistoryManager(
    private val scope: ApplicationScope,
    private val maxHistorySize: Int = 50,
    private val debounceMs: Long = 1000L
) {
    private val undoStack = mutableListOf<TextSnapshot>()
    private val redoStack = mutableListOf<TextSnapshot>()

    private var lastSavedSnapshot: TextSnapshot? = null
    private var debounceJob: Job? = null

    // Пунктуация, после которой сохраняем состояние немедленно
    private val forcePushChars = setOf(' ', '\n', '.', ',', '!', '?', ';', ':')

    fun recordChange(title: TextFieldValue, content: TextFieldValue, force: Boolean = false) {
        val newSnapshot = TextSnapshot(title, content)

        // Если текст не изменился (только курсор), не сохраняем в историю
        if (newSnapshot.title.text == lastSavedSnapshot?.title?.text &&
            newSnapshot.content.text == lastSavedSnapshot?.content?.text
        ) {
            return
        }

        debounceJob?.cancel()

        if (force || shouldPushImmediately(lastSavedSnapshot, newSnapshot)) {
            pushToUndo(newSnapshot)
        } else {
            debounceJob = scope.launch {
                delay(debounceMs)
                pushToUndo(newSnapshot)
            }
        }
    }

    private fun shouldPushImmediately(old: TextSnapshot?, new: TextSnapshot): Boolean {
        if (old == null) return true

        // Если удалили много текста сразу
        if (old.content.text.length - new.content.text.length > 5) return true

        // Если ввели спецсимвол (конец слова или предложения)
        val lastChar = new.content.text.lastOrNull()
        if (lastChar in forcePushChars) return true

        val lastTitleChar = new.title.text.lastOrNull()
        if (lastTitleChar in forcePushChars) return true

        return false
    }

    private fun pushToUndo(snapshot: TextSnapshot) {
        if (undoStack.lastOrNull() == snapshot) return

        undoStack.add(snapshot)
        if (undoStack.size > maxHistorySize) undoStack.removeAt(0)

        lastSavedSnapshot = snapshot
        redoStack.clear() // При новом изменении ветка Redo инвалидируется
    }

    fun undo(currentTitle: TextFieldValue, currentContent: TextFieldValue): TextSnapshot? {
        if (undoStack.isEmpty()) return null

        // Перед отменой сохраняем текущее состояние в Redo
        redoStack.add(TextSnapshot(currentTitle, currentContent))

        val snapshot = undoStack.removeAt(undoStack.lastIndex)
        lastSavedSnapshot = snapshot
        return snapshot
    }

    fun redo(currentTitle: TextFieldValue, currentContent: TextFieldValue): TextSnapshot? {
        if (redoStack.isEmpty()) return null

        undoStack.add(TextSnapshot(currentTitle, currentContent))

        val snapshot = redoStack.removeAt(redoStack.lastIndex)
        lastSavedSnapshot = snapshot
        return snapshot
    }
}
