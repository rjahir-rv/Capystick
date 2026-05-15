package com.capystick.checklist.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.capystick.checklist.R
import com.capystick.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChecklistTopBar(
    title: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title.ifBlank { stringResource(R.string.checklist_default_title) },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_content_description),
                )
            }
        },
        actions = {
            IconButton(onClick = onSaveClick) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_save),
                    contentDescription = stringResource(R.string.save_checklist_content_description),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
        ),
    )
}
