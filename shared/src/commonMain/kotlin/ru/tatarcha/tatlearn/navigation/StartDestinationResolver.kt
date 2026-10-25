package ru.tatarcha.tatlearn.navigation

import androidx.navigation3.runtime.NavKey
import ru.tatarcha.tatlearn.data.local.OnboardingStorage
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository

/**
 * С включённым флагом регистрации (прежний поток): вошёл -> Home, онбординг показан -> Auth, иначе Onboarding.
 * Гостевой режим (флаг выключен): есть профиль -> Home, онбординг показан -> знакомство, иначе Onboarding.
 * Уже вошедший пользователь без профиля получает его из имени сессии и знакомство не видит.
 */
class StartDestinationResolver(
    private val auth: AuthRepository,
    private val onboarding: OnboardingStorage,
    private val profile: ProfileRepository,
    private val registrationEnabled: Boolean,
) {
    fun resolve(): NavKey {
        if (registrationEnabled) {
            return when {
                auth.session.value != null -> HomeKey
                onboarding.isSeen() -> AuthKey()
                else -> OnboardingKey
            }
        }
        val session = auth.session.value
        if (profile.profile.value == null && session != null) {
            profile.save(session.name, Avatar.Default)
        }
        return when {
            profile.profile.value != null -> HomeKey
            onboarding.isSeen() -> IntroKey
            else -> OnboardingKey
        }
    }
}
