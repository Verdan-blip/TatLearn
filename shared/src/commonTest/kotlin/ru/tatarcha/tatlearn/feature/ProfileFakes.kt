package ru.tatarcha.tatlearn.feature

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.UserProfile
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.ProfileRepository

internal class FakeProfile(initial: UserProfile? = null) : ProfileRepository {
    private val state = MutableStateFlow(initial)
    override val profile: StateFlow<UserProfile?> = state
    private val savedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val saved: SharedFlow<Unit> = savedFlow
    val saves = mutableListOf<Pair<String, Avatar>>()

    override fun save(name: String, avatar: Avatar) {
        saves += name to avatar
        state.value = UserProfile(state.value?.id ?: "id", name, avatar)
        savedFlow.tryEmit(Unit)
    }

    override fun clear() { state.value = null }
}

/** Репозиторий входа для тестов профиля: хранит сессию, вход и регистрация не нужны. */
internal class SessionOnlyAuth(initial: UserSession? = null) : AuthRepository {
    private val state = MutableStateFlow(initial)
    override val session: StateFlow<UserSession?> = state
    override suspend fun signIn(email: String, password: String): Result<UserSession> = error("unused")
    override suspend fun signUp(email: String, password: String): Result<UserSession> = error("unused")
    override fun signOut() { state.value = null }
}
