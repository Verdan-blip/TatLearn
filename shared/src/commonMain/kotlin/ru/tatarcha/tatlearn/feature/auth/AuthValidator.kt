package ru.tatarcha.tatlearn.feature.auth

/** Намеренно лёгкие проверки на клиенте; настоящий авторитет — бэкенд. */
object AuthValidator {
    const val MIN_PASSWORD_LENGTH = 6

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun email(value: String): EmailError? = when {
        value.isBlank() -> EmailError.Empty
        !emailRegex.matches(value.trim()) -> EmailError.Invalid
        else -> null
    }

    fun password(value: String): PasswordError? =
        if (value.length < MIN_PASSWORD_LENGTH) PasswordError.TooShort else null
}
