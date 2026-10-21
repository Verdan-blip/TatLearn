package ru.tatarcha.tatlearn

import android.app.Application
import org.koin.android.ext.koin.androidContext
import ru.tatarcha.tatlearn.di.initKoin

class TatLearnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@TatLearnApplication) }
    }
}
