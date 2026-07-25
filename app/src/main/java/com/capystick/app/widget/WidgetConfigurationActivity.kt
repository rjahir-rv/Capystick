package com.capystick.app.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import com.capystick.app.CapystickAppThemeContent
import com.capystick.app.R
import com.capystick.widget.WidgetConfigurationScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetConfigurationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId =
            intent?.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setResult(RESULT_CANCELED)

        setContent {
            CapystickAppThemeContent {
                WidgetConfigurationScreen(
                    appWidgetId = appWidgetId,
                    title = getString(R.string.widget_configuration_title),
                    innerPadding = PaddingValues(),
                    onBack = ::finish,
                    onSaved = {
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
                        )
                        finish()
                    },
                )
            }
        }
    }
}
