package com.capystick.notepad

import android.content.Intent
import android.text.Html
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.notepad.viewmodel.NoteSortOrder
import com.capystick.notepad.viewmodel.NotesViewModel
import com.capystick.core.designsystem.R
import com.capystick.designsystem.components.CapyNoteCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    collectionId: Int? = null,
    collectionName: String? = null,
    onMenuClick: () -> Unit = {},
    onNoteClick: (Int) -> Unit = {},
    onAddNoteClick: () -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel()
) {
    LaunchedEffect(collectionId, collectionName) {
        viewModel.initialize(collectionId, collectionName)
    }

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsStateWithLifecycle()

    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            if (isSelectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedNoteIds.size,
                    isInCollection = collectionId != null,
                    onCloseClick = { viewModel.clearSelection() },
                    onDeleteClick = { showDeleteDialog = true },
                    onShareClick = {
                        val selectedNotes = notes.filter { selectedNoteIds.contains(it.id) }
                        val textToShare = selectedNotes.joinToString(separator = "\n\n---\n\n") {
                            "${it.title}\n${Html.fromHtml(it.content, Html.FROM_HTML_MODE_COMPACT).toString().trim()}"
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, textToShare)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir notas"))
                        viewModel.clearSelection()
                    },
                    onAddToCollectionClick = { showBottomSheet = true }
                )
            } else {
                NotesTopAppBar(
                    title = title,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearchActiveChange = viewModel::onSearchActiveChange,
                    onSortOrderChange = viewModel::onSortOrderChange,
                    onMenuClick = onMenuClick
                )
            }
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = scaffoldPadding)
        ) {
            if (notes.isEmpty()) {
                Text(
                    text = if (collectionId != null) "Aun no hay notas en esta coleccion" else "No hay notas aún",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(notes) { note ->
                        val plainText = remember( key1 = note.content) {
                            Html.fromHtml(note.content, Html.FROM_HTML_MODE_COMPACT).toString().trim()
                        }
                        
                        val dateString = remember(key1 = note.timestamp) {
                            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                            sdf.format(Date(note.timestamp))
                        }

                        val isSelected = selectedNoteIds.contains(note.id)

                        CapyNoteCard(
                            title = note.title,
                            dateString = dateString,
                            plainText = plainText,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelectionMode) {
                                    viewModel.toggleSelection(note.id)
                                } else {
                                    onNoteClick(note.id)
                                }
                            },
                            onLongClick = {
                                viewModel.toggleSelection(note.id)
                            }
                        )
                    }
                }
            }
            
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onAddNoteClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Crear nota"
                    )
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false 
                },
                sheetState = sheetState
            ) {
                CollectionAssignmentContent(
                    collections = collections,
                    onCollectionSelected = { collectionId ->
                        viewModel.addSelectedNotesToCollection(collectionId)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                        }
                    },
                    onCreateCollection = { name ->
                        viewModel.createCollectionAndAddSelectedNotes(name)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showBottomSheet = false
                        }
                    }
                )
            }
        }

        if (showDeleteDialog) {
            val isRemovingFromCollection = collectionId != null
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { 
                    Text(text = if (isRemovingFromCollection) "Quitar de la colección" else "Eliminar notas") 
                },
                text = { 
                    Text(text = if (isRemovingFromCollection) 
                        "¿Estás seguro de que deseas quitar las ${selectedNoteIds.size} notas de esta colección? Seguirán estando disponibles en 'Todas las notas'." 
                        else "¿Estás seguro de que deseas eliminar las ${selectedNoteIds.size} notas seleccionadas?") 
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.deleteSelectedNotes()
                            showDeleteDialog = false
                        }
                    ) {
                        Text(text = if (isRemovingFromCollection) "Quitar" else "Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteDialog = false }) {
                        Text(text = "Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun CollectionAssignmentContent(
    collections: List<com.capystick.model.Collection>,
    onCollectionSelected: (Int) -> Unit,
    onCreateCollection: (String) -> Unit
) {
    var newCollectionName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Añadir a colección",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        if (collections.isEmpty()) {
            Text(text = "No tienes colecciones. Crea una nueva:")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(collections) { collection ->
                    Surface(
                        onClick = { onCollectionSelected(collection.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = collection.name,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            Text(text = "O crea una nueva:")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newCollectionName,
                onValueChange = { newCollectionName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nombre de la colección") },
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (newCollectionName.isNotBlank()) {
                        onCreateCollection(newCollectionName)
                    }
                },
                enabled = newCollectionName.isNotBlank()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_new_note),
                    contentDescription = "Crear",
                    tint = if (newCollectionName.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopAppBar(
    title: String,
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
                        text = title,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
    selectedCount: Int,
    isInCollection: Boolean,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddToCollectionClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "$selectedCount seleccionadas",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Cancelar selección",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onShareClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = "Compartir",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onAddToCollectionClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_collection),
                    contentDescription = "Añadir a colección",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = if (isInCollection) "Quitar de la colección" else "Eliminar",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
