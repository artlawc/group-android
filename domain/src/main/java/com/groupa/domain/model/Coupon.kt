package com.groupa.domain.model

data class Coupon(
    val id: String,
    val title: String,
    val code: String,
    val value: Double,
    val minimumSpend: Double,
    val expiresOn: String,
    val isExpired: Boolean,
    val terms: List<String>,
)
