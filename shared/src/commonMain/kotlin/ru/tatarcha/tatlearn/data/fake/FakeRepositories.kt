package ru.tatarcha.tatlearn.data.fake

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import ru.tatarcha.tatlearn.core.common.AppException
import ru.tatarcha.tatlearn.core.common.ErrorCodes
import ru.tatarcha.tatlearn.core.common.runCatchingApp
import ru.tatarcha.tatlearn.data.local.SessionStorage
import ru.tatarcha.tatlearn.data.mapper.toDomain
import ru.tatarcha.tatlearn.data.remote.dto.HomeDto
import ru.tatarcha.tatlearn.data.remote.dto.LessonContentDto
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.LessonContent
import ru.tatarcha.tatlearn.domain.model.UserSession
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import tatlearn.shared.generated.resources.Res

private val json = Json { ignoreUnknownKeys = true }

/** Читает JSON в формате API из composeResources/files. Весь контент нужно проверить носителем языка. */
class FakeLessonsRepository(private val config: FakeBackendConfig) : LessonsRepository {

    override suspend fun getHome(): Result<HomeContent> = runCatchingApp {
        config.simulateNetwork()
        json.decodeFromString<HomeDto>(readFile("files/home.json")).toDomain()
    }

    override suspend fun getLessonContent(lessonId: String): Result<LessonContent> = runCatchingApp {
        config.simulateNetwork()
        val text = try {
            readFile("files/lessons/$lessonId.json")
        } catch (e: Exception) {
            throw AppException(ErrorCodes.NOT_FOUND, e)
        }
        json.decodeFromString<LessonContentDto>(text).toDomain()
    }

    private suspend fun readFile(path: String): String = Res.readBytes(path).decodeToString()
}

/** Принимает любые учётные данные; имя берётся из e-mail. Сессия переживает перезапуски через [SessionStorage]. */
class FakeAuthRepository(
    private val config: FakeBackendConfig,
    private val storage: SessionStorage,
) : AuthRepository {
    override val session: StateFlow<UserSession?> = storage.session

    override suspend fun signIn(email: String, password: String): Result<UserSession> = authenticate(email)

    override suspend fun signUp(email: String, password: String): Result<UserSession> = authenticate(email)

    override fun signOut() = storage.clear()

    private suspend fun authenticate(email: String): Result<UserSession> = runCatchingApp {
        config.simulateNetwork()
        val name = email.substringBefore('@').replaceFirstChar { it.uppercase() }
        UserSession(name = name, email = email).also(storage::save)
    }
}
