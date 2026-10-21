package ru.tatarcha.tatlearn.navigation

import androidx.navigation3.runtime.NavKey
import ru.tatarcha.tatlearn.data.local.OnboardingStorage
import ru.tatarcha.tatlearn.domain.repository.AuthRepository

/** Вошёл -> Home, онбординг уже показан -> Auth, иначе Onboarding. */
class StartDestinationResolver(
    private val auth: AuthRepository,
    private val onboarding: OnboardingStorage,
) {
    fun resolve(): NavKey = when {
        auth.session.value != null -> HomeKey
        onboarding.isSeen() -> AuthKey()
        else -> OnboardingKey
    }
}
