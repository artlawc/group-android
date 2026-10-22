package com.groupa.domain.model

data class Order(
    val orderId: String,
    val dealId: String,
    val title: String,
    val merchant: String,
    val address: String,
    val code: String,
    val total: Double,
    val date: String,
    val status: String,
    val image: String,
)
