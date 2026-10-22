package com.groupa.domain.model

data class StockInfo(
    val id: String,
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val percentChange: Double,
    val history: List<Double>,
)
