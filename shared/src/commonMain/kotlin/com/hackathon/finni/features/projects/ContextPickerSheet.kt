package com.hackathon.finni.features.projects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hackathon.finni.api.note.model.NoteContext
import com.hackathon.finni.core.resource.getValueOrNull
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_archive
import com.hackathon.finni.resources.ic_delete
import com.hackathon.finni.resources.ic_folder
import com.hackathon.finni.resources.ic_inbox
import com.hackathon.finni.resources.ic_notes
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextPickerSheet(
    initialContext: NoteContext,
    onDismiss: () -> Unit,
    viewModel: ContextPickerViewModel = koinViewModel(parameters = { parametersOf(initialContext) })
) {
    val state by viewModel.state.collectAsState()
    val projectsResource by viewModel.projects.collectAsState()
    val projects = projectsResource.getValueOrNull() ?: emptyList()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "Выбор контекста",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Группа: Оперативные
                item {
                    ContextMenuItem(
                        label = "Входящие",
                        icon = painterResource(Res.drawable.ic_inbox),
                        count = state.inboxCount,
                        isSelected = state.selectedContext is NoteContext.Inbox,
                        badgeColor = MaterialTheme.colorScheme.primary,
                        onClick = {
                            viewModel.onContextClick(NoteContext.Inbox)
                            onDismiss()
                        }
                    )
                }
                item {
                    ContextMenuItem(
                        label = "Все заметки",
                        icon = painterResource(Res.drawable.ic_notes),
                        count = state.allCount,
                        isSelected = state.selectedContext is NoteContext.All,
                        onClick = {
                            viewModel.onContextClick(NoteContext.All)
                            onDismiss()
                        }
                    )
                }

                // Группа: Организация
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Проекты",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(projects) { project ->
                    ContextMenuItem(
                        label = project.name,
                        icon = painterResource(Res.drawable.ic_folder),
                        count = state.projectCounts[project.id] ?: 0,
                        isSelected = (state.selectedContext as? NoteContext.Project)?.id == project.id,
                        onClick = {
                            viewModel.onContextClick(
                                NoteContext.Project(
                                    project.id,
                                    project.name
                                )
                            )
                            onDismiss()
                        }
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // Группа: Служебные
                item {
                    ContextMenuItem(
                        label = "Архив",
                        icon = painterResource(Res.drawable.ic_archive),
                        isSelected = state.selectedContext is NoteContext.Archive,
                        onClick = {
                            viewModel.onContextClick(NoteContext.Archive)
                            onDismiss()
                        }
                    )
                }
                item {
                    ContextMenuItem(
                        label = "Корзина",
                        icon = painterResource(Res.drawable.ic_delete),
                        count = state.trashCount,
                        isSelected = state.selectedContext is NoteContext.Trash,
                        onClick = {
                            viewModel.onContextClick(NoteContext.Trash)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    label: String,
    icon: Painter,
    isSelected: Boolean,
    onClick: () -> Unit,
    count: Int? = null,
    badgeColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (count != null && count > 0) {
                Badge(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else badgeColor,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else contentColorFor(
                        badgeColor
                    )
                ) {
                    Text(count.toString())
                }
            }
        }
    }
}
