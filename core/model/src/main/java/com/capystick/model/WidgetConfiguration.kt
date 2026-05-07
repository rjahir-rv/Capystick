package com.capystick.model

data class WidgetConfiguration(
    val appWidgetId: Int,
    val mode: WidgetMode,
    val collectionId: Int? = null,
    val collectionName: String? = null,
)
