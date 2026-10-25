package ru.tatarcha.tatlearn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.navigation.AppNavigation

/** Koin уже должен быть запущен (Android: Application, iOS: MainViewController). */
@Composable
fun App(showDebugTools: Boolean = false) {
    TamyrTheme {
        // Одна общая верхняя вставка под статус-бар для всех экранов (в том числе загрузка и ошибка, у которых своей нет):
        // на iOS Compose сам её не добавляет. Вложенный statusBarsPadding после этого получает 0, двойного отступа нет.
        Box(
            Modifier
                .fillMaxSize()
                .background(TamyrTheme.colors.background)
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            AppNavigation(showDebugTools)
        }
    }
}
