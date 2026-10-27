package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.UrlUtils
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.TabEntity
import com.example.ui.components.parseColorHex

@Composable
fun TabSwitcherScreen(
    activeProfile: ProfileEntity?,
    isPrivate: Boolean,
    tabs: List<TabEntity>,
    activeTab: TabEntity?,
    onSelectTab: (TabEntity) -> Unit,
    onCloseTab: (TabEntity) -> Unit,
    onCloseAllTabs: () -> Unit,
    onNewTab: (Boolean) -> Unit,
    onDuplicateTab: (TabEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabSegment by remember { mutableIntStateOf(if (isPrivate) 1 else 0) }

    val accentColor = if (isPrivate) Color(0xFF8B5CF6) else parseColorHex(activeProfile?.colorHex ?: "#3B82F6")

    val filteredTabs = tabs.filter {
        searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.url.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp).testTag("tab_switcher_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${activeProfile?.name ?: "Personal"} Tabs (${tabs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (tabs.isNotEmpty()) {
                    IconButton(
                        onClick = onCloseAllTabs,
                        modifier = Modifier.size(40.dp).testTag("close_all_tabs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Close all tabs",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Tab Mode Switcher (Profile Tabs vs Private Tabs)
            TabRow(
                selectedTabIndex = selectedTabSegment,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = accentColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabSegment == 0,
                    onClick = {
                        selectedTabSegment = 0
                        if (isPrivate) onNewTab(false)
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tab, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Profile Tabs")
                        }
                    },
                    modifier = Modifier.testTag("tab_segment_profile")
                )
                Tab(
                    selected = selectedTabSegment == 1,
                    onClick = {
                        selectedTabSegment = 1
                        if (!isPrivate) onNewTab(true)
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Private Tabs")
                        }
                    },
                    modifier = Modifier.testTag("tab_segment_private")
                )
            }

            // Search Bar for Tabs
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search open tabs...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(48.dp)
                    .testTag("search_tabs_input"),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                singleLine = true
            )

            // Grid of Tabs
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp)
            ) {
                items(filteredTabs, key = { it.id }) { tab ->
                    val isActive = tab.id == activeTab?.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectTab(tab) }
                            .then(
                                if (isActive) Modifier.border(2.dp, accentColor, RoundedCornerShape(16.dp))
                                else Modifier.border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            )
                            .testTag("tab_card_${tab.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Tab Card Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = tab.title.ifBlank { "New Tab" },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = { onCloseTab(tab) },
                                    modifier = Modifier.size(26.dp).testTag("close_tab_btn_${tab.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Tab",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Tab Thumbnail / Content Preview Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = UrlUtils.getDisplayHost(tab.url),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Card Footer Action
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isActive) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = accentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                IconButton(
                                    onClick = { onDuplicateTab(tab) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate Tab",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to add new tab
        FloatingActionButton(
            onClick = { onNewTab(selectedTabSegment == 1) },
            containerColor = accentColor,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("floating_new_tab_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Tab", modifier = Modifier.size(28.dp))
        }
    }
}
