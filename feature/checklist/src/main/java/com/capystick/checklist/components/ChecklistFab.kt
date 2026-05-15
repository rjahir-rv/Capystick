package com.capystick.checklist.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capystick.checklist.R
import com.capystick.core.designsystem.R as DesignR

@Composable
internal fun ChecklistFab(
    onClick: () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(18.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp),
    ) {
        Icon(
            painter = painterResource(id = DesignR.drawable.ic_add),
            contentDescription = stringResource(R.string.add_item_content_description),
            modifier = Modifier.size(32.dp),
        )
    }
}
