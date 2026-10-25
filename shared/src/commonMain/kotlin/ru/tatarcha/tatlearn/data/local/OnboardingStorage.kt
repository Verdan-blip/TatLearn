package ru.tatarcha.tatlearn.data.local

import com.russhwolf.settings.Settings

/** Запоминает, что пользователь уже прошёл пейджер онбординга. */
class OnboardingStorage(private val settings: Settings) {
    fun isSeen(): Boolean = settings.getBoolean(KEY_SEEN, false)

    fun markSeen() = settings.putBoolean(KEY_SEEN, true)

    /** Только для отладки: онбординг снова будет показан. */
    fun reset() = settings.remove(KEY_SEEN)

    private companion object {
        const val KEY_SEEN = "onboarding_seen"
    }
}
