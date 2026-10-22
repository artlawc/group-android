package com.groupa.domain.model

data class User(
    val name: String,
    val email: String,
    val memberSince: String,
    val totalSavings: Double,
    val dealsSaved: Int,
    val avatarInitial: String,
    val isSignedIn: Boolean,
    val avatarUri: String = "",
    val phone: String = "",
)
