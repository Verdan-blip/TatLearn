package ru.tatarcha.tatlearn

import androidx.compose.runtime.Composable
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.navigation.AppNavigation

/** Koin уже должен быть запущен (Android: Application, iOS: MainViewController). */
@Composable
fun App(showDebugTools: Boolean = false) {
    TamyrTheme {
        AppNavigation(showDebugTools)
    }
}
