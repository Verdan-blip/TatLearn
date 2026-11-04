package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.ModuleDetails
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.module_empty_home
import tatlearn.shared.generated.resources.module_empty_message
import tatlearn.shared.generated.resources.module_empty_title

/** Модуль без уроков (макет 22): баннер без прогресса, спящий Мыраужан и кнопка «На главную». */
@Composable
fun ModuleEmptyState(
    details: ModuleDetails,
    onHome: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            ModuleBanner(
                details = details,
                onBack = onHome,
                showProgress = false,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TamyrTheme.spacing.xxl, vertical = TamyrTheme.spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xl, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TamyrMascot(MascotPose.Sleeping, 130.dp)
                Text(
                    text = stringResource(Res.string.module_empty_title),
                    style = TamyrTheme.typography.headline24,
                    color = TamyrTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(Res.string.module_empty_message),
                    style = TamyrTheme.typography.body16,
                    color = TamyrTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = TamyrTheme.spacing.screen, end = TamyrTheme.spacing.screen, bottom = TamyrTheme.spacing.l),
        ) {
            TamyrButton(
                text = stringResource(Res.string.module_empty_home),
                onClick = onHome,
            )
        }
    }
}
