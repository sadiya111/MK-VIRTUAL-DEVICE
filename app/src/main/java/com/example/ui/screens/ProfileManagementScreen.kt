package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProfileEntity
import com.example.ui.components.getProfileIconVector
import com.example.ui.components.parseColorHex

@Composable
fun ProfileManagementScreen(
    profiles: List<ProfileEntity>,
    activeProfile: ProfileEntity?,
    onAddProfileClick: () -> Unit,
    onEditProfileClick: (ProfileEntity) -> Unit,
    onSetDefaultProfile: (ProfileEntity) -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit,
    onClearProfileData: (ProfileEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var profileToDelete by remember { mutableStateOf<ProfileEntity?>(null) }
    var profileToClear by remember { mutableStateOf<ProfileEntity?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp).testTag("profile_mgmt_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Manage Profiles",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Profiles let you keep your browsing separated—like Work, Personal, and Study—each with its own cookies, tabs, logins, and history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(profiles) { profile ->
                    val color = parseColorHex(profile.colorHex)
                    val isCurrent = profile.id == activeProfile?.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .then(
                                if (isCurrent) Modifier.border(2.dp, color, RoundedCornerShape(18.dp))
                                else Modifier.border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                            )
                            .testTag("profile_manage_card_${profile.name}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getProfileIconVector(profile.avatarIcon),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = profile.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = color.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Active",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = color,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Search Engine: ${profile.searchEngine}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { onSetDefaultProfile(profile) },
                                    modifier = Modifier.testTag("set_default_profile_${profile.name}")
                                ) {
                                    Icon(
                                        imageVector = if (profile.isDefault) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = "Default Profile",
                                        tint = if (profile.isDefault) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { onEditProfileClick(profile) },
                                    modifier = Modifier.testTag("edit_profile_btn_${profile.name}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit")
                                }

                                TextButton(
                                    onClick = { profileToClear = profile },
                                    modifier = Modifier.testTag("clear_profile_data_btn_${profile.name}")
                                ) {
                                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear Data")
                                }

                                if (profiles.size > 1) {
                                    TextButton(
                                        onClick = { profileToDelete = profile },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.testTag("delete_profile_btn_${profile.name}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Floating Action Button to Add Profile
        FloatingActionButton(
            onClick = onAddProfileClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_profile_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Profile", modifier = Modifier.size(28.dp))
        }

        // Delete Confirmation Dialog
        profileToDelete?.let { profile ->
            AlertDialog(
                onDismissRequest = { profileToDelete = null },
                title = { Text("Delete \"${profile.name}\" Profile?") },
                text = {
                    Text(
                        "Are you sure you want to permanently delete this profile? All its open tabs, cookies, browsing history, and saved bookmarks will be erased completely."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteProfile(profile)
                            profileToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_delete_profile")
                    ) {
                        Text("Delete Permanently")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { profileToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Clear Data Confirmation Dialog
        profileToClear?.let { profile ->
            AlertDialog(
                onDismissRequest = { profileToClear = null },
                title = { Text("Clear Data for \"${profile.name}\"?") },
                text = {
                    Text(
                        "This will clear all login cookies, website storage, and browsing history for this profile while keeping your bookmarks and open tabs."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearProfileData(profile)
                            profileToClear = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("confirm_clear_profile")
                    ) {
                        Text("Clear Data")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { profileToClear = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
