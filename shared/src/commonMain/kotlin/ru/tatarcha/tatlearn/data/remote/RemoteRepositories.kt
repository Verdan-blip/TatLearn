package ru.tatarcha.tatlearn.data.remote

import kotlinx.coroutines.flow.StateFlow
import ru.tatarcha.tatlearn.data.local.SessionStorage
import ru.tatarcha.tatlearn.data.mapper.toDomain
import ru.tatarcha.tatlearn.data.remote.dto.CredentialsDto
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.LessonContent
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository

class RemoteLessonsRepository(private val api: LessonsApi) : LessonsRepository {
    override suspend fun getHome(): Result<HomeContent> =
        remoteCall { api.getHome().toDomain() }

    override suspend fun getLessonContent(lessonId: String): Result<LessonContent> =
        remoteCall { api.getLesson(lessonId).toDomain() }
}

class RemoteAuthRepository(
    private val api: AuthApi,
    private val storage: SessionStorage,
) : AuthRepository {
    override val session: StateFlow<UserSession?> = storage.session

    override suspend fun signIn(email: String, password: String): Result<UserSession> =
        remoteCall { api.login(CredentialsDto(email, password)).toDomain() }.onSuccess(storage::save)

    override suspend fun signUp(email: String, password: String): Result<UserSession> =
        remoteCall { api.register(CredentialsDto(email, password)).toDomain() }.onSuccess(storage::save)

    override fun signOut() = storage.clear()
}
