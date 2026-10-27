package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.SearchEngine
import com.example.browser.SearchSuggestion
import com.example.browser.UrlUtils
import com.example.data.local.entity.ProfileEntity
import com.example.ui.components.parseColorHex

@Composable
fun OmniboxOverlay(
    initialText: String,
    activeProfile: ProfileEntity?,
    isPrivate: Boolean,
    suggestions: List<SearchSuggestion>,
    onTextChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(initialText) }
    var selectedEngine by remember {
        mutableStateOf(SearchEngine.fromName(activeProfile?.searchEngine ?: "Google"))
    }
    val focusRequester = remember { FocusRequester() }
    val accentColor = if (isPrivate) Color(0xFF8B5CF6) else parseColorHex(activeProfile?.colorHex ?: "#3B82F6")

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Search Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("omnibox_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onTextChange(it)
                },
                placeholder = { Text("Search or type URL", fontSize = 14.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Go,
                    keyboardType = KeyboardType.Uri
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (text.isNotBlank()) onSubmit(text)
                    }
                ),
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                text = ""
                                onTextChange("")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .focusRequester(focusRequester)
                    .testTag("omnibox_input_field"),
                shape = RoundedCornerShape(26.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedIndicatorColor = accentColor,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { /* Simulated QR Code Scan */ onSubmit("https://www.google.com") },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "QR Scanner",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Engine Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchEngine.entries.forEach { engine ->
                FilterChip(
                    selected = selectedEngine == engine,
                    onClick = { selectedEngine = engine },
                    label = { Text(engine.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.2f),
                        selectedLabelColor = accentColor
                    ),
                    modifier = Modifier.testTag("engine_chip_${engine.displayName}")
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.8.dp)

        // Suggestions List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            if (text.isNotBlank()) {
                item {
                    SuggestionItem(
                        icon = Icons.Default.Search,
                        iconTint = accentColor,
                        title = "Search with ${selectedEngine.displayName}",
                        subtitle = text,
                        onClick = { onSubmit(UrlUtils.formatInputToUrl(text, selectedEngine)) }
                    )
                }
            }

            items(suggestions) { item ->
                val icon = when {
                    item.isBookmark -> Icons.Default.Star
                    item.isHistory -> Icons.Default.History
                    else -> Icons.Default.Language
                }
                val tint = when {
                    item.isBookmark -> Color(0xFFF59E0B)
                    item.isHistory -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> accentColor
                }

                SuggestionItem(
                    icon = icon,
                    iconTint = tint,
                    title = item.title,
                    subtitle = item.url,
                    onClick = { onSubmit(item.url) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("suggestion_item_$title"),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank() && subtitle != title) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
