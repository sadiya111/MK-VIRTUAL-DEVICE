package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReaderModeView(
    title: String,
    content: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF0D9)) // Classic sepia paper reader tone
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reader View",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFF5F4B32),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp).testTag("close_reader_view")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close reader",
                        tint = Color(0xFF5F4B32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C241B)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = content.ifBlank { "Distraction-free article text extracted from the current webpage." },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = Color(0xFF382F24)
                )
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
