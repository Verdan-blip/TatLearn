package ru.tatarcha.tatlearn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import ru.tatarcha.tatlearn.core.notifications.Reminders
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.navigation.AppNavigation

/** Koin уже должен быть запущен (Android: Application, iOS: MainViewController). */
@Composable
fun App(showDebugTools: Boolean = false) {
    val reminders = koinInject<Reminders>()
    LaunchedEffect(reminders) { reminders.restore() }
    TamyrTheme {
        AppNavigation(showDebugTools)
    }
}
