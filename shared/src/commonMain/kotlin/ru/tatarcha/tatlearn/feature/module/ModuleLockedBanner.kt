package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.RequiredModule
import ru.tatarcha.tatlearn.domain.model.moduleTitle
import ru.tatarcha.tatlearn.feature.home.LockIcon
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.module_locked_hint
import tatlearn.shared.generated.resources.module_locked_title

/** Плашка закрытого модуля под баннером: что нужно пройти, чтобы он открылся. */
@Composable
fun ModuleLockedBanner(
    required: RequiredModule?,
    modifier: Modifier = Modifier,
) {
    if (required == null) return
    TamyrCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = TamyrTheme.colors.goldContainer,
        borderColor = TamyrTheme.colors.gold,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LockIcon(tint = TamyrTheme.colors.goldText)
            Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                Text(
                    text = stringResource(Res.string.module_locked_title),
                    style = TamyrTheme.typography.body16Bold,
                    color = TamyrTheme.colors.goldText,
                )
                Text(
                    text = stringResource(Res.string.module_locked_hint, moduleTitle(required.titleTt, required.titleRu)),
                    style = TamyrTheme.typography.body14,
                    color = TamyrTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Preview
@Composable
private fun ModuleLockedBannerPreview() = PreviewFrame {
    ModuleLockedBanner(required = RequiredModule("prev", "Кибет", "Магазин и кафе", 1, 4))
}
