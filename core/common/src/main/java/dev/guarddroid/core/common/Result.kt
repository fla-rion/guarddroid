package dev.guarddroid.core.common

sealed class GuardResult<out T> {
    data class Success<T>(val data: T) : GuardResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : GuardResult<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error

    fun getOrNull(): T? = if (this is Success) data else null
    fun errorMessage(): String? = if (this is Error) message else null
}

fun <T> guardRunCatching(block: () -> T): GuardResult<T> = try {
    GuardResult.Success(block())
} catch (e: Exception) {
    GuardResult.Error(e.message ?: "Unknown error", e)
}
