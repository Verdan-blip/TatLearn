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

    /** При входе длина не проверяется: у существующего аккаунта пароль мог быть создан по другому правилу. */
    fun password(value: String, mode: AuthMode): PasswordError? = when {
        value.isEmpty() -> PasswordError.Empty
        // TODO(backend): правило длины при регистрации должно совпасть с серверным.
        mode == AuthMode.SignUp && value.length < MIN_PASSWORD_LENGTH -> PasswordError.TooShort
        else -> null
    }
}
