package com.groupa.core.error

sealed class Failure {
    data class UnknownError(val throwable: Throwable? = null) : Failure()
}

fun Failure.message(): String = when (this) {
    is Failure.UnknownError -> throwable?.message ?: "An unexpected error occurred."
}
