package com.capystick.notepad

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import com.capystick.core.designsystem.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.capystick.notepad.util.noteContentToPlainText
import com.capystick.notepad.viewmodel.NotePreviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePreviewScreen(
    modifier: Modifier = Modifier,
    noteId: Int,
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    onEditNote: (Int) -> Unit = {},
    viewModel: NotePreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val note = uiState.note

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    LaunchedEffect(uiState.noteMissing) {
        if (uiState.noteMissing) {
            onBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(text = note?.title ?: "Cargando...")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Icon back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = note != null,
                        onClick = {
                            note?.let(viewModel::toggleFavorite)
                        }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (note?.isFavorite == true) {
                                    R.drawable.ic_favorite_filled
                                } else {
                                    R.drawable.ic_favorite
                                }
                            ),
                            contentDescription = if (note?.isFavorite == true) {
                                "Quitar de favoritas"
                            } else {
                                "Agregar a favoritas"
                            },
                            tint = if (note?.isFavorite == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete note"
                        )
                    }
                    IconButton(onClick = {
                        note?.let {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${it.title}\n\n${noteContentToPlainText(it.content)}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir nota"))
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = "Share note"
                        )
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar nota") },
                text = { Text("¿Estás seguro de que deseas eliminar esta nota? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        note?.let {
                            viewModel.deleteNote(it) {
                                showDeleteDialog = false
                                onBack()
                            }
                        }
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
        if (uiState.isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding), contentAlignment = Alignment.Center) {
                Text("Cargando nota...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    onClick = { onEditNote(noteId) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val plainText = remember(note?.content) {
                        noteContentToPlainText(note?.content ?: "")
                    }
                    Text(
                        text = plainText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.background
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
