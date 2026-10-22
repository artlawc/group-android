package com.groupa.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.domain.model.ChatMessage
import com.groupa.domain.model.ChatThread
import com.groupa.presentation.component.formatMoney

@Composable
fun ChatScreen(
    thread: ChatThread,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onOpenDeal: () -> Unit,
    onBack: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        Surface(
            color = White,
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Box {
                    Surface(shape = CircleShape, color = GroupBlue.copy(alpha = 0.1f)) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = thread.avatarLabel,
                                color = GroupBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = SuccessGreen,
                        border = androidx.compose.foundation.BorderStroke(2.dp, White),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp),
                    ) {}
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    Text(
                        text = thread.title,
                        color = Ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (thread.isActive) "Active now" else "Offline",
                        color = if (thread.isActive) SuccessGreen else Gray400,
                        fontSize = 12.sp,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Phone,
                            contentDescription = "Call merchant",
                            tint = CheckoutBlue,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Today",
                    color = Gray400,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Classic Cheeseburger Deal",
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = formatMoney(5.0),
                                color = CheckoutBlue,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = formatMoney(10.0),
                                color = Gray400,
                                fontSize = 12.sp,
                            )
                        }
                        OutlinedButton(onClick = onOpenDeal) {
                            Text(text = "View Details")
                        }
                    }
                }
            }
            items(messages, key = { it.id }) { message ->
                ChatBubble(message)
            }
        }

        Surface(
            color = White,
            shadowElevation = 10.dp,
        ) {
            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(text = "Type a message...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ColorCompat,
                        unfocusedContainerColor = ColorCompat,
                    ),
                )
                Button(
                    onClick = {
                        if (input.isNotBlank()) {
                            onSendMessage(input)
                            input = ""
                        }
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = CheckoutBlue),
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send message",
                        tint = White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private val ColorCompat = androidx.compose.ui.graphics.Color(0xFFF1F5F9)

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.fromUser) 18.dp else 4.dp,
                bottomEnd = if (message.fromUser) 4.dp else 18.dp,
            ),
            color = if (message.fromUser) CheckoutBlue else White,
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = message.text,
                    color = if (message.fromUser) White else Ink,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Text(
                    text = message.timestamp,
                    color = if (message.fromUser) White.copy(alpha = 0.75f) else Gray500,
                    fontSize = 10.sp,
                )
            }
        }
    }
}
