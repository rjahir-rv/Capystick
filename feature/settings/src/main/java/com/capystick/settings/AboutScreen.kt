package com.capystick.settings

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capystick.core.designsystem.R as DesignR

private const val GitHubUrl = "https://github.com/rjahir-rv/Capystick"
private const val ContactEmail = "support.imaginarydeer@proton.me"
private const val ContactEmailUri = "mailto:$ContactEmail"
private const val ReportIssueSubject = "Reporte de problema - Capystick"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val uriHandler = LocalUriHandler.current
    val versionName = remember(context) { context.appVersionName() }
    val reportIssueUri = remember(versionName) { buildReportIssueUri(versionName) }
    val features = remember { aboutFeatures() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = DesignR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_content_description),
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = innerPadding.calculateBottomPadding(),
                ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                AboutHeader()
            }

            item {
                SectionTitle(text = stringResource(R.string.about_features_title))
            }

            item {
                FeaturesCard(features = features)
            }

            item {
                SectionTitle(text = stringResource(R.string.about_quick_actions_title))
            }

            item {
                OutlinedButton(
                    onClick = { uriHandler.openUri(GitHubUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        painter = painterResource(id = DesignR.drawable.ic_github),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.about_github_action))
                }
            }

            item {
                OutlinedButton(
                    onClick = { uriHandler.openUri(ContactEmailUri) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        painter = painterResource(id = DesignR.drawable.ic_email),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.about_contact_developer_action))
                }
            }

            item {
                OutlinedButton(
                    onClick = { uriHandler.openUri(reportIssueUri) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        painter = painterResource(id = DesignR.drawable.ic_report_problem),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.about_report_issue_action))
                }
            }

            item {
                VersionCard(versionName = versionName)
            }
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun AboutHeader(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(id = DesignR.drawable.capystick_logo),
                contentDescription = stringResource(R.string.about_logo_content_description),
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .height(64.dp),
                alignment = Alignment.CenterStart,
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FeaturesCard(
    features: List<AboutFeature>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            features.forEachIndexed { index, feature ->
                FeatureRow(
                    feature = feature,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (index != features.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 64.dp),
                        color = DividerDefaults.color.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    feature: AboutFeature,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FeatureIcon(iconRes = feature.iconRes)
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(feature.titleRes),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(feature.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FeatureIcon(
    iconRes: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun VersionCard(
    versionName: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FeatureIcon(iconRes = DesignR.drawable.ic_info)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = stringResource(R.string.about_version_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.about_version_value, versionName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun aboutFeatures(): List<AboutFeature> = listOf(
    AboutFeature(
        iconRes = DesignR.drawable.ic_checklist,
        titleRes = R.string.about_feature_notes_title,
        descriptionRes = R.string.about_feature_notes_description,
    ),
    AboutFeature(
        iconRes = DesignR.drawable.ic_document_scann,
        titleRes = R.string.about_feature_ocr_title,
        descriptionRes = R.string.about_feature_ocr_description,
    ),
    AboutFeature(
        iconRes = DesignR.drawable.ic_collection,
        titleRes = R.string.about_feature_organization_title,
        descriptionRes = R.string.about_feature_organization_description,
    ),
    AboutFeature(
        iconRes = DesignR.drawable.ic_lock,
        titleRes = R.string.about_feature_privacy_title,
        descriptionRes = R.string.about_feature_privacy_description,
    ),
    AboutFeature(
        iconRes = DesignR.drawable.ic_palette,
        titleRes = R.string.about_feature_customization_title,
        descriptionRes = R.string.about_feature_customization_description,
    ),
)

private data class AboutFeature(
    val iconRes: Int,
    val titleRes: Int,
    val descriptionRes: Int,
)

private fun buildReportIssueUri(versionName: String): String {
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

    return "mailto:$ContactEmail" +
        "?subject=${Uri.encode(ReportIssueSubject)}" +
        "&body=${Uri.encode(body)}"
}

@Suppress("DEPRECATION")
private fun Context.appVersionName(): String =
    runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank {
        "0.9.0"
    }
