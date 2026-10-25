package ru.tatarcha.tatlearn.data.remote

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import ru.tatarcha.tatlearn.data.remote.dto.CredentialsDto
import ru.tatarcha.tatlearn.data.remote.dto.LessonContentDto
import ru.tatarcha.tatlearn.data.remote.dto.ModuleDto
import ru.tatarcha.tatlearn.data.remote.dto.SessionDto

/** Не используется при `useRemote = false`; эндпоинты — предложение для будущего бэкенда. */
interface LessonsApi {
    @GET("modules")
    suspend fun getModules(): List<ModuleDto>

    @GET("lessons/{id}")
    suspend fun getLesson(@Path("id") id: String): LessonContentDto
}

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: CredentialsDto): SessionDto

    @POST("auth/register")
    suspend fun register(@Body body: CredentialsDto): SessionDto
}
