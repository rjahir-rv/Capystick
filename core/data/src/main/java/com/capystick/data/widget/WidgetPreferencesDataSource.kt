package com.capystick.data.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.capystick.model.WidgetConfiguration
import com.capystick.model.WidgetMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "capystick_widget_preferences",
)

@Singleton
class WidgetPreferencesDataSource @Inject constructor(
    @param : ApplicationContext
    private val context: Context,
) {
    fun getWidgetConfigurations(): Flow<List<WidgetConfiguration>> = context.widgetDataStore.data.map { preferences ->
        preferences.asMap().keys
            .mapNotNull { key ->
                key.name.removePrefix(WIDGET_MODE_PREFIX).takeIf { key.name.startsWith(WIDGET_MODE_PREFIX) }
            }
            .mapNotNull(String::toIntOrNull)
            .sorted()
            .mapNotNull { appWidgetId -> preferences.toWidgetConfiguration(appWidgetId) }
    }

    suspend fun getWidgetConfiguration(appWidgetId: Int): WidgetConfiguration? {
        val preferences = context.widgetDataStore.data.first()
        return preferences.toWidgetConfiguration(appWidgetId)
    }

    suspend fun saveWidgetConfiguration(configuration: WidgetConfiguration) {
        context.widgetDataStore.edit { preferences ->
            preferences[widgetModeKey(configuration.appWidgetId)] = configuration.mode.name
            configuration.collectionId?.let {
                preferences[widgetCollectionIdKey(configuration.appWidgetId)] = it
            } ?: preferences.remove(widgetCollectionIdKey(configuration.appWidgetId))
            configuration.collectionName?.let {
                preferences[widgetCollectionNameKey(configuration.appWidgetId)] = it
            } ?: preferences.remove(widgetCollectionNameKey(configuration.appWidgetId))
        }
    }

    suspend fun deleteWidgetConfiguration(appWidgetId: Int) {
        context.widgetDataStore.edit { preferences ->
            preferences.remove(widgetModeKey(appWidgetId))
            preferences.remove(widgetCollectionIdKey(appWidgetId))
            preferences.remove(widgetCollectionNameKey(appWidgetId))
        }
    }

    private fun Preferences.toWidgetConfiguration(appWidgetId: Int): WidgetConfiguration? {
        val modeName = this[widgetModeKey(appWidgetId)] ?: return null
        val mode = WidgetMode.entries.firstOrNull { it.name == modeName } ?: return null
        return WidgetConfiguration(
            appWidgetId = appWidgetId,
            mode = mode,
            collectionId = this[widgetCollectionIdKey(appWidgetId)],
            collectionName = this[widgetCollectionNameKey(appWidgetId)],
        )
    }

    private companion object {
        const val WIDGET_MODE_PREFIX = "widget_mode_"

        fun widgetModeKey(appWidgetId: Int) = stringPreferencesKey("$WIDGET_MODE_PREFIX$appWidgetId")
        fun widgetCollectionIdKey(appWidgetId: Int) =
            intPreferencesKey("widget_collection_id_$appWidgetId")

        fun widgetCollectionNameKey(appWidgetId: Int) =
            stringPreferencesKey("widget_collection_name_$appWidgetId")
    }
}
