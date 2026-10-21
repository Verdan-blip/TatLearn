package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Оборачивает содержимое `@Preview` в тему приложения на фоне экрана (размер телефона задаётся в каждом `@Preview`). */
@Composable
internal fun PreviewFrame(content: @Composable () -> Unit) {
    TamyrTheme {
        Box(Modifier.fillMaxSize().background(TamyrTheme.colors.background)) { content() }
    }
}
