package com.capystick.app

import android.app.Application
import com.capystick.app.widget.WidgetAutoRefreshManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CapystickApplication : Application() {
    @Inject
    lateinit var widgetAutoRefreshManager: WidgetAutoRefreshManager

    override fun onCreate() {
        super.onCreate()
        widgetAutoRefreshManager.start()
    }
}
