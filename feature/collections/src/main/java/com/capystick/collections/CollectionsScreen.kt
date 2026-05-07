package com.capystick.collections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.collections.viewmodel.CollectionSortOrder
import com.capystick.collections.viewmodel.CollectionsViewModel
import com.capystick.core.designsystem.R
import com.capystick.model.Collection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onCollectionClick: (Int, String) -> Unit = { _, _ -> },
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val favoriteNoteCount by viewModel.favoriteNoteCount.collectAsStateWithLifecycle()
    val showFavoritesCollection = searchQuery.isBlank() ||
        "Favoritas".contains(searchQuery, ignoreCase = true)

    var collectionToRename by remember { mutableStateOf<Collection?>(null) }
    var collectionToDelete by remember { mutableStateOf<Collection?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CollectionsTopAppBar(
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
                .padding(scaffoldPadding)
        ) {
            if (collections.isEmpty() && !showFavoritesCollection) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "No se encontro una coleccion relacionada con tu busqueda"
                        } else {
                            "No hay colecciones aún"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showFavoritesCollection) {
                        item {
                            FavoriteCollectionItem(
                                noteCount = favoriteNoteCount,
                                onClick = { onCollectionClick(FAVORITES_COLLECTION_ID, "Favoritas") }
                            )
                        }
                    }
                    items(collections) { collection ->
                        CollectionItem(
                            collection = collection,
                            onClick = { onCollectionClick(collection.id, collection.name) },
                            onRename = { collectionToRename = collection },
                            onDelete = { collectionToDelete = collection }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Nueva colección"
                )
            }

            if (showCreateDialog) {
                CollectionNameDialog(
                    title = "Nueva colección",
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name ->
                        viewModel.createCollection(name)
                        showCreateDialog = false
                    }
                )
            }

            if (collectionToRename != null) {
                CollectionNameDialog(
                    title = "Renombrar colección",
                    initialName = collectionToRename!!.name,
                    onDismiss = { collectionToRename = null },
                    onConfirm = { newName ->
                        viewModel.renameCollection(collectionToRename!!, newName)
                        collectionToRename = null
                    }
                )
            }

            if (collectionToDelete != null) {
                AlertDialog(
                    onDismissRequest = { collectionToDelete = null },
                    title = { Text(text = "Eliminar colección") },
                    text = { Text(text = "¿Estás seguro de que deseas eliminar la colección '${collectionToDelete?.name}'? Las notas asociadas no se eliminarán, solo se quitarán de esta colección.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteCollection(collectionToDelete!!)
                                collectionToDelete = null
                            }
                        ) {
                            Text(text = "Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { collectionToDelete = null }) {
                            Text(text = "Cancelar")
                        }
                    }
                )
            }
        }
    }
}

const val FAVORITES_COLLECTION_ID = -1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsTopAppBar(
    isSearchActive: Boolean,
    searchQuery: String,
    sortOrder: CollectionSortOrder,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onSortOrderChange: (CollectionSortOrder) -> Unit,
    onMenuClick: () -> Unit
) {
    var expandedFilter by remember { mutableStateOf(false) }

    androidx.compose.animation.AnimatedContent(targetState = isSearchActive, label = "search_bar_anim") { searchActive ->
        if (searchActive) {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar colección...") },
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
                        text = "Colecciones",
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
                                text = { Text("A-Z") },
                                onClick = {
                                    onSortOrderChange(CollectionSortOrder.NAME_ASC)
                                    expandedFilter = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Z-A") },
                                onClick = {
                                    onSortOrderChange(CollectionSortOrder.NAME_DESC)
                                    expandedFilter = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Más notas") },
                                onClick = {
                                    onSortOrderChange(CollectionSortOrder.NOTE_COUNT_DESC)
                                    expandedFilter = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Menos notas") },
                                onClick = {
                                    onSortOrderChange(CollectionSortOrder.NOTE_COUNT_ASC)
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


@Composable
fun CollectionItem(
    collection: Collection,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${collection.noteCount} NOTES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = onRename,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Rename",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteCollectionItem(
    noteCount: Int,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Favoritas",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$noteCount NOTES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun CollectionNameDialog(
    title: String,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
