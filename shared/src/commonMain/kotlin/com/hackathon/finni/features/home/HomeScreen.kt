package com.hackathon.finni.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hackathon.finni.api.note.model.NoteContext
import com.hackathon.finni.api.note.model.NoteFilter
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.note.model.NoteResponse
import com.hackathon.finni.core.paginator.PageState
import com.hackathon.finni.core.paginator.Paginator
import com.hackathon.finni.core.paginator.PaginatorEffect
import com.hackathon.finni.core.paginator.shouldShowAppend
import com.hackathon.finni.core.paginator.shouldShowPrepend
import com.hackathon.finni.core.ui.components.AppPullToRefreshBox
import com.hackathon.finni.features.projects.ContextPickerSheet
import com.hackathon.finni.resources.Res
import com.hackathon.finni.resources.ic_edit
import com.hackathon.finni.resources.ic_folder
import com.hackathon.finni.resources.ic_menu
import com.hackathon.finni.resources.ic_reminder
import com.hackathon.finni.resources.ic_search
import com.hackathon.finni.resources.ic_sort
import com.hackathon.finni.resources.ic_tag
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val paginator by viewModel.paginator.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Box(
                Modifier.width(300.dp).fillMaxSize().background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text("Navigation Drawer")
            }
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    isSearchExpanded = uiState.isSearchExpanded,
                    searchQuery = uiState.filter.searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearchExpandToggle = viewModel::onSearchExpandToggle,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                HomeBottomToolbar(
                    onContextClick = viewModel::onSelectContextClick,
                    onReminderToggle = viewModel::onToggleReminderOnly,
                    onTagsClick = viewModel::onSelectTagsClick,
                    onCreateClick = viewModel::onCreateNoteClick,
                    isReminderFilterActive = uiState.filter.hasReminderOnly
                )
            }
        ) { paddingValues ->
            AppPullToRefreshBox(
                refreshHandler = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ActiveFiltersRow(
                        filter = uiState.filter,
                        onContextClick = viewModel::onSelectContextClick,
                        onReminderClick = viewModel::onToggleReminderOnly,
                        onTagsClick = viewModel::onSelectTagsClick
                    )

                    NotesGrid(
                        pinnedNotes = uiState.pinnedNotes,
                        otherNotes = uiState.otherNotes,
                        onNoteClick = viewModel::onNoteClick,
                        paginator = paginator,
                        appendState = uiState.appendState,
                        prependState = uiState.prependState
                    )
                }
            }

            // Internal Pickers
            uiState.activePicker?.let { picker ->
                when (picker) {
                    is HomePicker.Context -> {
                        ContextPickerSheet(
                            initialContext = picker.selected,
                            onDismiss = viewModel::onDismissPicker
                        )
                    }

                    is HomePicker.Tags -> {
                        // TODO: TagPickerSheet(picker.selectedIds)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    isSearchExpanded: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchExpandToggle: (Boolean) -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isSearchExpanded) 0.dp else 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = { /* Execute search */ },
                    expanded = isSearchExpanded,
                    onExpandedChange = onSearchExpandToggle,
                    placeholder = { Text("Поиск заметок") },
                    leadingIcon = {
                        IconButton(
                            onClick = if (isSearchExpanded) {
                                { onSearchExpandToggle(false) }
                            } else onMenuClick) {
                            Icon(
                                painterResource(if (isSearchExpanded) Res.drawable.ic_search else Res.drawable.ic_menu),
                                contentDescription = null
                            )
                        }
                    },
                    trailingIcon = {
                        if (!isSearchExpanded) {
                            IconButton(onClick = { /* Sort */ }) {
                                Icon(
                                    painterResource(Res.drawable.ic_sort),
                                    contentDescription = "Sort"
                                )
                            }
                        }
                    }
                )
            },
            expanded = isSearchExpanded,
            onExpandedChange = onSearchExpandToggle,
            modifier = Modifier.fillMaxWidth(),
            content = {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(selected = false, onClick = {}, label = { Text("Проекты") })
                        FilterChip(selected = false, onClick = {}, label = { Text("Теги") })
                        FilterChip(selected = false, onClick = {}, label = { Text("Напоминания") })
                    }
                    Text(
                        "Результаты поиска будут здесь",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }
}

@Composable
private fun ActiveFiltersRow(
    filter: NoteFilter,
    onContextClick: () -> Unit,
    onReminderClick: () -> Unit,
    onTagsClick: () -> Unit
) {
    AnimatedVisibility(visible = filter.hasActiveFilters) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filter.context != NoteContext.All) {
                InputChip(
                    selected = true,
                    onClick = onContextClick,
                    label = { Text(filter.context.toString()) },
                    leadingIcon = {
                        Icon(painterResource(Res.drawable.ic_folder), null, Modifier.size(18.dp))
                    }
                )
            }

            if (filter.hasReminderOnly) {
                InputChip(
                    selected = true,
                    onClick = onReminderClick,
                    label = { Text("С напоминаниями") },
                    leadingIcon = {
                        Icon(painterResource(Res.drawable.ic_reminder), null, Modifier.size(18.dp))
                    }
                )
            }

            filter.selectedTagIds.forEach { tagId ->
                InputChip(
                    selected = true,
                    onClick = onTagsClick,
                    label = { Text(tagId.value.toString()) },
                    leadingIcon = {
                        Icon(painterResource(Res.drawable.ic_tag), null, Modifier.size(18.dp))
                    }
                )
            }
        }
    }
}

@Composable
private fun NotesGrid(
    pinnedNotes: List<NoteResponse>,
    otherNotes: List<NoteResponse>,
    onNoteClick: (NoteId) -> Unit,
    paginator: Paginator<NoteResponse>,
    appendState: PageState,
    prependState: PageState
) {
    val gridState = rememberLazyGridState()

    PaginatorEffect(
        state = gridState,
        paginator = paginator
    )

    val showPrepend = gridState.shouldShowPrepend(prependState)
    val showAppend = gridState.shouldShowAppend(appendState)

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (showPrepend) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }
            }
        }

        if (pinnedNotes.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "ЗАКРЕПЛЕННЫЕ",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(pinnedNotes) { note ->
                NoteCard(note = note, onClick = { onNoteClick(note.id) })
            }

            if (otherNotes.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "ОСТАЛЬНЫЕ",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
            }
        }

        items(otherNotes) { note ->
            NoteCard(note = note, onClick = { onNoteClick(note.id) })
        }

        if (showAppend) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeBottomToolbar(
    onContextClick: () -> Unit,
    onReminderToggle: () -> Unit,
    onTagsClick: () -> Unit,
    onCreateClick: () -> Unit,
    isReminderFilterActive: Boolean
) {
    BottomAppBar(
        actions = {
            IconButton(onClick = onContextClick) {
                Icon(painterResource(Res.drawable.ic_folder), contentDescription = "Context")
            }
            IconButton(onClick = onReminderToggle) {
                Icon(
                    painterResource(Res.drawable.ic_reminder),
                    contentDescription = "Reminders",
                    tint = if (isReminderFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onTagsClick) {
                Icon(painterResource(Res.drawable.ic_tag), contentDescription = "Tags")
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(painterResource(Res.drawable.ic_edit), contentDescription = "Create Note")
            }
        }
    )
}
