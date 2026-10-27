package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMoreMenu(
    isBookmarked: Boolean,
    isDesktopSite: Boolean,
    isReaderMode: Boolean,
    onNewTab: () -> Unit,
    onNewPrivateTab: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onSwitchProfile: () -> Unit,
    onFindInPage: () -> Unit,
    onToggleReader: () -> Unit,
    onToggleDesktop: () -> Unit,
    onShare: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Group 1: Tab & Profiles
            MoreMenuItem(
                icon = Icons.Default.Add,
                label = "New Tab",
                onClick = onNewTab,
                testTag = "menu_new_tab"
            )
            MoreMenuItem(
                icon = Icons.Default.Security,
                label = "New Incognito Tab",
                onClick = onNewPrivateTab,
                testTag = "menu_new_private_tab"
            )
            MoreMenuItem(
                icon = Icons.Default.ManageAccounts,
                label = "Switch / Manage Profiles",
                onClick = onSwitchProfile,
                testTag = "menu_switch_profile"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Group 2: Library
            MoreMenuItem(
                icon = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = if (isBookmarked) "Remove Bookmark" else "Bookmark Page",
                onClick = onToggleBookmark,
                testTag = "menu_toggle_bookmark"
            )
            MoreMenuItem(
                icon = Icons.Default.Bookmark,
                label = "Bookmarks",
                onClick = onOpenBookmarks,
                testTag = "menu_bookmarks"
            )
            MoreMenuItem(
                icon = Icons.Default.History,
                label = "Browsing History",
                onClick = onOpenHistory,
                testTag = "menu_history"
            )
            MoreMenuItem(
                icon = Icons.Default.Download,
                label = "Downloads",
                onClick = onOpenDownloads,
                testTag = "menu_downloads"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Group 3: Page Actions
            MoreMenuItem(
                icon = Icons.Default.FindInPage,
                label = "Find in Page",
                onClick = onFindInPage,
                testTag = "menu_find_in_page"
            )
            MoreMenuItem(
                icon = Icons.Default.AutoStories,
                label = if (isReaderMode) "Exit Reader Mode" else "Reader Mode",
                onClick = onToggleReader,
                testTag = "menu_reader_mode"
            )

            // Desktop Site Checkbox Item
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggleDesktop)
                    .testTag("menu_desktop_site"),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Desktop Site",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = isDesktopSite,
                        onCheckedChange = { onToggleDesktop() }
                    )
                }
            }

            MoreMenuItem(
                icon = Icons.Default.Share,
                label = "Share Link",
                onClick = onShare,
                testTag = "menu_share"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Group 4: Settings & About
            MoreMenuItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = onOpenSettings,
                testTag = "menu_settings"
            )
            MoreMenuItem(
                icon = Icons.Default.Info,
                label = "About Prism Browser",
                onClick = onOpenAbout,
                testTag = "menu_about"
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
