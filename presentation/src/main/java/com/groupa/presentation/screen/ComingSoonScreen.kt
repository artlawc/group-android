package com.groupa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.groupa.coreui.component.CircularIconButton
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.White

@Composable
fun ComingSoonScreen(
    title: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = White,
            shadowElevation = 3.dp,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                CircularIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
            }
        }
        Text(
            text = "This section is on the way.",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            modifier = Modifier
                .padding(top = 120.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "We're polishing this experience for you.",
            color = Gray500,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}
