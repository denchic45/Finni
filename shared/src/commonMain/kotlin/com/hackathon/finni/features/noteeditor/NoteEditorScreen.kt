package com.hackathon.finni.features.noteeditor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.core.util.formatDateTimeCompose
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_add
import com.hackathon.finni.resources.ic_arrow_back
import com.hackathon.finni.resources.ic_close
import com.hackathon.finni.resources.ic_folder
import com.hackathon.finni.resources.ic_format_bold
import com.hackathon.finni.resources.ic_format_clear
import com.hackathon.finni.resources.ic_format_h1
import com.hackathon.finni.resources.ic_format_italic
import com.hackathon.finni.resources.ic_format_strikethrough
import com.hackathon.finni.resources.ic_format_underline
import com.hackathon.finni.resources.ic_match_case
import com.hackathon.finni.resources.ic_more
import com.hackathon.finni.resources.ic_pin
import com.hackathon.finni.resources.ic_redo
import com.hackathon.finni.resources.ic_reminder
import com.hackathon.finni.resources.ic_style
import com.hackathon.finni.resources.ic_text_format
import com.hackathon.finni.resources.ic_undo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    noteId: NoteId?,
    viewModel: NoteEditorViewModel = koinViewModel<NoteEditorViewModel>(
        parameters = { parametersOf(noteId) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val draft = uiState.draft

    var isContentFocused by remember { mutableStateOf(false) }
    var isFormattingExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = viewModel::onBack) {
                        Icon(
                            painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onTogglePin() }) {
                        Icon(
                            painterResource(Res.drawable.ic_pin),
                            contentDescription = "Pin",
                            tint = if (draft.isPinned) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { viewModel.onToggleReminder() }) {
                        Icon(
                            painterResource(Res.drawable.ic_reminder),
                            contentDescription = "Reminder",
                            tint = if (draft.reminder != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { /* More action */ }) {
                        Icon(painterResource(Res.drawable.ic_more), contentDescription = "More")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Заголовок
                BasicTextField(
                    value = draft.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        .onFocusChanged { if (!it.isFocused) viewModel.onFocusLost() },
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (draft.title.text.isEmpty()) {
                            Text(
                                "Заголовок",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )

                // Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    draft.reminder?.let { reminder ->
                        AssistChip(
                            onClick = {},
                            label = { Text(reminder.formatDateTimeCompose()) },
                            leadingIcon = {
                                Icon(
                                    painterResource(Res.drawable.ic_reminder),
                                    contentDescription = null,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                    }

                    if (draft.projectId != null) {
                        AssistChip(
                            onClick = {},
                            label = { Text(draft.projectId.value.toString()) },
                            leadingIcon = {
                                Icon(
                                    painterResource(Res.drawable.ic_folder),
                                    contentDescription = null,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                                )
                            }
                        )
                    }

                    draft.tagIds.forEach { tagId ->
                        AssistChip(
                            onClick = {},
                            label = { Text(tagId.value.toString()) }
                        )
                    }
                }

                // Контент
                BasicTextField(
                    value = draft.content,
                    onValueChange = viewModel::onContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .onFocusChanged {
                            isContentFocused = it.isFocused
                            if (!it.isFocused) {
                                isFormattingExpanded = false
                                viewModel.onFocusLost()
                            }
                        }
                        .padding(bottom = 80.dp), // Space for toolbar
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (draft.content.text.isEmpty()) {
                            Text(
                                "Заметка",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // Floating Toolbar
            AnimatedVisibility(
                visible = !isContentFocused || isFormattingExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = 16.dp)
            ) {
                FloatingToolbar(
                    isExpanded = isFormattingExpanded,
                    onExpandToggle = { isFormattingExpanded = !isFormattingExpanded },
                    onUndo = viewModel::onUndo,
                    onRedo = viewModel::onRedo,
                    onStyleClick = { /* Change note style */ },
                    onFormattingClick = { /* Text formatting */ },
                    isFormattingEnabled = isContentFocused
                )
            }
        }
    }
}

@Composable
fun FloatingToolbar(
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onStyleClick: () -> Unit,
    onFormattingClick: () -> Unit,
    isFormattingEnabled: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        AnimatedContent(
            targetState = isExpanded,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            }
        ) { expanded ->
            if (expanded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { /* Header */ }) {
                        Icon(painterResource(Res.drawable.ic_format_h1), "Header")
                    }
                    IconButton(onClick = { /* Normal text */ }) {
                        Icon(painterResource(Res.drawable.ic_match_case), "Normal text")
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    IconButton(onClick = { /* Bold */ }) {
                        Icon(painterResource(Res.drawable.ic_format_bold), "Bold")
                    }
                    IconButton(onClick = { /* Italic */ }) {
                        Icon(painterResource(Res.drawable.ic_format_italic), "Italic")
                    }
                    IconButton(onClick = { /* Underline */ }) {
                        Icon(painterResource(Res.drawable.ic_format_underline), "Underline")
                    }
                    IconButton(onClick = { /* Strikethrough */ }) {
                        Icon(painterResource(Res.drawable.ic_format_strikethrough), "Strikethrough")
                    }
                    IconButton(onClick = { /* Clear styles */ }) {
                        Icon(painterResource(Res.drawable.ic_format_clear), "Clear styles")
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onExpandToggle) {
                        Icon(painterResource(Res.drawable.ic_close), "Close")
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onUndo) {
                        Icon(painterResource(Res.drawable.ic_undo), contentDescription = "Undo")
                    }
                    IconButton(onClick = onRedo) {
                        Icon(painterResource(Res.drawable.ic_redo), contentDescription = "Redo")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = { /* Save or Add action */ },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        Icon(painterResource(Res.drawable.ic_add), contentDescription = "Add")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onStyleClick) {
                        Icon(painterResource(Res.drawable.ic_style), contentDescription = "Styles")
                    }
                    IconButton(
                        onClick = onExpandToggle,
                        enabled = isFormattingEnabled
                    ) {
                        Icon(
                            painterResource(Res.drawable.ic_text_format),
                            contentDescription = "Text Formatting",
                            tint = if (isFormattingEnabled) LocalContentColor.current else LocalContentColor.current.copy(
                                alpha = 0.3f
                            )
                        )
                    }
                }
            }
        }
    }
}
