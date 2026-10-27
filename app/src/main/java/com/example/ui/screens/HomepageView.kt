package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ShortcutEntity
import com.example.ui.components.getProfileIconVector
import com.example.ui.components.parseColorHex

@Composable
fun HomepageView(
    activeProfile: ProfileEntity?,
    isPrivate: Boolean,
    shortcuts: List<ShortcutEntity>,
    bookmarks: List<BookmarkEntity>,
    onSearchClick: () -> Unit,
    onUrlSelect: (String) -> Unit,
    onAddShortcut: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val profileColor = if (isPrivate) Color(0xFF8B5CF6) else parseColorHex(activeProfile?.colorHex ?: "#3B82F6")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero / Profile Welcome Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("homepage_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    profileColor.copy(alpha = 0.22f),
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(profileColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPrivate) Icons.Default.Security else getProfileIconVector(activeProfile?.avatarIcon ?: "person"),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (isPrivate) "Incognito Session" else "Prism • ${activeProfile?.name ?: "Personal"}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isPrivate) "Cookies & history are completely isolated & ephemeral" else "Engine: Android WebView • Isolated Profile Vault",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Large Centered Search Pill
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .clickable(onClick = onSearchClick)
                                .testTag("homepage_search_pill"),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 3.dp,
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = profileColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Search with ${activeProfile?.searchEngine ?: "Google"} or enter URL",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan QR",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Speed Dial Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speed Dial",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_shortcut_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Shortcut", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4-column speed dial grid
            val itemsToDisplay = shortcuts.take(8)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                itemsToDisplay.chunked(4).forEach { rowShortcuts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowShortcuts.forEach { shortcut ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clickable { onUrlSelect(shortcut.url) }
                                    .testTag("shortcut_${shortcut.title}")
                            ) {
                                Surface(
                                    modifier = Modifier.size(54.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shadowElevation = 1.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = shortcut.iconEmoji,
                                            fontSize = 24.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = shortcut.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Bookmarks
        if (bookmarks.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = profileColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Bookmarks (${bookmarks.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    bookmarks.take(4).forEach { bm ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onUrlSelect(bm.url) }
                                .testTag("recent_bookmark_${bm.title}"),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bm.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = bm.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Discovery & Trending Topics
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Discovery & Quick Topics",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val topics = listOf(
                "Technology & AI" to "https://news.ycombinator.com",
                "World News" to "https://www.bbc.com/news",
                "Open Source" to "https://github.com/trending",
                "Wikipedia Today" to "https://en.wikipedia.org/wiki/Main_Page",
                "Reddit Frontpage" to "https://www.reddit.com"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topics.forEach { (topic, url) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUrlSelect(url) }
                            .testTag("discovery_$topic"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Explore",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Shortcut Dialog
    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("https://") }
        var newEmoji by remember { mutableStateOf("🌐") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Shortcut") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title (e.g. YouTube)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("shortcut_title_input")
                    )
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("shortcut_url_input")
                    )
                    OutlinedTextField(
                        value = newEmoji,
                        onValueChange = { newEmoji = it },
                        label = { Text("Icon Emoji (e.g. 📺, 🚀, 💻)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("shortcut_emoji_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() && newUrl.isNotBlank()) {
                            onAddShortcut(newTitle, newUrl, newEmoji.ifBlank { "🌐" })
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_shortcut_button")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
