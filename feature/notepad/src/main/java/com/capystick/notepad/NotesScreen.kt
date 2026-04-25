package com.capystick.notepad

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.notepad.viewmodel.NoteSortOrder
import com.capystick.notepad.viewmodel.NotesViewModel
import com.capystick.core.designsystem.R
import com.capystick.designsystem.components.CapyNoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onNoteClick: (Int) -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            NotesTopAppBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                sortOrder = sortOrder,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearchActiveChange = viewModel::onSearchActiveChange,
                onSortOrderChange = viewModel::onSortOrderChange,
                onMenuClick = onMenuClick
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = scaffoldPadding)
        ) {
            if (notes.isEmpty()) {
                Text(
                    text = "No hay notas aún",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(notes) { note ->
                        val plainText = remember(note.content) {
                            android.text.Html.fromHtml(note.content, android.text.Html.FROM_HTML_MODE_COMPACT).toString().trim()
                        }
                        
                        val dateString = remember(note.timestamp) {
                            val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                            sdf.format(java.util.Date(note.timestamp))
                        }

                        CapyNoteCard(
                            title = note.title,
                            dateString = dateString,
                            plainText = plainText,
                            onClick = { onNoteClick(note.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopAppBar(
    isSearchActive: Boolean,
    searchQuery: String,
    sortOrder: NoteSortOrder,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onSortOrderChange: (NoteSortOrder) -> Unit,
    onMenuClick: () -> Unit
) {
    var expandedFilter by remember { mutableStateOf(false) }

    AnimatedContent(targetState = isSearchActive, label = "search_bar_anim") { searchActive ->
        if (searchActive) {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onSearchActiveChange(false) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Cerrar búsqueda",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        } else {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Todas las notas",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Menu",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onSearchActiveChange(true) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Buscar",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { expandedFilter = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_filter_list),
                                contentDescription = "Filtrar",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = expandedFilter,
                            onDismissRequest = { expandedFilter = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recientes") },
                                onClick = {
                                    onSortOrderChange(NoteSortOrder.DATE_DESC)
                                    expandedFilter = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Antiguos") },
                                onClick = {
                                    onSortOrderChange(NoteSortOrder.DATE_ASC)
                                    expandedFilter = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("A-Z") },
                                onClick = {
                                    onSortOrderChange(NoteSortOrder.TITLE_ASC)
                                    expandedFilter = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Z-A") },
                                onClick = {
                                    onSortOrderChange(NoteSortOrder.TITLE_DESC)
                                    expandedFilter = false
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}
