package com.capystick.navigation

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.capystick.checklist.ChecklistScreen
import com.capystick.collections.CollectionsScreen
import com.capystick.collections.FAVORITES_COLLECTION_ID
import com.capystick.notepad.NotePreviewScreen
import com.capystick.notepad.NotepadScreen
import com.capystick.notepad.NotesScreen
import com.capystick.settings.AboutScreen
import com.capystick.settings.SettingsScreen
import com.capystick.settings.TrashScreen
import com.capystick.widget.WidgetConfigurationScreen
import com.capystick.widget.WidgetManagementScreen
import com.capystick.backup.BackupScreen
import com.capystick.scan.ScanScreen
import kotlinx.coroutines.launch
import com.capystick.core.designsystem.R as DesignR

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    externalNavigationCommand: ExternalNavigationCommand? = null,
    onExternalNavigationHandled: () -> Unit = {},
) {
    val levelRoutes = listOf(NotepadRoute, NotesRoute, ScanRoute, CollectionsRoute, SettingsRoute)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val topLevelBackStack = rememberSaveable(saver = TopLevelBackStack.Saver) {
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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val openDrawer = {
        if (!isLandscape) {
            scope.launch { drawerState.open() }
        }
    }

    if (isLandscape) {
        Row(modifier = modifier.fillMaxSize()) {
            AppNavigationRail(
                levelRoutes = levelRoutes,
                selectedRoute = topLevelBackStack.topLevelKey,
                onRouteClick = topLevelBackStack::addTopLevel,
            )
            AppNavigationScaffold(
                modifier = Modifier.weight(1f),
                topLevelBackStack = topLevelBackStack,
                externalNavigationCommand = externalNavigationCommand,
                onExternalNavigationHandled = onExternalNavigationHandled,
                skipInitialExternalNavigationHandling = skipInitialExternalNavigationHandling,
                onSkipInitialExternalNavigationHandled = {
                    skipInitialExternalNavigationHandling = false
                },
                recentlyDeletedNoteIds = recentlyDeletedNoteIds,
                onRecentlyDeletedNoteIdsChange = { recentlyDeletedNoteIds = it },
                onOpenDrawer = openDrawer,
                showNavigationIcon = false,
            )
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppNavigationDrawer(
                    levelRoutes = levelRoutes,
                    selectedRoute = topLevelBackStack.topLevelKey,
                    onRouteClick = { item ->
                        scope.launch { drawerState.close() }
                        topLevelBackStack.addTopLevel(item)
                    },
                )
            },
            modifier = modifier
        ) {
            AppNavigationScaffold(
                modifier = modifier,
                topLevelBackStack = topLevelBackStack,
                externalNavigationCommand = externalNavigationCommand,
                onExternalNavigationHandled = onExternalNavigationHandled,
                skipInitialExternalNavigationHandling = skipInitialExternalNavigationHandling,
                onSkipInitialExternalNavigationHandled = {
                    skipInitialExternalNavigationHandling = false
                },
                recentlyDeletedNoteIds = recentlyDeletedNoteIds,
                onRecentlyDeletedNoteIdsChange = { recentlyDeletedNoteIds = it },
                onOpenDrawer = openDrawer,
                showNavigationIcon = true,
            )
        }
    }
}

@Composable
private fun AppNavigationDrawer(
    levelRoutes: List<TopLevelRoute>,
    selectedRoute: TopLevelRoute,
    onRouteClick: (TopLevelRoute) -> Unit,
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))
            Image(
                painter = painterResource(id = DesignR.drawable.capystick_logo),
                contentDescription = stringResource(R.string.capystick_logo_content_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                alignment = Alignment.CenterStart,
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(12.dp))

            levelRoutes.forEach { item ->
                val title = stringResource(item.titleRes)
                NavigationDrawerItem(
                    label = { Text(title) },
                    selected = item == selectedRoute,
                    onClick = { onRouteClick(item) },
                    icon = { TopLevelRouteIcon(item = item, contentDescription = title) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.padding(paddingValues = NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

@Composable
private fun AppNavigationRail(
    levelRoutes: List<TopLevelRoute>,
    selectedRoute: TopLevelRoute,
    onRouteClick: (TopLevelRoute) -> Unit,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            levelRoutes.forEach { item ->
                val title = stringResource(item.titleRes)
                NavigationRailItem(
                    selected = item == selectedRoute,
                    onClick = { onRouteClick(item) },
                    icon = { TopLevelRouteIcon(item = item, contentDescription = title) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    ),
                )
            }
        }
    }
}

@Composable
private fun TopLevelRouteIcon(
    item: TopLevelRoute,
    contentDescription: String,
) {
    val iconRes = when (item) {
        NotepadRoute -> DesignR.drawable.ic_new_note
        NotesRoute -> DesignR.drawable.ic_all_notes
        CollectionsRoute -> DesignR.drawable.ic_collection
        SettingsRoute -> DesignR.drawable.ic_settings
        ScanRoute -> DesignR.drawable.ic_document_scann
        else -> DesignR.drawable.ic_new_note
    }
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
    )
}

@Composable
private fun AppNavigationScaffold(
    modifier: Modifier,
    topLevelBackStack: TopLevelBackStack<TopLevelRoute>,
    externalNavigationCommand: ExternalNavigationCommand?,
    onExternalNavigationHandled: () -> Unit,
    skipInitialExternalNavigationHandling: Boolean,
    onSkipInitialExternalNavigationHandled: () -> Unit,
    recentlyDeletedNoteIds: Set<Int>,
    onRecentlyDeletedNoteIdsChange: (Set<Int>) -> Unit,
    onOpenDrawer: () -> Unit,
    showNavigationIcon: Boolean,
) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                val currentRoute = topLevelBackStack.backStack.lastOrNull()
                if (currentRoute is TopLevelRoute && currentRoute != NotepadRoute && currentRoute !is ChecklistRoute && currentRoute != NotesRoute && currentRoute != CollectionsRoute && currentRoute != ScanRoute) {
                    CapyTopAppBar(
                        title = stringResource(currentRoute.titleRes),
                        onMenuClick = onOpenDrawer,
                        showNavigationIcon = showNavigationIcon,
                    )
                }
            }
        ) { innerPadding ->
            androidx.compose.runtime.LaunchedEffect(externalNavigationCommand) {
                val command = externalNavigationCommand ?: return@LaunchedEffect
                if (skipInitialExternalNavigationHandling) {
                    onSkipInitialExternalNavigationHandled()
                    onExternalNavigationHandled()
                    return@LaunchedEffect
                }

                topLevelBackStack.applyExternalNavigationCommand(command)
                onExternalNavigationHandled()
            }

            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = topLevelBackStack.backStack,
                onBack = { topLevelBackStack.removeLast() },
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(300),
                        initialOffsetX = { it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(300),
                        targetOffsetX = { -it }
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
                predictivePopTransitionSpec = {
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
                            onMenuClick = onOpenDrawer,
                            showNavigationIcon = showNavigationIcon,
                            onNoteSaved = {
                                topLevelBackStack.addTopLevel(NotesRoute)
                            }
                        )
                    }
                    entry<ChecklistRoute> { args ->
                        ChecklistScreen(
                            innerPadding = innerPadding,
                            initialTitle = args.initialTitle,
                            onMenuClick = onOpenDrawer,
                            onChecklistSaved = {
                                topLevelBackStack.addTopLevel(NotesRoute)
                            },
                        )
                    }
                    entry<CollectionsRoute> {
                        CollectionsScreen(
                            innerPadding = innerPadding,
                            onMenuClick = onOpenDrawer,
                            showNavigationIcon = showNavigationIcon,
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
                                onRecentlyDeletedNoteIdsChange(emptySet())
                            },
                            onMenuClick = onOpenDrawer,
                            showNavigationIcon = showNavigationIcon,
                            onNoteClick = { noteId ->
                                topLevelBackStack.addRoute(NotePreviewRoute(noteId, isUnlocked = true))
                            },
                            onAddTextNoteClick = {
                                topLevelBackStack.addTopLevel(NotepadRoute)
                            },
                            onAddChecklistClick = { title ->
                                topLevelBackStack.addTopLevel(ChecklistRoute(initialTitle = title))
                            }
                        )

                    }
                    entry<ScanRoute> {
                        ScanScreen(
                            innerPadding = innerPadding,
                            onMenuClick = onOpenDrawer,
                            showNavigationIcon = showNavigationIcon,
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
                            onMenuClick = onOpenDrawer,
                            showNavigationIcon = showNavigationIcon,
                            onNoteClick = { noteId ->
                                topLevelBackStack.addRoute(NotePreviewRoute(noteId, isUnlocked = true))
                            },
                            onAddTextNoteClick = {
                                topLevelBackStack.addRoute(CreateCollectionNoteRoute(args.collectionId))
                            },
                            onAddChecklistClick = { title ->
                                topLevelBackStack.addRoute(
                                    CreateCollectionChecklistRoute(
                                        collectionId = args.collectionId,
                                        initialTitle = title,
                                    ),
                                )
                            }
                        )
                    }
                    entry<FavoriteNotesRoute> {
                        NotesScreen(
                            innerPadding = innerPadding,
                            favoriteOnly = true,
                            onMenuClick = onOpenDrawer,
                            showNavigationIcon = showNavigationIcon,
                            onNoteClick = { noteId ->
                                topLevelBackStack.addRoute(NotePreviewRoute(noteId, isUnlocked = true))
                            },
                            onAddTextNoteClick = {
                                topLevelBackStack.addTopLevel(NotepadRoute)
                            },
                            onAddChecklistClick = { title ->
                                topLevelBackStack.addTopLevel(ChecklistRoute(initialTitle = title))
                            }
                        )
                    }
                    entry<CreateCollectionNoteRoute> { args ->
                        NotepadScreen(
                            noteId = null,
                            collectionId = args.collectionId,
                            innerPadding = innerPadding,
                            onMenuClick = onOpenDrawer,
                            showNavigationIcon = showNavigationIcon,
                            onNoteSaved = {
                                topLevelBackStack.removeLast()
                            }
                        )
                    }
                    entry<CreateCollectionChecklistRoute> { args ->
                        ChecklistScreen(
                            noteId = null,
                            collectionId = args.collectionId,
                            innerPadding = innerPadding,
                            initialTitle = args.initialTitle,
                            onMenuClick = onOpenDrawer,
                            onChecklistSaved = {
                                topLevelBackStack.removeLast()
                            },
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
                            onAboutClick = {
                                topLevelBackStack.addRoute(AboutRoute)
                            },
                        )
                    }
                    entry<AboutRoute> {
                        AboutScreen(
                            innerPadding = innerPadding,
                            onBack = { topLevelBackStack.removeLast() },
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
                                onRecentlyDeletedNoteIdsChange(setOf(deletedNoteId))
                            },
                            onEditNote = { noteId, isUnlocked ->
                                topLevelBackStack.addRoute(EditNoteRoute(noteId, isUnlocked))
                            },
                            onEditChecklist = { noteId, isUnlocked ->
                                topLevelBackStack.addRoute(EditChecklistRoute(noteId, isUnlocked))
                            }
                        )
                    }
                    entry<EditNoteRoute> { args ->
                        NotepadScreen(
                            noteId = args.noteId,
                            innerPadding = innerPadding,
                            isUnlockedInitially = args.isUnlocked,
                            onMenuClick = onOpenDrawer,
                            showNavigationIcon = showNavigationIcon,
                            onNoteSaved = {
                                topLevelBackStack.removeLast()
                            }
                        )
                    }
                    entry<EditChecklistRoute> { args ->
                        ChecklistScreen(
                            noteId = args.noteId,
                            innerPadding = innerPadding,
                            isUnlockedInitially = args.isUnlocked,
                            onMenuClick = onOpenDrawer,
                            onChecklistSaved = {
                                topLevelBackStack.removeLast()
                            },
                        )
                    }
                }
            )
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
            if (command.collectionId == FAVORITES_COLLECTION_ID) {
                addRoute(FavoriteNotesRoute)
            } else {
                addRoute(
                    CollectionNotesRoute(
                        collectionId = command.collectionId,
                        collectionName = command.collectionName,
                    ),
                )
            }
        }

        is ExternalNavigationCommand.OpenEditRecentNote -> {
            addTopLevel(NotesRoute)
            addRoute(NotePreviewRoute(command.noteId, isUnlocked = true))
        }

        is ExternalNavigationCommand.OpenEditCollectionNote -> {
            addTopLevel(CollectionsRoute)
            if (command.collectionId == FAVORITES_COLLECTION_ID) {
                addRoute(FavoriteNotesRoute)
            } else {
                addRoute(
                    CollectionNotesRoute(
                        collectionId = command.collectionId,
                        collectionName = command.collectionName,
                    ),
                )
            }
            addRoute(NotePreviewRoute(command.noteId, isUnlocked = true))
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

    private fun toSaveableState(): List<String> {
        return buildList {
            add(routeToToken(topLevelKey))
            topLevelStacks.values.forEach { stack ->
                add(stack.joinToString(StackRouteSeparator) { routeToToken(it) })
            }
        }
    }

    companion object {
        val Saver = listSaver<TopLevelBackStack<TopLevelRoute>, String>(
            save = { it.toSaveableState() },
            restore = { savedState -> restoreFromSaveableState(savedState) },
        )

        private const val StackRouteSeparator = "\u001F"

        private fun restoreFromSaveableState(savedState: List<String>): TopLevelBackStack<TopLevelRoute> {
            val restoredStacks = savedState
                .drop(1)
                .mapNotNull { stackToken ->
                    stackToken
                        .split(StackRouteSeparator)
                        .mapNotNull(::routeFromToken)
                        .takeIf { it.firstOrNull() is TopLevelRoute }
                }

            val firstKey = restoredStacks
                .firstOrNull()
                ?.firstOrNull() as? TopLevelRoute ?: NotepadRoute
            val restoredBackStack = TopLevelBackStack(firstKey)

            restoredBackStack.topLevelStacks.clear()
            restoredStacks.forEach { stack ->
                val key = stack.first() as? TopLevelRoute ?: return@forEach
                restoredBackStack.topLevelStacks[key] = mutableStateListOf<Any>().apply {
                    addAll(stack)
                }
            }

            if (restoredBackStack.topLevelStacks.isEmpty()) {
                restoredBackStack.topLevelStacks[NotepadRoute] = mutableStateListOf(NotepadRoute)
            }

            val restoredTopLevelKey = routeFromToken(savedState.firstOrNull().orEmpty()) as? TopLevelRoute
            restoredBackStack.topLevelKey = if (
                restoredTopLevelKey != null &&
                restoredBackStack.topLevelStacks.containsKey(restoredTopLevelKey)
            ) {
                restoredTopLevelKey
            } else {
                restoredBackStack.topLevelStacks.keys.last()
            }
            restoredBackStack.updateBackStack()
            return restoredBackStack
        }

        private fun routeToToken(route: Any): String {
            return when (route) {
                NotepadRoute -> "notepad"
                NotesRoute -> "notes"
                ScanRoute -> "scan"
                CollectionsRoute -> "collections"
                SettingsRoute -> "settings"
                is ChecklistRoute -> "checklist|${route.initialTitle.encodeRoutePart()}"
                is NotePreviewRoute -> "notePreview|${route.noteId}|${route.isUnlocked}"
                is EditNoteRoute -> "editNote|${route.noteId}|${route.isUnlocked}"
                is EditChecklistRoute -> "editChecklist|${route.noteId}|${route.isUnlocked}"
                is CollectionNotesRoute ->
                    "collectionNotes|${route.collectionId}|${route.collectionName.encodeRoutePart()}"
                FavoriteNotesRoute -> "favoriteNotes"
                is CreateCollectionNoteRoute -> "createCollectionNote|${route.collectionId}"
                is CreateCollectionChecklistRoute ->
                    "createCollectionChecklist|${route.collectionId}|${route.initialTitle.encodeRoutePart()}"
                TrashRoute -> "trash"
                BackupRoute -> "backup"
                AboutRoute -> "about"
                WidgetManagementRoute -> "widgetManagement"
                is WidgetConfigurationRoute -> "widgetConfiguration|${route.appWidgetId}"
                else -> "notepad"
            }
        }

        private fun routeFromToken(token: String): Any? {
            val parts = token.split("|")
            return when (parts.firstOrNull()) {
                "notepad" -> NotepadRoute
                "notes" -> NotesRoute
                "scan" -> ScanRoute
                "collections" -> CollectionsRoute
                "settings" -> SettingsRoute
                "checklist" -> ChecklistRoute(initialTitle = parts.getOrNull(1).decodeRoutePart())
                "notePreview" -> NotePreviewRoute(
                    noteId = parts.getIntOrNull(1) ?: return null,
                    isUnlocked = parts.getBooleanOrFalse(2),
                )
                "editNote" -> EditNoteRoute(
                    noteId = parts.getIntOrNull(1) ?: return null,
                    isUnlocked = parts.getBooleanOrFalse(2),
                )
                "editChecklist" -> EditChecklistRoute(
                    noteId = parts.getIntOrNull(1) ?: return null,
                    isUnlocked = parts.getBooleanOrFalse(2),
                )
                "collectionNotes" -> CollectionNotesRoute(
                    collectionId = parts.getIntOrNull(1) ?: return null,
                    collectionName = parts.getOrNull(2).decodeRoutePart(),
                )
                "favoriteNotes" -> FavoriteNotesRoute
                "createCollectionNote" -> CreateCollectionNoteRoute(
                    collectionId = parts.getIntOrNull(1) ?: return null,
                )
                "createCollectionChecklist" -> CreateCollectionChecklistRoute(
                    collectionId = parts.getIntOrNull(1) ?: return null,
                    initialTitle = parts.getOrNull(2).decodeRoutePart(),
                )
                "trash" -> TrashRoute
                "backup" -> BackupRoute
                "about" -> AboutRoute
                "widgetManagement" -> WidgetManagementRoute
                "widgetConfiguration" -> WidgetConfigurationRoute(
                    appWidgetId = parts.getIntOrNull(1) ?: return null,
                )
                else -> null
            }
        }

        private fun String.encodeRoutePart(): String = Uri.encode(this)

        private fun String?.decodeRoutePart(): String = this?.let(Uri::decode).orEmpty()

        private fun List<String>.getIntOrNull(index: Int): Int? = getOrNull(index)?.toIntOrNull()

        private fun List<String>.getBooleanOrFalse(index: Int): Boolean = getOrNull(index)?.toBoolean() ?: false
    }
}
