package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.FindInPageState
import com.example.browser.SecurityStatus
import com.example.browser.UrlUtils
import com.example.data.local.entity.ProfileEntity

@Composable
fun BrowserTopBar(
    activeProfile: ProfileEntity?,
    isPrivate: Boolean,
    currentUrl: String,
    openTabsCount: Int,
    isLoading: Boolean,
    loadingProgress: Int,
    findInPage: FindInPageState,
    onProfileClick: () -> Unit,
    onUrlClick: () -> Unit,
    onReloadClick: () -> Unit,
    onTabsClick: () -> Unit,
    onMoreClick: () -> Unit,
    onFindQueryChange: (String) -> Unit,
    onFindNext: (Boolean) -> Unit,
    onCloseFind: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressAnimated by animateFloatAsState(
        targetValue = if (isLoading) (loadingProgress.coerceIn(5, 100) / 100f) else 0f,
        label = "web_progress"
    )

    val securityStatus = UrlUtils.getSecurityStatus(currentUrl)
    val displayHost = UrlUtils.getDisplayHost(currentUrl)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Find In Page Toolbar if active
        AnimatedVisibility(visible = findInPage.active) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = findInPage.query,
                        onValueChange = onFindQueryChange,
                        placeholder = { Text("Find in page...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("find_input_field"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (findInPage.totalMatches > 0) {
                        Text(
                            text = "${findInPage.currentIndex + 1}/${findInPage.totalMatches}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { onFindNext(false) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match")
                    }

                    IconButton(
                        onClick = { onFindNext(true) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match")
                    }

                    IconButton(
                        onClick = onCloseFind,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close find")
                    }
                }
            }
        }

        // Main Browser App Bar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Profile Switcher Badge
            ProfileAvatarBadge(
                profile = activeProfile,
                isPrivate = isPrivate,
                onClick = onProfileClick
            )

            // Address & Search Omnibox Pill
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .clickable(onClick = onUrlClick)
                    .testTag("address_bar_pill"),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(21.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (securityStatus) {
                        SecurityStatus.SECURE -> Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure Connection",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(15.dp)
                        )
                        SecurityStatus.NOT_SECURE -> Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Not Secure",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(15.dp)
                        )
                        SecurityStatus.INTERNAL -> Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = displayHost,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = if (securityStatus == SecurityStatus.INTERNAL) FontWeight.Normal else FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onReloadClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("reload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload page",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Tab Counter Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable(onClick = onTabsClick)
                    .testTag("tab_counter_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = openTabsCount.coerceAtLeast(1).toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Overflow Three-Dot Menu Button
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("three_dot_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Browser Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Loading Progress Bar
        if (isLoading && progressAnimated > 0f) {
            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = parseColorHex(activeProfile?.colorHex ?: "#3B82F6"),
                trackColor = Color.Transparent
            )
        } else {
            Spacer(modifier = Modifier.height(2.5.dp))
        }
    }
}
