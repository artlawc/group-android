package com.groupa.domain.model

data class GroupMessage(
    val id: String,
    val title: String,
    val preview: String,
    val time: String,
    val isChat: Boolean,
    val unread: Boolean,
    val avatarLabel: String,
    val orderId: String? = null,
    val stockId: String? = null,
    val bodyMuscleId: String? = null,
    val car3dId: String? = null,
)
