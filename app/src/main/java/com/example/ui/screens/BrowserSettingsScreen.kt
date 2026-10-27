package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.SearchEngine
import com.example.ui.BrowserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserSettingsScreen(
    settings: BrowserSettings,
    onUpdateSettings: (BrowserSettings) -> Unit,
    onClearDataClick: () -> Unit,
    onAboutClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var themeExpanded by remember { mutableStateOf(false) }
    var engineExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp).testTag("settings_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }

            Text(
                text = "Browser Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // General Settings Card
            SettingsSectionHeader(icon = Icons.Default.Palette, title = "Appearance & General")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Theme Selector
                    Text("App Theme", style = MaterialTheme.typography.labelLarge)
                    ExposedDropdownMenuBox(
                        expanded = themeExpanded,
                        onExpandedChange = { themeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = settings.themeMode,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable).testTag("theme_selector_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = themeExpanded,
                            onDismissRequest = { themeExpanded = false }
                        ) {
                            listOf("System", "Light", "Dark").forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode) },
                                    onClick = {
                                        onUpdateSettings(settings.copy(themeMode = mode))
                                        themeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Default Search Engine
                    Text("Default Search Engine", style = MaterialTheme.typography.labelLarge)
                    ExposedDropdownMenuBox(
                        expanded = engineExpanded,
                        onExpandedChange = { engineExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = settings.defaultSearchEngine,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = engineExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable).testTag("settings_engine_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = engineExpanded,
                            onDismissRequest = { engineExpanded = false }
                        ) {
                            SearchEngine.entries.forEach { engine ->
                                DropdownMenuItem(
                                    text = { Text(engine.displayName) },
                                    onClick = {
                                        onUpdateSettings(settings.copy(defaultSearchEngine = engine.displayName))
                                        engineExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Privacy & Security Card
            SettingsSectionHeader(icon = Icons.Default.Security, title = "Privacy & Security")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingToggleRow(
                        title = "Block Pop-ups",
                        subtitle = "Prevent websites from opening unwanted pop-up windows",
                        checked = settings.blockPopups,
                        onCheckedChange = { onUpdateSettings(settings.copy(blockPopups = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SettingToggleRow(
                        title = "Send \"Do Not Track\"",
                        subtitle = "Request websites not to track your browsing activities",
                        checked = settings.doNotTrack,
                        onCheckedChange = { onUpdateSettings(settings.copy(doNotTrack = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Clear Data Action Row
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onClearDataClick)
                            .testTag("clear_browsing_data_setting_row"),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Clear Cookies & Web Storage",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "Removes stored cookies and session storage for active profile",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Advanced Engine Settings Card
            SettingsSectionHeader(icon = Icons.Default.Settings, title = "Advanced Engine")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingToggleRow(
                        title = "Enable JavaScript",
                        subtitle = "Required for interactive modern web apps and dynamic pages",
                        checked = settings.javascriptEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(javascriptEnabled = it)) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    SettingToggleRow(
                        title = "Request Desktop Site by Default",
                        subtitle = "Always request full desktop version of websites",
                        checked = settings.desktopSiteDefault,
                        onCheckedChange = { onUpdateSettings(settings.copy(desktopSiteDefault = it)) }
                    )
                }
            }

            // About Link Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onAboutClick)
                    .testTag("about_browser_link_card"),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("About Prism Browser", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                        Text("Version 1.0.0 • Architecture & Isolation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag("switch_$title")
        )
    }
}
