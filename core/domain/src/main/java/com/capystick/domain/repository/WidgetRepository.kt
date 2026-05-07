package com.capystick.domain.repository

import com.capystick.model.WidgetConfiguration
import kotlinx.coroutines.flow.Flow

interface WidgetRepository {
    fun getWidgetConfigurations(): Flow<List<WidgetConfiguration>>
    suspend fun getWidgetConfiguration(appWidgetId: Int): WidgetConfiguration?
    suspend fun saveWidgetConfiguration(configuration: WidgetConfiguration)
    suspend fun deleteWidgetConfiguration(appWidgetId: Int)
}
