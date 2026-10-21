package ru.tatarcha.tatlearn.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.tatarcha.tatlearn.core.database.TamyrDatabase

// expect/actual (сторона Android): SQLite через AndroidSqliteDriver, настройки в SharedPreferences, движок OkHttp.
actual val platformModule = module {
    single<SqlDriver> { AndroidSqliteDriver(TamyrDatabase.Schema, androidContext(), "tamyr.db") }
    single<Settings> { SharedPreferencesSettings(androidContext().getSharedPreferences("tamyr_settings", Context.MODE_PRIVATE)) }
    single<HttpClientEngine> { OkHttp.create() }
}
