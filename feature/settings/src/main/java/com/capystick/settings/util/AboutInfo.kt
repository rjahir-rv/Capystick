package com.capystick.settings.util

import android.content.Context
import android.net.Uri
import android.os.Build

internal const val ABOUT_GITHUB_URL = "https://github.com/rjahir-rv/Capystick"
internal const val ABOUT_CONTACT_EMAIL_URI = "mailto:support.imaginarydeer@proton.me"

private const val CONTACT_EMAIL = "support.imaginarydeer@proton.me"
private const val REPORT_ISSUE_SUBJECT = "Reporte de problema - Capystick"

internal fun buildReportIssueUri(versionName: String): String {
    val device = listOf(
        Build.MANUFACTURER,
        Build.MODEL,
    ).joinToString(separator = " ").trim()
    val body = """
        Describe el problema:


        Pasos para reproducirlo:
        1.
        2.
        3.

        Informacion del dispositivo:
        - App: $versionName
        - Dispositivo: $device
        - Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
    """.trimIndent()

    return "mailto:$CONTACT_EMAIL" +
        "?subject=${Uri.encode(REPORT_ISSUE_SUBJECT)}" +
        "&body=${Uri.encode(body)}"
}

@Suppress("DEPRECATION")
internal fun Context.appVersionName(): String =
    runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank {
        "0.9.0"
    }
