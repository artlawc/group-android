package com.groupa.domain.model

data class Deal(
    val id: String,
    val category: String,
    val title: String,
    val image: String,
    val rating: String,
    val reviews: Int,
    val cuisine: String,
    val price: Double,
    val originalPrice: Double,
    val discount: String,
    val distance: String,
    val merchant: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val hours: String,
    val highlights: List<String>,
    val about: String,
)
