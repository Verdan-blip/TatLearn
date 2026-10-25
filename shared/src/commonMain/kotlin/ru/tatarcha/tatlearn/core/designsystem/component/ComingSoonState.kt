package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.coming_soon_tag
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/**
 * Экран 16. Заглушка раздела, которого ещё нет: спящий маскот, тег, заголовок и пояснение.
 * Кнопки нет: выход на главную — вкладка «Өй» в нижнем меню (его рисует вызывающий экран).
 * [extra] — необязательный блок под текстом (в отладочной сборке: вход в инструменты на вкладке «Профиль»).
 * Татарский текст («Тиздән · Скоро», заголовки) подтверждён автором-носителем.
 */
@Composable
fun ComingSoonState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    extra: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val spacing = TamyrTheme.spacing
    Column(
        modifier
            .fillMaxSize()
            .background(TamyrTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TamyrMascot(MascotPose.Sleeping, ComingSoonMascotSize)
        Text(
            text = stringResource(Res.string.coming_soon_tag),
            style = TamyrTheme.typography.caption13Bold,
            color = TamyrTheme.colors.goldText,
            modifier = Modifier
                .clip(TamyrTheme.shapes.tag)
                .background(TamyrTheme.colors.goldContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = TamyrTheme.typography.headline26, color = TamyrTheme.colors.textPrimary, textAlign = TextAlign.Center)
            Text(message, style = TamyrTheme.typography.body16, color = TamyrTheme.colors.textSecondary, textAlign = TextAlign.Center)
        }
        extra?.invoke(this)
    }
}

private val ComingSoonMascotSize = 150.dp

@Preview(name = "Coming soon", group = "States", widthDp = 390, heightDp = 844)
@Composable
private fun ComingSoonPreview() = PreviewFrame {
    ComingSoonState(title = "Сүзлек әзерләнә", message = "Словарь ещё готовится. Мыраужан вздремнул, но скоро проснётся.")
}

@Preview(name = "Coming soon · small phone", group = "States", widthDp = 320, heightDp = 568)
@Composable
private fun ComingSoonSmallPreview() = PreviewFrame {
    ComingSoonState(title = "Профиль әзерләнә", message = "Профиль ещё готовится. Мыраужан вздремнул, но скоро проснётся.")
}
