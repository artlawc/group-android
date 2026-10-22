package com.groupa.domain.model

data class Contact(
    val id: String,
    val name: String,
    val phone: String,
) {
    val sectionInitial: String
        get() {
            val initial = name.trim().firstOrNull()?.uppercaseChar()
            return if (initial != null && initial in 'A'..'Z') initial.toString() else "#"
        }
}
