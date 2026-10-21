package ru.tatarcha.tatlearn

import androidx.compose.ui.window.ComposeUIViewController
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import ru.tatarcha.tatlearn.di.initKoin

// Сторона iOS: Koin запускается здесь (один раз на процесс); единственная проверка платформы — отладочная ли
// сборка Kotlin, в ней показывается кнопка «UI kit». Проверка: запустить из Xcode (Debug) -> «UI kit» справа вверху.
private val koinStarted: Unit by lazy { initKoin() }

@OptIn(ExperimentalNativeApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = { koinStarted },
) { App(showDebugTools = Platform.isDebugBinary) }
