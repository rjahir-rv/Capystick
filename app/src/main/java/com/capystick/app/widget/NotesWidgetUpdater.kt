package com.capystick.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object NotesWidgetUpdater {
    suspend fun updateAll(context: Context) {
        NotesWidget().updateAll(context)
    }
}
