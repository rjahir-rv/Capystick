package com.capystick.app.widget

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.capystick.app.widget.WidgetNavigationIntents
import com.capystick.core.designsystem.R
import com.capystick.model.WidgetContentState
import com.capystick.model.WidgetMode
import com.capystick.model.WidgetNoteSummary
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.glance.color.ColorProvider as DayNightColorProvider

class NotesWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val entryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java,
            )
        val widgetState =
            withContext(Dispatchers.IO) {
                entryPoint.getWidgetContentUseCase().invoke(appWidgetId)
            }
        val widgetContentFlow = entryPoint.getWidgetContentUseCase().observe(appWidgetId)

        provideContent {
            val currentWidgetState by widgetContentFlow.collectAsState(initial = widgetState)

            WidgetScaffold(
                context = context,
                widgetState = currentWidgetState,
            )
        }
    }
}

@Composable
private fun WidgetScaffold(
    context: Context,
    widgetState: WidgetContentState,
) {
    val palette = rememberWidgetPalette(context)

    Column(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(palette.surfaceContainer)
                .cornerRadius(24.dp)
                .appWidgetBackground(), // ensures proper clipping and background bounds on Android 12+
    ) {
        WidgetHeader(
            context = context,
            title = widgetState.title,
            palette = palette,
        )

        Column(
            modifier =
                GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = GlanceModifier.height(12.dp))

            when (widgetState) {
                is WidgetContentState.Content -> {
                    LazyColumn {
                        items(widgetState.notes) { note ->
                            WidgetNoteRow(
                                context = context,
                                note = note,
                                state = widgetState,
                                palette = palette,
                            )
                        }
                        item {
                            Spacer(modifier = GlanceModifier.height(8.dp))
                            WidgetFooterAction(
                                label = footerLabelFor(widgetState),
                                intent = footerIntentFor(context, widgetState),
                                palette = palette,
                            )
                        }
                    }
                }

                is WidgetContentState.EmptyMissingCollection -> {
                    WidgetEmptyState(
                        message = "La coleccion ya no existe.",
                        buttonLabel = "Reconfigurar",
                        buttonIntent =
                            WidgetNavigationIntents.openWidgetEditor(
                                context = context,
                                appWidgetId = widgetState.configuration.appWidgetId,
                            ),
                        palette = palette,
                    )
                }

                is WidgetContentState.EmptyNoCollections -> {
                    WidgetEmptyState(
                        message = "No hay colecciones disponibles.",
                        buttonLabel = "Abrir colecciones",
                        buttonIntent = WidgetNavigationIntents.openCollections(context),
                        palette = palette,
                    )
                }

                is WidgetContentState.EmptyNoNotes -> {
                    WidgetEmptyState(
                        message =
                            if (widgetState.isCollectionContext) {
                                "Esta coleccion aun no tiene notas."
                            } else {
                                "Aun no tienes notas."
                            },
                        buttonLabel =
                            if (widgetState.isCollectionContext) {
                                "Abrir coleccion"
                            } else {
                                "Crear nota"
                            },
                        buttonIntent = widgetState.toActionIntent(context),
                        palette = palette,
                    )
                }
            }
        }
    }
}

private fun WidgetContentState.EmptyNoNotes.toActionIntent(context: Context): Intent =
    if (isCollectionContext) {
        WidgetNavigationIntents.openCollection(
            context = context,
            collectionId = configuration.collectionId ?: 0,
            collectionName = configuration.collectionName ?: title,
        )
    } else {
        WidgetNavigationIntents.openCreateNote(context)
    }

@Composable
private fun WidgetHeader(
    context: Context,
    title: String,
    palette: WidgetPalette,
) {
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .background(palette.primary),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = GlanceModifier.defaultWeight(),
                style =
                    TextStyle(
                        color = palette.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                maxLines = 1,
            )
            Image(
                provider = ImageProvider(R.drawable.ic_search),
                contentDescription = "Buscar notas",
                colorFilter = ColorFilter.tint(palette.onPrimary),
                modifier =
                    GlanceModifier
                        .size(18.dp)
                        .clickable(actionStartActivity(WidgetNavigationIntents.openNotes(context))),
            )
        }
    }
}

@Composable
private fun WidgetNoteRow(
    context: Context,
    note: WidgetNoteSummary,
    state: WidgetContentState.Content,
    palette: WidgetPalette,
) {
    val clickIntent =
        if (state.configuration.mode == WidgetMode.RECENT_NOTES) {
            WidgetNavigationIntents.editRecentNote(context, note.noteId)
        } else {
            WidgetNavigationIntents.editCollectionNote(
                context = context,
                noteId = note.noteId,
                collectionId = state.configuration.collectionId ?: 0,
                collectionName = state.configuration.collectionName ?: state.title,
            )
        }

    Column(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(clickIntent))
                .padding(bottom = 12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = note.title,
                modifier = GlanceModifier.defaultWeight(),
                maxLines = 1,
                style =
                    TextStyle(
                        color = palette.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    ),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = formatTimestamp(note.timestamp),
                maxLines = 1,
                style =
                    TextStyle(
                        color = palette.onSurfaceVariant,
                        fontSize = 12.sp,
                    ),
            )
        }
        note.preview?.let {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = it,
                maxLines = 1,
                style =
                    TextStyle(
                        color = palette.onSurfaceVariant,
                        fontSize = 13.sp,
                    ),
            )
        }
    }
}

@Composable
private fun WidgetEmptyState(
    message: String,
    buttonLabel: String,
    buttonIntent: Intent,
    palette: WidgetPalette,
) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style =
                    TextStyle(
                        color = palette.onSurfaceVariant,
                        fontSize = 13.sp,
                    ),
            )
            Spacer(modifier = GlanceModifier.height(12.dp))
            Button(
                text = buttonLabel,
                onClick = actionStartActivity(buttonIntent),
                style =
                    TextStyle(
                        color = palette.primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                    ),
            )
        }
    }
}

@Composable
private fun WidgetFooterAction(
    label: String,
    intent: Intent,
    palette: WidgetPalette,
) {
    Text(
        text = label,
        modifier = GlanceModifier.clickable(actionStartActivity(intent)),
        style =
            TextStyle(
                color = palette.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            ),
        maxLines = 1,
    )
}

private fun footerLabelFor(state: WidgetContentState.Content): String =
    if (state.configuration.mode == WidgetMode.RECENT_NOTES) {
        "Ver todo"
    } else {
        "Abrir coleccion"
    }

private fun footerIntentFor(
    context: Context,
    state: WidgetContentState.Content,
): Intent =
    if (state.configuration.mode == WidgetMode.RECENT_NOTES) {
        WidgetNavigationIntents.openNotes(context)
    } else {
        WidgetNavigationIntents.openCollection(
            context = context,
            collectionId = state.configuration.collectionId ?: 0,
            collectionName = state.configuration.collectionName ?: state.title,
        )
    }

@Composable
private fun rememberWidgetPalette(context: Context): WidgetPalette {
    val lightScheme: ColorScheme
    val darkScheme: ColorScheme

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        lightScheme = dynamicLightColorScheme(context)
        darkScheme = dynamicDarkColorScheme(context)
    } else {
        lightScheme = lightColorScheme()
        darkScheme = darkColorScheme()
    }

    return WidgetPalette(
        surfaceContainer = DayNightColorProvider(lightScheme.surfaceContainer, darkScheme.surfaceContainer),
        onSurface = DayNightColorProvider(lightScheme.onSurface, darkScheme.onSurface),
        onSurfaceVariant = DayNightColorProvider(lightScheme.onSurfaceVariant, darkScheme.onSurfaceVariant),
        primary = DayNightColorProvider(lightScheme.primary, darkScheme.primary),
        onPrimary = DayNightColorProvider(lightScheme.onPrimary, darkScheme.onPrimary),
    )
}

private data class WidgetPalette(
    val surfaceContainer: ColorProvider,
    val onSurface: ColorProvider,
    val onSurfaceVariant: ColorProvider,
    val primary: ColorProvider,
    val onPrimary: ColorProvider,
)

private fun formatTimestamp(timestamp: Long): String =
    TIMESTAMP_FORMATTER.format(
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()),
    )

private val TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM")
