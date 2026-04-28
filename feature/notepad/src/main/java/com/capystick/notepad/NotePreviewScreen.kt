package com.capystick.notepad

import android.text.Html
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
import androidx.compose.ui.unit.sp
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
    val note by viewModel.note.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
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
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete note"
                        )
                    }
                    IconButton(onClick = { onEditNote(noteId) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit note"
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
        if (note == null) {
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val plainText = remember(note?.content) {
                        Html.fromHtml(note?.content ?: "", Html.FROM_HTML_MODE_COMPACT).toString().trim()
                    }
                    Text(
                        text = plainText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 28.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
