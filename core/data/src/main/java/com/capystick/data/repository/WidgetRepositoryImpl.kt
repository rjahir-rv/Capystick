package com.capystick.data.repository

import com.capystick.data.widget.WidgetPreferencesDataSource
import com.capystick.data.widget.WidgetRefreshRequester
import com.capystick.domain.repository.WidgetRepository
import com.capystick.model.WidgetConfiguration
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WidgetRepositoryImpl @Inject constructor(
    private val preferencesDataSource: WidgetPreferencesDataSource,
    private val widgetRefreshRequester: WidgetRefreshRequester,
) : WidgetRepository {
    override fun getWidgetConfigurations(): Flow<List<WidgetConfiguration>> =
        preferencesDataSource.getWidgetConfigurations()

    override suspend fun getWidgetConfiguration(appWidgetId: Int): WidgetConfiguration? =
        preferencesDataSource.getWidgetConfiguration(appWidgetId)

    override suspend fun saveWidgetConfiguration(configuration: WidgetConfiguration) {
        preferencesDataSource.saveWidgetConfiguration(configuration)
        widgetRefreshRequester.requestRefresh()
    }

    override suspend fun deleteWidgetConfiguration(appWidgetId: Int) {
        preferencesDataSource.deleteWidgetConfiguration(appWidgetId)
        widgetRefreshRequester.requestRefresh()
    }
}
