package com.capystick.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorShowcase(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Primary colors
        ColorGroup(
            title = "Primary",
            colors = listOf(
                "Primary" to MaterialTheme.colorScheme.primary,
                "OnPrimary" to MaterialTheme.colorScheme.onPrimary,
                "PrimaryContainer" to MaterialTheme.colorScheme.primaryContainer,
                "OnPrimaryContainer" to MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        )

        // Secondary colors
        ColorGroup(
            title = "Secondary",
            colors = listOf(
                "Secondary" to MaterialTheme.colorScheme.secondary,
                "OnSecondary" to MaterialTheme.colorScheme.onSecondary,
                "SecondaryContainer" to MaterialTheme.colorScheme.secondaryContainer,
                "OnSecondaryContainer" to MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        )

        // Tertiary colors
        ColorGroup(
            title = "Tertiary",
            colors = listOf(
                "Tertiary" to MaterialTheme.colorScheme.tertiary,
                "OnTertiary" to MaterialTheme.colorScheme.onTertiary,
                "TertiaryContainer" to MaterialTheme.colorScheme.tertiaryContainer,
                "OnTertiaryContainer" to MaterialTheme.colorScheme.onTertiaryContainer,
            ),
        )

        // Error colors
        ColorGroup(
            title = "Error",
            colors = listOf(
                "Error" to MaterialTheme.colorScheme.error,
                "OnError" to MaterialTheme.colorScheme.onError,
                "ErrorContainer" to MaterialTheme.colorScheme.errorContainer,
                "OnErrorContainer" to MaterialTheme.colorScheme.onErrorContainer,
            ),
        )

        // Surface colors
        ColorGroup(
            title = "Surface",
            colors = listOf(
                "Surface" to MaterialTheme.colorScheme.surface,
                "OnSurface" to MaterialTheme.colorScheme.onSurface,
                "SurfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
                "OnSurfaceVariant" to MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        // Background colors
        ColorGroup(
            title = "Background",
            colors = listOf(
                "Background" to MaterialTheme.colorScheme.background,
                "OnBackground" to MaterialTheme.colorScheme.onBackground,
            ),
        )

        // Outline colors
        ColorGroup(
            title = "Outline",
            colors = listOf(
                "Outline" to MaterialTheme.colorScheme.outline,
                "OutlineVariant" to MaterialTheme.colorScheme.outlineVariant,
            ),
        )

        // Surface container colors
        ColorGroup(
            title = "Surface Containers",
            colors = listOf(
                "SurfaceContainerLowest" to MaterialTheme.colorScheme.surfaceContainerLowest,
                "SurfaceContainerLow" to MaterialTheme.colorScheme.surfaceContainerLow,
                "SurfaceContainer" to MaterialTheme.colorScheme.surfaceContainer,
                "SurfaceContainerHigh" to MaterialTheme.colorScheme.surfaceContainerHigh,
                "SurfaceContainerHighest" to MaterialTheme.colorScheme.surfaceContainerHighest,
            ),
        )
    }
}

@Composable
private fun ColorGroup(
    title: String,
    colors: List<Pair<String, Color>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            colors.forEach { (name, color) ->
                ColorSwatch(name = name, color = color)
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                color = color,
                shape = MaterialTheme.shapes.small,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isColorLight(color)) {
                    Color.Black
                } else {
                    Color.White
                },
            )
        }
    }
}

private fun isColorLight(color: Color): Boolean {
    val red = color.red * 255
    val green = color.green * 255
    val blue = color.blue * 255
    val brightness = (red * 299 + green * 587 + blue * 114) / 1000
    return brightness > 128
}

