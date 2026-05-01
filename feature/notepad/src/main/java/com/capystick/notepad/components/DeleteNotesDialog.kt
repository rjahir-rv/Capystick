package com.capystick.notepad.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun DeleteNotesDialog(
    selectedCount: Int,
    isRemovingFromCollection: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isRemovingFromCollection) "Quitar de la colección" else "Eliminar notas",
            )
        },
        text = {
            Text(
                text = if (isRemovingFromCollection) {
                    "¿Estás seguro de que deseas quitar las $selectedCount notas de esta colección? Seguirán estando disponibles en 'Todas las notas'."
                } else {
                    "¿Estás seguro de que deseas eliminar las $selectedCount notas seleccionadas?"
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(
                    text = if (isRemovingFromCollection) "Quitar" else "Eliminar",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
    )
}
