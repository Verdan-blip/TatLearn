package ru.tatarcha.tatlearn.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.tatarcha.tatlearn.domain.model.UserSession

/**
 * Сессия хранится в multiplatform-settings (выбрано вместо DataStore KMP: синхронно,
 * без лишней асинхронной обвязки, поверх SharedPreferences / NSUserDefaults).
 * TODO: хранить токен авторизации в Keychain / EncryptedSharedPreferences, когда появится настоящий бэкенд.
 */
class SessionStorage(private val settings: Settings) {
    private val _session = MutableStateFlow(read())
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    fun save(session: UserSession) {
        settings.putString(KEY_NAME, session.name)
        settings.putString(KEY_EMAIL, session.email)
        _session.value = session
    }

    fun clear() {
        settings.remove(KEY_NAME)
        settings.remove(KEY_EMAIL)
        _session.value = null
    }

    private fun read(): UserSession? {
        val name = settings.getStringOrNull(KEY_NAME) ?: return null
        val email = settings.getStringOrNull(KEY_EMAIL) ?: return null
        return UserSession(name, email)
    }

    private companion object {
        const val KEY_NAME = "session_name"
        const val KEY_EMAIL = "session_email"
    }
}
