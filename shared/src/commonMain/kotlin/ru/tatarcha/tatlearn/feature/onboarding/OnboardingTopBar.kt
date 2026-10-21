package ru.tatarcha.tatlearn.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.icon.ChevronBackIcon
import ru.tatarcha.tatlearn.core.designsystem.component.icon.CloseIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.onboarding_back_description
import tatlearn.shared.generated.resources.onboarding_close_description

private val ActionSize = 48.dp

@Composable
fun OnboardingTopBar(
    canGoBack: Boolean,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = TamyrTheme.spacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canGoBack) {
            TopBarAction(
                description = stringResource(Res.string.onboarding_back_description),
                onClick = onBackClick,
            ) {
                ChevronBackIcon()
            }
        } else {
            Box(
                modifier = Modifier.size(ActionSize)
            )
        }

        TopBarAction(
            description = stringResource(Res.string.onboarding_close_description),
            onClick = onCloseClick,
        ) {
            CloseIcon()
        }
    }
}

@Composable
private fun TopBarAction(
    description: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(ActionSize)
            .semantics { contentDescription = description }
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

@Preview
@Composable
private fun OnboardingTopBarFirstPagePreview() = PreviewFrame {
    OnboardingTopBar(
        canGoBack = false,
        onBackClick = {},
        onCloseClick = {},
    )
}

@Preview
@Composable
private fun OnboardingTopBarWithBackPreview() = PreviewFrame {
    OnboardingTopBar(
        canGoBack = true,
        onBackClick = {},
        onCloseClick = {},
    )
}
