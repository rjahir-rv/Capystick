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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.capystick.collections.CollectionsScreen
import com.capystick.notepad.NotepadScreen
import com.capystick.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val levelRoutes = listOf(NotepadRoute, CollectionsRoute, SettingsRoute)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val topLevelBackStack = remember { TopLevelBackStack<TopLevelRoute>(NotepadRoute) }
    val selectedRoute =  topLevelBackStack.topLevelKey

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
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
                            val iconRes = when(item.title){
                                "Crear nota" -> painterResource(com.capystick.core.designsystem.R.drawable.ic_new_note)
                                "Colecciones" -> painterResource(com.capystick.core.designsystem.R.drawable.ic_collection)
                                "Ajustes" -> painterResource(com.capystick.core.designsystem.R.drawable.ic_settings)
                                else -> null
                            }
                            Icon(painter = iconRes!!, contentDescription = item.title)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                if (selectedRoute != NotepadRoute) {
                    CapyTopAppBar(
                        title = selectedRoute.title,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }
            }
        ) { innerPadding ->
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
                            }
                        )
                    }
                    entry<CollectionsRoute> {
                        CollectionsScreen(innerPadding = innerPadding)
                    }
                    entry<SettingsRoute> {
                        SettingsScreen(innerPadding = innerPadding)
                    }
                }
            )
        }
    }
}

class TopLevelBackStack<T: TopLevelRoute>(startKey: T) {
    private var topLevelStacks : LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    fun addTopLevel(key: T){
        if (topLevelStacks[key] == null){
            topLevelStacks[key] = mutableStateListOf(key)
        } else {
            topLevelStacks.apply {
                remove(key)?.let { put(key, it) }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T){
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast(){
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        topLevelStacks.remove(removedKey)
        if (topLevelStacks.isNotEmpty()) {
            topLevelKey = topLevelStacks.keys.last()
            updateBackStack()
        } else {
            backStack.clear()
        }
    }
}
