package com.capystick.data.widget

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefreshRequester @Inject constructor(
    @param: ApplicationContext private val context: Context,
) {
    fun requestRefresh() {
        context.sendBroadcast(
            Intent(WidgetRefreshContract.ACTION_REFRESH_WIDGETS)
                .setClassName(
                    context.packageName,
                    WidgetRefreshContract.REFRESH_RECEIVER_CLASS_NAME,
                ),
        )
    }
}
