package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProfileEntity

fun getProfileIconVector(iconKey: String): ImageVector {
    return when (iconKey.lowercase()) {
        "work" -> Icons.Default.Work
        "school" -> Icons.Default.School
        "shield" -> Icons.Default.Security
        "rocket" -> Icons.Default.RocketLaunch
        "palette" -> Icons.Default.Palette
        "code" -> Icons.Default.Code
        "star" -> Icons.Default.Star
        else -> Icons.Default.Person
    }
}

fun parseColorHex(hex: String, fallback: Color = Color(0xFF3B82F6)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun ProfileAvatarBadge(
    profile: ProfileEntity?,
    isPrivate: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val bgColor = if (isPrivate) Color(0xFF1E1B4B) else parseColorHex(profile?.colorHex ?: "#3B82F6")
    val icon = if (isPrivate) Icons.Default.Security else getProfileIconVector(profile?.avatarIcon ?: "person")
    val label = if (isPrivate) "Private" else profile?.name ?: "Profile"

    Surface(
        modifier = modifier
            .testTag("profile_avatar_badge")
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = bgColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            if (showLabel) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
