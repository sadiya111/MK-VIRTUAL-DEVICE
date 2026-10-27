package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.ProfilePresets
import com.example.browser.SearchEngine
import com.example.data.local.entity.ProfileEntity
import com.example.ui.components.getProfileIconVector
import com.example.ui.components.parseColorHex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    profileToEdit: ProfileEntity?,
    onSaveProfile: (name: String, colorHex: String, icon: String, searchEngine: String, customHome: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = profileToEdit != null

    var name by remember { mutableStateOf(profileToEdit?.name ?: "") }
    var selectedColorHex by remember { mutableStateOf(profileToEdit?.colorHex ?: "#3B82F6") }
    var selectedIcon by remember { mutableStateOf(profileToEdit?.avatarIcon ?: "person") }
    var selectedEngine by remember { mutableStateOf(profileToEdit?.searchEngine ?: "Google") }
    var customHome by remember { mutableStateOf(profileToEdit?.customHomeUrl ?: "") }
    var isEngineMenuExpanded by remember { mutableStateOf(false) }

    val activeColor = parseColorHex(selectedColorHex)

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
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp).testTag("profile_edit_back")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = if (isEditing) "Edit Profile" else "Create Profile",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(activeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getProfileIconVector(selectedIcon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = name.ifBlank { "Profile Name" },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Search Engine: $selectedEngine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile Name (e.g. Work, Finance, Social)") },
                modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                singleLine = true
            )

            // Color Palette Selector
            Column {
                Text(
                    text = "Profile Accent Color",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfilePresets.COLORS.forEach { preset ->
                        val isSelected = selectedColorHex.equals(preset.hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(preset.color)
                                .clickable { selectedColorHex = preset.hex }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .testTag("color_preset_${preset.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // Avatar Icon Selector
            Column {
                Text(
                    text = "Profile Avatar Icon",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfilePresets.ICONS.forEach { (iconKey, label) ->
                        val isSelected = selectedIcon == iconKey
                        Surface(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedIcon = iconKey }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, activeColor, RoundedCornerShape(14.dp))
                                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                                )
                                .testTag("icon_preset_$iconKey"),
                            color = if (isSelected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = getProfileIconVector(iconKey),
                                    contentDescription = label,
                                    tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search Engine Dropdown
            Column {
                Text(
                    text = "Default Search Engine",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = isEngineMenuExpanded,
                    onExpandedChange = { isEngineMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedEngine,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isEngineMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("engine_dropdown_field")
                    )

                    ExposedDropdownMenu(
                        expanded = isEngineMenuExpanded,
                        onDismissRequest = { isEngineMenuExpanded = false }
                    ) {
                        SearchEngine.entries.forEach { engine ->
                            DropdownMenuItem(
                                text = { Text(engine.displayName) },
                                onClick = {
                                    selectedEngine = engine.displayName
                                    isEngineMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Custom Homepage URL (Optional)
            OutlinedTextField(
                value = customHome,
                onValueChange = { customHome = it },
                label = { Text("Custom Homepage URL (Optional)") },
                placeholder = { Text("https://...") },
                modifier = Modifier.fillMaxWidth().testTag("custom_homepage_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSaveProfile(name, selectedColorHex, selectedIcon, selectedEngine, customHome)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button")
            ) {
                Text(if (isEditing) "Save Changes" else "Create Profile", fontSize = 16.sp)
            }
        }
    }
}
