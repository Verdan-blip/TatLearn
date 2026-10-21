package ru.tatarcha.tatlearn.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import de.jensklingenberg.ktorfit.Ktorfit
import app.cash.sqldelight.db.SqlDriver
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.tatarcha.tatlearn.core.common.BuildConfig
import ru.tatarcha.tatlearn.core.common.SystemTimeProvider
import ru.tatarcha.tatlearn.core.common.TimeProvider
import ru.tatarcha.tatlearn.core.database.TamyrDatabase
import ru.tatarcha.tatlearn.data.fake.FakeAuthRepository
import ru.tatarcha.tatlearn.data.fake.FakeBackendConfig
import ru.tatarcha.tatlearn.data.fake.FakeLessonsRepository
import ru.tatarcha.tatlearn.data.local.SessionStorage
import ru.tatarcha.tatlearn.data.local.SqlDelightProgressRepository
import ru.tatarcha.tatlearn.data.remote.AuthApi
import ru.tatarcha.tatlearn.data.remote.LessonsApi
import ru.tatarcha.tatlearn.data.remote.RemoteAuthRepository
import ru.tatarcha.tatlearn.data.remote.RemoteLessonsRepository
import ru.tatarcha.tatlearn.data.remote.createAuthApi
import ru.tatarcha.tatlearn.data.remote.createLessonsApi
import ru.tatarcha.tatlearn.domain.repository.AuthRepository
import ru.tatarcha.tatlearn.domain.repository.LessonsRepository
import ru.tatarcha.tatlearn.domain.repository.ProgressRepository
import ru.tatarcha.tatlearn.data.local.OnboardingStorage
import ru.tatarcha.tatlearn.feature.auth.AuthMode
import ru.tatarcha.tatlearn.feature.auth.AuthViewModel
import ru.tatarcha.tatlearn.feature.home.HomeViewModel
import ru.tatarcha.tatlearn.feature.onboarding.OnboardingViewModel
import ru.tatarcha.tatlearn.feature.result.ResultViewModel
import ru.tatarcha.tatlearn.core.audio.StubbingAudioPlayer
import ru.tatarcha.tatlearn.core.audio.createPlatformAudioPlayer
import ru.tatarcha.tatlearn.feature.lesson.LessonViewModel
import ru.tatarcha.tatlearn.navigation.LessonKey
import ru.tatarcha.tatlearn.navigation.ResultKey
import ru.tatarcha.tatlearn.navigation.StartDestinationResolver
import ru.tatarcha.tatlearn.feature.showcase.ShowcaseViewModel

val presentationModule = module {
    single { StartDestinationResolver(get(), get()) }
    viewModelOf(::OnboardingViewModel)
    viewModel { (mode: AuthMode) -> AuthViewModel(mode, get()) }
    viewModelOf(::HomeViewModel)
    viewModel { (key: ResultKey) -> ResultViewModel(key, get(), get()) }
    viewModel { (key: LessonKey) -> LessonViewModel(key, get(), get(), StubbingAudioPlayer(createPlatformAudioPlayer()), get()) }
    viewModelOf(::ShowcaseViewModel)
}

/** Клиент Ktor и API Ktorfit. Движок приходит из платформенного модуля; при useRemote = false здесь ничего не выполняется. */
val networkModule = module {
    single {
        HttpClient(get<HttpClientEngine>()) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(HttpTimeout) { requestTimeoutMillis = REQUEST_TIMEOUT_MS }
        }
    }
    single { Ktorfit.Builder().httpClient(get<HttpClient>()).baseUrl(BuildConfig.BASE_URL).build() }
    single<LessonsApi> { get<Ktorfit>().createLessonsApi() }
    single<AuthApi> { get<Ktorfit>().createAuthApi() }
}

val databaseModule = module {
    single { TamyrDatabase(get<SqlDriver>()) }
    single<TimeProvider> { SystemTimeProvider() }
    single<ProgressRepository> { SqlDelightProgressRepository(get(), get()) }
}

/** По умолчанию — фейковые репозитории; чтобы использовать Ktorfit-репозитории, переключите `tamyr.useRemote` в gradle.properties. */
val dataModule = module {
    single { SessionStorage(get()) }
    single { OnboardingStorage(get()) }
    single { FakeBackendConfig() }
    if (BuildConfig.USE_REMOTE) {
        single<LessonsRepository> { RemoteLessonsRepository(get()) }
        single<AuthRepository> { RemoteAuthRepository(get(), get()) }
    } else {
        single<LessonsRepository> { FakeLessonsRepository(get()) }
        single<AuthRepository> { FakeAuthRepository(get(), get()) }
    }
}

private const val REQUEST_TIMEOUT_MS = 15_000L
