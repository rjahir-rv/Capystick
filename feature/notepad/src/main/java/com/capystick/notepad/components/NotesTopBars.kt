package com.capystick.notepad.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capystick.designsystem.components.CapyTopAppBar
import com.capystick.notepad.R
import com.capystick.notepad.viewmodel.NoteSortOrder
import com.capystick.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotesTopBar(
    title: String,
    isSearchActive: Boolean,
    searchQuery: String,
    sortOrder: NoteSortOrder,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onSortOrderChange: (NoteSortOrder) -> Unit,
    onMenuClick: () -> Unit,
    showNavigationIcon: Boolean = true,
) {
    var expandedFilter by remember { mutableStateOf(false) }

    AnimatedContent(targetState = isSearchActive, label = "search_bar_animate") { searchActive ->
        if (searchActive) {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onSearchActiveChange(false) }) {
                        Icon(
                            painter = painterResource(id = DesignR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.close_search_content_description),
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
            )
        } else {
            CapyTopAppBar(
                title = title,
                onMenuClick = onMenuClick,
                showNavigationIcon = showNavigationIcon,
                actions = {
                    IconButton(onClick = { onSearchActiveChange(true) }) {
                        Icon(
                            painter = painterResource(id = DesignR.drawable.ic_search),
                            contentDescription = stringResource(R.string.search_content_description),
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Box {
                        IconButton(onClick = { expandedFilter = true }) {
                            Icon(
                                painter = painterResource(id = DesignR.drawable.ic_filter_list),
                                contentDescription = stringResource(R.string.filter_content_description),
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = expandedFilter,
                            onDismissRequest = { expandedFilter = false },
                        ) {
                            SortMenuItem(stringResource(R.string.sort_recent)) {
                                onSortOrderChange(NoteSortOrder.DATE_DESC)
                                expandedFilter = false
                            }
                            SortMenuItem(stringResource(R.string.sort_oldest)) {
                                onSortOrderChange(NoteSortOrder.DATE_ASC)
                                expandedFilter = false
                            }
                            SortMenuItem(stringResource(R.string.sort_name_asc)) {
                                onSortOrderChange(NoteSortOrder.TITLE_ASC)
                                expandedFilter = false
                            }
                            SortMenuItem(stringResource(R.string.sort_name_desc)) {
                                onSortOrderChange(NoteSortOrder.TITLE_DESC)
                                expandedFilter = false
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun SortMenuItem(
    text: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionTopBar(
    selectedCount: Int,
    isInCollection: Boolean,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddToCollectionClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.selected_count, selectedCount),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.cancel_selection_content_description),
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        actions = {
            IconButton(onClick = onShareClick) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_share),
                    contentDescription = stringResource(R.string.share_content_description),
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = onAddToCollectionClick) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_add_collection),
                    contentDescription = stringResource(R.string.add_to_collection_content_description),
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_delete),
                    contentDescription = if (isInCollection) {
                        stringResource(R.string.remove_from_collection_content_description)
                    } else {
                        stringResource(R.string.delete_content_description)
                    },
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}
