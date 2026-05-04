package com.capystick.notepad.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capystick.core.designsystem.R
import com.capystick.model.Collection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionSheet(
    collections: List<Collection>,
    onDismiss: () -> Unit,
    onCollectionSelected: (Int) -> Unit,
    onCreateCollection: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        CollectionAssignmentContent(
            collections = collections,
            onCollectionSelected = { collectionId ->
                onCollectionSelected(collectionId)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    onDismiss()
                }
            },
            onCreateCollection = { name ->
                onCreateCollection(name)
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    onDismiss()
                }
            },
        )
    }
}

@Composable
private fun CollectionAssignmentContent(
    collections: List<Collection>,
    onCollectionSelected: (Int) -> Unit,
    onCreateCollection: (String) -> Unit,
) {
    var newCollectionName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Añadir a colección",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        if (collections.isEmpty()) {
            Text(text = "No tienes colecciones. Crea una nueva:")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(collections) { collection ->
                    Surface(
                        onClick = { onCollectionSelected(collection.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = collection.name,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
            Text(text = "O crea una nueva:")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = newCollectionName,
                onValueChange = { newCollectionName = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nombre de la colección") },
                singleLine = true,
            )
            IconButton(
                onClick = {
                    if (newCollectionName.isNotBlank()) {
                        onCreateCollection(newCollectionName)
                    }
                },
                enabled = newCollectionName.isNotBlank(),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_save),
                    contentDescription = "Crear",
                    tint = if (newCollectionName.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
                )
            }
        }
    }
}
