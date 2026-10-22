package com.groupa.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.groupa.coreui.theme.AppBackground
import com.groupa.coreui.theme.CheckoutBlue
import com.groupa.coreui.theme.DividerGray
import com.groupa.coreui.theme.Gray400
import com.groupa.coreui.theme.Gray500
import com.groupa.coreui.theme.GroupBlue
import com.groupa.coreui.theme.Ink
import com.groupa.coreui.theme.SuccessGreen
import com.groupa.coreui.theme.White
import com.groupa.coreui.R
import com.groupa.domain.model.ChatThread
import com.groupa.domain.model.GroupMessage
import com.groupa.presentation.viewmodel.GroupUiState

@Composable
fun MessagesScreen(
    state: GroupUiState,
    onOpenChat: (String) -> Unit,
    onOpenOrder: (String) -> Unit,
    onOpenStock: (String) -> Unit,
    onOpenBodyMuscle: (String) -> Unit,
    onOpenCar3D: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onOpenContacts: () -> Unit,
    onMessageRead: (String) -> Unit,
    onDeleteMessages: (Set<String>) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf("Chats") }

    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    val batchMode = selectedIds.isNotEmpty()

    var previousChatUnreadCount by remember { mutableStateOf(state.chats.count { it.unread }) }
    var previousNotificationUnreadCount by remember { mutableStateOf(state.messages.count { it.unread }) }
    var previousChatMessageCount by remember { mutableStateOf(state.chatMessages.size) }

    LaunchedEffect(state.chats, state.messages, state.chatMessages) {
        val chatUnreadCount = state.chats.count { it.unread }
        val notificationUnreadCount = state.messages.count { it.unread }
        val chatMessageCount = state.chatMessages.size

        if (
            chatUnreadCount > previousChatUnreadCount ||
            chatMessageCount > previousChatMessageCount
        ) {
            selectedTab = "Chats"
        } else if (notificationUnreadCount > previousNotificationUnreadCount) {
            selectedTab = "Notifications"
        }

        previousChatUnreadCount = chatUnreadCount
        previousNotificationUnreadCount = notificationUnreadCount
        previousChatMessageCount = chatMessageCount
    }

    fun toggleSelection(messageId: String) {
        selectedIds = if (messageId in selectedIds) {
            selectedIds - messageId
        } else {
            selectedIds + messageId
        }
    }

    fun deleteSelected() {
        if (selectedIds.isEmpty()) return
        onDeleteMessages(selectedIds)
        selectedIds = emptySet()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 115.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (selectedIds.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = ::deleteSelected,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.action_delete_selected),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    OutlinedButton(
                        onClick = { selectedIds = emptySet() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(text = stringResource(R.string.action_cancel_selection))
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.title_messages),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                TextButton(onClick = onMarkAllRead) {
                    Text(
                        text = stringResource(R.string.action_mark_all_read),
                        color = CheckoutBlue,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                IconButton(onClick = onOpenContacts) {
                    Icon(
                        imageVector = Icons.Rounded.Contacts,
                        contentDescription = stringResource(R.string.action_contacts),
                        tint = CheckoutBlue,
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(White)
                    .border(1.dp, DividerGray, RoundedCornerShape(18.dp)),
            ) {
                listOf(
                    stringResource(R.string.tab_chats) to "Chats",
                    stringResource(R.string.tab_notifications) to "Notifications",
                ).forEach { (label, tab) ->
                    val selected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) GroupBlue else White)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            color = if (selected) White else Gray400,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
        if (selectedTab == "Chats") {
            items(state.chats, key = { it.id }) { chat ->
                SwipeToDeleteContainer(
                    messageId = chat.id,
                    enabled = chat.id !in selectedIds,
                    onRowClick = {
                        if (selectedIds.isNotEmpty()) {
                            toggleSelection(chat.id)
                        } else {
                            onMessageRead(chat.id)
                            onOpenChat(chat.id)
                        }
                    },
                    onRowLongClick = { toggleSelection(chat.id) },
                    onDelete = {
                        onDeleteMessages(setOf(chat.id))
                        selectedIds = selectedIds - chat.id
                    },
                    rowContent = { rowClick, rowLongClick ->
                        ChatRow(
                            title = chat.title,
                            avatarLabel = chat.avatarLabel,
                            active = chat.isActive,
                            unread = chat.unread,
                            selected = chat.id in selectedIds,
                            batchMode = batchMode,
                            onClick = rowClick,
                            onLongClick = rowLongClick,
                        )
                    },
                )
            }
        } else {
            items(state.messages, key = { it.id }) { message ->
                SwipeToDeleteContainer(
                    messageId = message.id,
                    enabled = message.id !in selectedIds,
                    onRowClick = {
                        if (selectedIds.isNotEmpty()) {
                            toggleSelection(message.id)
                        } else {
                            onMessageRead(message.id)
                            val bodyMuscleId = message.bodyMuscleId
                            val stockId = message.stockId
                            val orderId = message.orderId
                            val car3dId = message.car3dId
                            when {
                                car3dId != null -> onOpenCar3D(car3dId)
                                bodyMuscleId != null -> onOpenBodyMuscle(bodyMuscleId)
                                stockId != null -> onOpenStock(stockId)
                                orderId != null -> onOpenOrder(orderId)
                            }
                        }
                    },
                    onRowLongClick = { toggleSelection(message.id) },
                    onDelete = {
                        onDeleteMessages(setOf(message.id))
                        selectedIds = selectedIds - message.id
                    },
                    rowContent = { rowClick, rowLongClick ->
                        MessageRow(
                            title = message.title,
                            preview = message.preview,
                            time = message.time,
                            avatarLabel = message.avatarLabel,
                            unread = message.unread,
                            selected = message.id in selectedIds,
                            batchMode = batchMode,
                            onClick = rowClick,
                            onLongClick = rowLongClick,
                        )
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ChatRow(
    title: String,
    avatarLabel: String,
    active: Boolean,
    unread: Boolean,
    selected: Boolean,
    batchMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)

    Surface(
        modifier = rowModifier,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Color(0xFFF3F4F6) else White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MessageLeadingIcon(
                avatarLabel = avatarLabel,
                batchMode = batchMode,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    color = Ink,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (active) "Active now" else "Offline",
                    color = if (active) SuccessGreen else Gray400,
                    fontSize = 12.sp,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(18.dp),
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = GroupBlue,
                    modifier = Modifier.size(18.dp),
                )
            }
            if (unread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFEF4444), CircleShape),
                )
            }
        }
    }
}

@Composable
private fun MessageLeadingIcon(
    avatarLabel: String,
    batchMode: Boolean,
) {
    if (batchMode) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFEFF6FF),
            border = androidx.compose.foundation.BorderStroke(2.dp, GroupBlue),
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = avatarLabel,
                    color = GroupBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    } else {
        Surface(shape = CircleShape, color = GroupBlue.copy(alpha = 0.1f)) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = avatarLabel,
                    color = GroupBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun MessageRow(
    title: String,
    preview: String,
    time: String,
    avatarLabel: String,
    unread: Boolean,
    selected: Boolean,
    batchMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)

    Surface(
        modifier = rowModifier,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Color(0xFFF3F4F6) else White,
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerGray),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MessageLeadingIcon(
                avatarLabel = avatarLabel,
                batchMode = batchMode,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = time,
                        color = Gray400,
                        fontSize = 11.sp,
                    )
                }
                Text(
                    text = preview,
                    color = Gray500,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (unread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(GroupBlue, CircleShape),
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = GroupBlue,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeToDeleteContainer(
    messageId: String,
    enabled: Boolean,
    onRowClick: () -> Unit,
    onRowLongClick: () -> Unit,
    onDelete: () -> Unit,
    rowContent: @Composable (onClick: () -> Unit, onLongClick: () -> Unit) -> Unit,
) {
    var rowHeightPx by remember(messageId) { mutableStateOf(0) }
    val maximumSwipeDistance = rowHeightPx * 1.1f
    val deleteAction = leftSwipeDeleteAction
    var swipeOffset by remember(messageId) { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .onSizeChanged { size ->
                rowHeightPx = size.height
            },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(deleteAction.backgroundColor),
        ) {
            Box(
                modifier = Modifier
                    .align(deleteAction.areaAlignment)
                    .fillMaxHeight()
                    .width(
                        with(LocalDensity.current) {
                            maximumSwipeDistance.toDp()
                        },
                    )
                    .clickable(onClick = onDelete),
                contentAlignment = deleteAction.contentAlignment,
            ) {
                Text(
                    text = stringResource(R.string.action_confirm_delete),
                    color = deleteAction.labelColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(swipeOffset.roundToInt(), 0) }
                .pointerInput(enabled, messageId, maximumSwipeDistance) {
                    if (enabled) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                swipeOffset = (swipeOffset + dragAmount).coerceIn(
                                    -maximumSwipeDistance,
                                    0f,
                                )
                            },
                            onDragEnd = {
                                swipeOffset = if (swipeOffset < -maximumSwipeDistance / 2) {
                                    -maximumSwipeDistance
                                } else {
                                    0f
                                }
                            },
                        )
                    }
                },
        ) {
            rowContent(onRowClick, onRowLongClick)
        }
    }
}
