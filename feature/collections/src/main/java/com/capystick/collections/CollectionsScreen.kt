package com.capystick.collections

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.collections.components.CollectionItem
import com.capystick.collections.components.CollectionNameDialog
import com.capystick.collections.components.CollectionsStarterEmptyState
import com.capystick.collections.components.CollectionsTopAppBar
import com.capystick.collections.components.FavoriteCollectionItem
import com.capystick.collections.viewmodel.CollectionsViewModel
import com.capystick.model.Collection
import com.capystick.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    showNavigationIcon: Boolean = true,
    onCollectionClick: (Int, String) -> Unit = { _, _ -> },
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val favoriteNoteCount by viewModel.favoriteNoteCount.collectAsStateWithLifecycle()
    val favoritesCollectionName = stringResource(R.string.favorites_collection_name)
    val showFavoritesCollection = searchQuery.isBlank() ||
        favoritesCollectionName.contains(searchQuery, ignoreCase = true)
    val showStarterEmptyState = collections.isEmpty() && searchQuery.isBlank()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val layoutDirection = LocalLayoutDirection.current
    val gridColumns = if (isLandscape) {
        GridCells.Adaptive(minSize = 220.dp)
    } else {
        GridCells.Fixed(2)
    }

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
                onMenuClick = onMenuClick,
                showNavigationIcon = showNavigationIcon
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            if (collections.isEmpty() && !showFavoritesCollection) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            stringResource(R.string.collections_empty_search)
                        } else {
                            stringResource(R.string.collections_empty)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = gridColumns,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showFavoritesCollection) {
                        item {
                            FavoriteCollectionItem(
                                noteCount = favoriteNoteCount,
                                onClick = { onCollectionClick(FAVORITES_COLLECTION_ID, favoritesCollectionName) }
                            )
                        }
                    }
                    if (showStarterEmptyState && isLandscape) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CollectionsStarterEmptyState(
                                modifier = Modifier.padding(horizontal = 40.dp),
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

            if (showStarterEmptyState && !isLandscape) {
                CollectionsStarterEmptyState(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 40.dp),
                )
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
                    painter = painterResource(id = DesignR.drawable.ic_add),
                    contentDescription = stringResource(R.string.new_collection_content_description)
                )
            }

            if (showCreateDialog) {
                CollectionNameDialog(
                    title = stringResource(R.string.new_collection_title),
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name ->
                        viewModel.createCollection(name)
                        showCreateDialog = false
                    }
                )
            }

            if (collectionToRename != null) {
                CollectionNameDialog(
                    title = stringResource(R.string.rename_collection_title),
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
                    title = { Text(text = stringResource(R.string.delete_collection_title)) },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.delete_collection_message,
                                collectionToDelete?.name.orEmpty(),
                            ),
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteCollection(collectionToDelete!!)
                                collectionToDelete = null
                            }
                        ) {
                            Text(text = stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { collectionToDelete = null }) {
                            Text(text = stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

const val FAVORITES_COLLECTION_ID = -1
