package ru.tatarcha.tatlearn.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Предоставляется каждой платформой (expect/actual): SqlDriver, Settings и движок Ktor HttpClientEngine.
 *
 * iOS: NativeSqliteDriver, Settings на NSUserDefaults и движок Darwin (см. iosMain).
 * Проверка на iOS: данные входа и прогресс переживают перезапуск приложения в симуляторе.
 */
expect val platformModule: Module

val appModules: List<Module>
    get() = listOf(platformModule, networkModule, databaseModule, dataModule, presentationModule)

/**
 * Запускает Koin один раз. Android вызывает его из Application.onCreate (нужен androidContext),
 * iOS — из MainViewController.
 */
fun initKoin(extra: KoinApplication.() -> Unit = {}) {
    startKoin {
        extra()
        modules(appModules)
    }
}
