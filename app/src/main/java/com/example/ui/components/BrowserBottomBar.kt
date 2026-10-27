package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.ripple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun BrowserBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onTabs: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.8.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavAction(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "Back",
                enabled = canGoBack,
                onClick = onBack,
                testTag = "bottom_nav_back"
            )
            BottomNavAction(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                label = "Forward",
                enabled = canGoForward,
                onClick = onForward,
                testTag = "bottom_nav_forward"
            )
            BottomNavAction(
                icon = Icons.Default.Home,
                label = "Home",
                enabled = true,
                onClick = onHome,
                testTag = "bottom_nav_home"
            )
            BottomNavAction(
                icon = Icons.Default.Tab,
                label = "Tabs",
                enabled = true,
                onClick = onTabs,
                testTag = "bottom_nav_tabs"
            )
            BottomNavAction(
                icon = Icons.Default.MoreVert,
                label = "Menu",
                enabled = true,
                onClick = onMenu,
                testTag = "bottom_nav_menu"
            )
        }
    }
}

@Composable
private fun BottomNavAction(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .size(48.dp)
            .testTag(testTag)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
