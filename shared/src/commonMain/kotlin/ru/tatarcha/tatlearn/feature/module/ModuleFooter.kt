package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.ModuleDetails
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.module_continue
import tatlearn.shared.generated.resources.module_next_module

/** Закреплённая кнопка внизу: «Продолжить» с рекомендуемым уроком или переход к следующему модулю, когда этот пройден. */
@Composable
fun ModuleFooter(
    details: ModuleDetails,
    onEvent: (ModuleUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val next = details.nextLesson
    val nextModule = details.nextModule
    val (text, event) = when {
        next != null -> stringResource(Res.string.module_continue, next.number) to ModuleUiEvent.ContinueClicked
        details.isCompleted && nextModule != null -> stringResource(Res.string.module_next_module, nextModule.titleTt) to ModuleUiEvent.NextModuleClicked
        else -> return
    }
    Column(modifier = modifier.fillMaxWidth().background(TamyrTheme.colors.background)) {
        Box(Modifier.fillMaxWidth().height(TamyrSizes.borderThin).background(TamyrTheme.colors.border))
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(
                    start = TamyrTheme.spacing.screen,
                    end = TamyrTheme.spacing.screen,
                    top = TamyrTheme.spacing.m,
                    bottom = TamyrTheme.spacing.l,
                ),
        ) {
            TamyrButton(
                text = text,
                onClick = { onEvent(event) },
            )
        }
    }
}
