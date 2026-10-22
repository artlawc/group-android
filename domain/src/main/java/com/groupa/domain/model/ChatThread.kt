package com.groupa.domain.model

data class ChatThread(
    val id: String,
    val title: String,
    val isActive: Boolean,
    val avatarLabel: String,
    val unread: Boolean = false,
)

data class ChatMessage(
    val id: String,
    val text: String,
    val timestamp: String,
    val fromUser: Boolean,
)
