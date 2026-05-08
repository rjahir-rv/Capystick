package com.capystick.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import com.capystick.designsystem.components.CapyTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.capystick.collections.CollectionsScreen
import com.capystick.collections.FAVORITES_COLLECTION_ID
import com.capystick.notepad.NotePreviewScreen
import com.capystick.notepad.NotepadScreen
import com.capystick.notepad.NotesScreen
import com.capystick.core.designsystem.R
import com.capystick.settings.SettingsScreen
import com.capystick.settings.TrashScreen
import com.capystick.widget.WidgetConfigurationScreen
import com.capystick.widget.WidgetManagementScreen
import com.capystick.backup.BackupScreen
import com.capystick.scan.ScanScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    externalNavigationCommand: ExternalNavigationCommand? = null,
    onExternalNavigationHandled: () -> Unit = {},
) {
    val levelRoutes = listOf(NotepadRoute, NotesRoute, ScanRoute, CollectionsRoute, SettingsRoute)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val topLevelBackStack = remember {
        TopLevelBackStack<TopLevelRoute>(NotepadRoute).apply {
            externalNavigationCommand?.let(::applyExternalNavigationCommand)
        }
    }
    var recentlyDeletedNoteIds by remember {
        mutableStateOf<Set<Int>>(emptySet())
    }
    var skipInitialExternalNavigationHandling by remember {
        mutableStateOf(externalNavigationCommand != null)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier.height(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.capystick_logo),
                        contentDescription = "Capystick logo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        alignment = Alignment.CenterStart,
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier.height(12.dp))

                    levelRoutes.forEach { item ->
                        val isSelected = item == topLevelBackStack.topLevelKey
                        NavigationDrawerItem(
                            label = { Text(item.title) },
                            selected = isSelected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                topLevelBackStack.addTopLevel(item)
                            },
                            icon = {
                                val iconRes = when (item.title) {
                                    "Crear nota" -> painterResource(id = R.drawable.ic_new_note)
                                    "Todas las notas" -> painterResource(id = R.drawable.ic_all_notes)
                                    "Colecciones" -> painterResource(id = R.drawable.ic_collection)
                                    "Ajustes" -> painterResource(id = R.drawable.ic_settings)
                                    "Escanear nota" -> painterResource(id = R.drawable.ic_document_scann)
                                    else -> null
                                }
                                Icon(painter = iconRes!!, contentDescription = item.title)
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.padding(paddingValues = NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                val currentRoute = topLevelBackStack.backStack.lastOrNull()
                if (currentRoute is TopLevelRoute && currentRoute != NotepadRoute && currentRoute != NotesRoute && currentRoute != CollectionsRoute && currentRoute != ScanRoute) {
                    CapyTopAppBar(
                        title = currentRoute.title,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }
            }
        ) { innerPadding ->
            androidx.compose.runtime.LaunchedEffect(externalNavigationCommand) {
                val command = externalNavigationCommand ?: return@LaunchedEffect
                if (skipInitialExternalNavigationHandling) {
                    skipInitialExternalNavigationHandling = false
                    onExternalNavigationHandled()
                    return@LaunchedEffect
                }

                topLevelBackStack.applyExternalNavigationCommand(command)
                onExternalNavigationHandled()
            }

            NavDisplay(
                modifier = modifier,
                backStack = topLevelBackStack.backStack,
                onBack = { topLevelBackStack.removeLast() },
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { -it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { it }
                    )
                },
                popTransitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { -it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { it }
                    )
                },
                entryProvider = entryProvider {
                    entry<NotepadRoute> {
                        NotepadScreen(
                            innerPadding = innerPadding,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onNoteSaved = {
                                topLevelBackStack.addTopLevel(NotesRoute)
                            }
                        )
                    }
                    entry<CollectionsRoute> {
                        CollectionsScreen(
                            innerPadding = innerPadding,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onCollectionClick = { id, name ->
                                if (id == FAVORITES_COLLECTION_ID) {
                                    topLevelBackStack.addRoute(FavoriteNotesRoute)
                                } else {
                                    topLevelBackStack.addRoute(CollectionNotesRoute(id, name))
                                }
                            }
                        )
                    }
                    entry<NotesRoute> {
                        NotesScreen(
                            innerPadding = innerPadding,
                            recentlyDeletedNoteIds = recentlyDeletedNoteIds,
                            onRecentlyDeletedHandled = {
                                recentlyDeletedNoteIds = emptySet()
                            },
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onNoteClick = { noteId ->
                                topLevelBackStack.addRoute(NotePreviewRoute(noteId, isUnlocked = true))
                            },
                            onAddNoteClick = {
                                topLevelBackStack.addTopLevel(NotepadRoute)
                            }
                        )

                    }
                    entry<ScanRoute> {
                        ScanScreen(
                            innerPadding = innerPadding,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onNoteSaved = {
                                topLevelBackStack.addTopLevel(NotesRoute)
                            }
                        )
                    }
                    entry<CollectionNotesRoute> { args ->
                        NotesScreen(
                            innerPadding = innerPadding,
                            collectionId = args.collectionId,
                            collectionName = args.collectionName,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onNoteClick = { noteId ->
                                topLevelBackStack.addRoute(NotePreviewRoute(noteId, isUnlocked = true))
                            },
                            onAddNoteClick = {
                                topLevelBackStack.addRoute(CreateCollectionNoteRoute(args.collectionId))
                            }
                        )
                    }
                    entry<FavoriteNotesRoute> {
                        NotesScreen(
                            innerPadding = innerPadding,
                            favoriteOnly = true,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onNoteClick = { noteId ->
                                topLevelBackStack.addRoute(NotePreviewRoute(noteId, isUnlocked = true))
                            },
                            onAddNoteClick = {
                                topLevelBackStack.addTopLevel(NotepadRoute)
                            }
                        )
                    }
                    entry<CreateCollectionNoteRoute> { args ->
                        NotepadScreen(
                            noteId = null,
                            collectionId = args.collectionId,
                            innerPadding = innerPadding,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onNoteSaved = {
                                topLevelBackStack.removeLast()
                            }
                        )
                    }
                    entry<SettingsRoute> {
                        SettingsScreen(
                            innerPadding = innerPadding,
                            onTrashClick = {
                                topLevelBackStack.addRoute(TrashRoute)
                            },
                            onBackupClick = {
                                topLevelBackStack.addRoute(BackupRoute)
                            },
                            onWidgetsClick = {
                                topLevelBackStack.addRoute(WidgetManagementRoute)
                            },
                        )
                    }
                    entry<WidgetManagementRoute> {
                        WidgetManagementScreen(
                            innerPadding = innerPadding,
                            onBack = { topLevelBackStack.removeLast() },
                            onEditWidget = { appWidgetId ->
                                topLevelBackStack.addRoute(WidgetConfigurationRoute(appWidgetId))
                            },
                        )
                    }
                    entry<WidgetConfigurationRoute> { args ->
                        WidgetConfigurationScreen(
                            appWidgetId = args.appWidgetId,
                            innerPadding = innerPadding,
                            onBack = { topLevelBackStack.removeLast() },
                        )
                    }
                    entry<TrashRoute> {
                        TrashScreen(
                            innerPadding = innerPadding,
                            onBack = { topLevelBackStack.removeLast() },
                        )
                    }
                    entry<BackupRoute> {
                        BackupScreen(
                            innerPadding = innerPadding,
                            onBack = { topLevelBackStack.removeLast() },
                        )
                    }
                    entry<NotePreviewRoute> { args ->
                       NotePreviewScreen(
                            noteId = args.noteId,
                            innerPadding = innerPadding,
                            isUnlockedInitially = args.isUnlocked,
                            onBack = { topLevelBackStack.removeLast() },
                            onDeleteComplete = { topLevelBackStack.addTopLevel(NotesRoute) },
                            onNoteMovedToTrash = { deletedNoteId ->
                                recentlyDeletedNoteIds = setOf(deletedNoteId)
                            },
                            onEditNote = { noteId, isUnlocked ->
                                topLevelBackStack.addRoute(EditNoteRoute(noteId, isUnlocked))
                            }
                        )
                    }
                    entry<EditNoteRoute> { args ->
                        NotepadScreen(
                            noteId = args.noteId,
                            innerPadding = innerPadding,
                            isUnlockedInitially = args.isUnlocked,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            },
                            onNoteSaved = {
                                topLevelBackStack.removeLast()
                            }
                        )
                    }
                }
            )
        }
    }
}

private fun TopLevelBackStack<TopLevelRoute>.applyExternalNavigationCommand(
    command: ExternalNavigationCommand,
) {
    when (command) {
        is ExternalNavigationCommand.OpenNotes -> {
            addTopLevel(NotesRoute)
        }

        is ExternalNavigationCommand.OpenCreateNote -> {
            addTopLevel(NotepadRoute)
        }

        is ExternalNavigationCommand.OpenCollections -> {
            addTopLevel(CollectionsRoute)
        }

        is ExternalNavigationCommand.OpenCollection -> {
            addTopLevel(CollectionsRoute)
            addRoute(
                CollectionNotesRoute(
                    collectionId = command.collectionId,
                    collectionName = command.collectionName,
                ),
            )
        }

        is ExternalNavigationCommand.OpenEditRecentNote -> {
            addTopLevel(NotesRoute)
            addRoute(EditNoteRoute(command.noteId))
        }

        is ExternalNavigationCommand.OpenEditCollectionNote -> {
            addTopLevel(CollectionsRoute)
            addRoute(
                CollectionNotesRoute(
                    collectionId = command.collectionId,
                    collectionName = command.collectionName,
                ),
            )
            addRoute(EditNoteRoute(command.noteId))
        }

        is ExternalNavigationCommand.OpenWidgetManagement -> {
            addTopLevel(SettingsRoute)
            addRoute(WidgetManagementRoute)
        }

        is ExternalNavigationCommand.OpenWidgetEditor -> {
            addTopLevel(SettingsRoute)
            addRoute(WidgetManagementRoute)
            addRoute(WidgetConfigurationRoute(command.appWidgetId))
        }
    }
}

sealed interface ExternalNavigationCommand {
    data object OpenNotes : ExternalNavigationCommand
    data object OpenCreateNote : ExternalNavigationCommand
    data object OpenCollections : ExternalNavigationCommand
    data object OpenWidgetManagement : ExternalNavigationCommand
    data class OpenCollection(
        val collectionId: Int,
        val collectionName: String,
    ) : ExternalNavigationCommand

    data class OpenEditRecentNote(val noteId: Int) : ExternalNavigationCommand

    data class OpenEditCollectionNote(
        val noteId: Int,
        val collectionId: Int,
        val collectionName: String,
    ) : ExternalNavigationCommand

    data class OpenWidgetEditor(val appWidgetId: Int) : ExternalNavigationCommand
}

class TopLevelBackStack<T: TopLevelRoute>(startKey: T) {
    private var topLevelStacks : LinkedHashMap<T, SnapshotStateList<Any>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf<Any>(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    fun addTopLevel(key: T, resetToRoot: Boolean = true){
        if (topLevelStacks[key] == null || resetToRoot){
            topLevelStacks.remove(key)
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            topLevelStacks.apply {
                remove(key)?.let { put(key, it) }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun addRoute(key: Any){
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast(){
        val currentStack = topLevelStacks[topLevelKey]
        currentStack?.removeLastOrNull()
        if (currentStack?.isEmpty() == true) {
            topLevelStacks.remove(topLevelKey)
            if (topLevelStacks.isNotEmpty()) {
                topLevelKey = topLevelStacks.keys.last()
            }
        }
        updateBackStack()
    }
}
