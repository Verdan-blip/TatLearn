package ru.tatarcha.tatlearn.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults
import ru.tatarcha.tatlearn.core.database.TamyrDatabase
import ru.tatarcha.tatlearn.core.haptics.IosHaptics
import ru.tatarcha.tatlearn.core.haptics.PlatformHaptics

// expect/actual (сторона iOS): SQLite через NativeSqliteDriver (файл в песочнице приложения),
// NSUserDefaults для настроек, движок Darwin на основе URLSession для Ktor, вибро через Taptic Engine (UIFeedbackGenerator).
// Проверка: запустить приложение в симуляторе, записать прогресс в UI kit, перезапустить — значения сохраняются.
actual val platformModule = module {
    single<SqlDriver> { NativeSqliteDriver(TamyrDatabase.Schema, "tamyr.db") }
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
    single<HttpClientEngine> { Darwin.create() }
    single<PlatformHaptics> { IosHaptics() }
}
