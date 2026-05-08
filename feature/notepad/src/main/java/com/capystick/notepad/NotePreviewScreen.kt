package com.capystick.notepad

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.core.designsystem.R
import com.capystick.designsystem.components.rememberBiometricAuthenticator
import com.capystick.notepad.util.noteContentToPlainText
import com.capystick.notepad.viewmodel.NotePreviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePreviewScreen(
    modifier: Modifier = Modifier,
    noteId: Int,
    innerPadding: PaddingValues,
    isUnlockedInitially: Boolean = false,
    onBack: () -> Unit,
    onEditNote: (Int, Boolean) -> Unit = { _, _ -> },
    viewModel: NotePreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val note = uiState.note
    val authenticator = rememberBiometricAuthenticator()
    var isUnlocked by rememberSaveable(noteId) { mutableStateOf(isUnlockedInitially) }

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
                    Text(
                        text = note?.title ?: "Cargando...",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                        )
                    }
                },
                actions = {
                    if (note != null) {
                        IconButton(onClick = { viewModel.toggleFavorite(note) }) {
                            Icon(
                                painter = painterResource(
                                    id = if (note.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite,
                                ),
                                contentDescription = if (note.isFavorite) "Quitar de favoritas" else "Agregar a favoritas",
                                tint = if (note.isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }

                        IconButton(
                            onClick = {
                                if (note.isSecure && !authenticator.isDeviceSecure()) {
                                    Toast.makeText(
                                        context,
                                        "El telefono ya no tiene bloqueo configurado",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                } else {
                                    authenticator.authenticate(
                                        title = if (note.isSecure) "Desbloquear nota" else "Bloquear nota",
                                        subtitle = if (note.isSecure) {
                                            "Autenticate para quitar el bloqueo"
                                        } else {
                                            "Autenticate para bloquear esta nota"
                                        },
                                        onSuccess = { viewModel.toggleSecure(note) },
                                        onError = { /* Toast is handled by the authenticator when needed. */ },
                                    )
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (note.isSecure) R.drawable.ic_lock else R.drawable.ic_lock_open,
                                ),
                                contentDescription = if (note.isSecure) "Quitar proteccion" else "Proteger nota",
                                tint = if (note.isSecure) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_more_vert),
                                    contentDescription = "Mas opciones",
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Compartir") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_share),
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        if (note.isSecure && !isUnlocked) {
                                            Toast.makeText(
                                                context,
                                                "Desbloquea la nota para compartirla",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                            return@DropdownMenuItem
                                        }

                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "${note.title}\n\n${noteContentToPlainText(note.content)}",
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartir nota"))
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_delete),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar nota") },
                text = { Text("Estas seguro de enviar a la papelera esta nota?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            note?.let {
                                viewModel.deleteNote(it) {
                                    showDeleteDialog = false
                                    onBack()
                                }
                            }
                        },
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                },
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Cargando nota...")
            }
        } else if (note != null) {
            val deviceSecure = authenticator.isDeviceSecure()
            val needsRecovery = note.isSecure && !deviceSecure
            val needsUnlock = note.isSecure && !isUnlocked && deviceSecure

            when {
                needsRecovery -> {
                    LockedNotePrompt(
                        message = "El bloqueo del telefono esta desactivado",
                        actionLabel = "Quitar bloqueo y abrir",
                        onUnlock = {
                            viewModel.toggleSecure(note)
                            isUnlocked = true
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(scaffoldPadding),
                    )
                }

                needsUnlock -> {
                    LockedNotePrompt(
                        message = "Esta nota esta protegida",
                        actionLabel = "Desbloquear",
                        onUnlock = {
                            authenticator.authenticate(
                                title = "Desbloquear nota",
                                subtitle = "Autenticate para ver el contenido",
                                onSuccess = { isUnlocked = true },
                                onError = { /* Toast */ },
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(scaffoldPadding),
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(scaffoldPadding)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            onClick = { onEditNote(noteId, isUnlocked) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            val plainText = remember(note.content) {
                                noteContentToPlainText(note.content)
                            }
                            Text(
                                text = plainText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LockedNotePrompt(
    message: String,
    actionLabel: String,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onUnlock) {
                Text(actionLabel)
            }
        }
    }
}


