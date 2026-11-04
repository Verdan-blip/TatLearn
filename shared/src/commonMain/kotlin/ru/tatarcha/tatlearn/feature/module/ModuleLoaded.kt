package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.ModuleDetails
import ru.tatarcha.tatlearn.domain.model.moduleTitle

private val CollapseStart = 150.dp
private val CollapseSpan = 70.dp

/**
 * Экран модуля с уроками: баннер и лента прокручиваются вместе, кнопка внизу закреплена. Когда баннер уходит,
 * сверху проявляется компактная строка «назад и название».
 */
@Composable
fun ModuleLoaded(
    details: ModuleDetails,
    onEvent: (ModuleUiEvent) -> Unit,
) {
    val scroll = rememberScrollState()
    val start = with(LocalDensity.current) { CollapseStart.toPx() }
    val span = with(LocalDensity.current) { CollapseSpan.toPx() }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll),
            ) {
                ModuleBanner(
                    details = details,
                    onBack = { onEvent(ModuleUiEvent.BackClicked) },
                )
                if (details.isLocked) {
                    ModuleLockedBanner(
                        required = details.lockedBy,
                        modifier = Modifier.padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.l),
                    )
                }
                Column(
                    modifier = Modifier.padding(start = TamyrTheme.spacing.xl, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.xl, bottom = TamyrTheme.spacing.l),
                    verticalArrangement = Arrangement.spacedBy(LessonGap),
                ) {
                    details.lessons.forEachIndexed { index, row ->
                        ModuleLessonItem(
                            row = row,
                            isLast = index == details.lessons.lastIndex,
                            onClick = { onEvent(ModuleUiEvent.LessonClicked(row.lesson.id)) },
                        )
                    }
                }
            }
            ModuleFooter(details = details, onEvent = onEvent)
        }
        ModuleCompactBar(
            title = moduleTitle(details.module.titleTt, details.module.titleRu),
            onBack = { onEvent(ModuleUiEvent.BackClicked) },
            alpha = { ((scroll.value - start) / span).coerceIn(0f, 1f) },
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
