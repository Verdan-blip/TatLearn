package ru.tatarcha.tatlearn.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val IllustrationSize = 250.dp
private val TextBlockMinHeight = 160.dp

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = TamyrTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(
            space = 24.dp,
            alignment = Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(IllustrationSize)
                .clip(CircleShape)
                .background(TamyrTheme.colors.surfaceMuted),
            contentAlignment = Alignment.BottomCenter,
        ) {
            TamyrMascot(
                pose = page.pose,
                size = IllustrationSize,
            )
        }

        Column(
            modifier = Modifier.heightIn(min = TextBlockMinHeight),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(page.title),
                style = TamyrTheme.typography.headline32,
                color = TamyrTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(page.body),
                style = TamyrTheme.typography.body16,
                color = TamyrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }

    }
}

@Preview
@Composable
private fun OnboardingPageContentPreview() = PreviewFrame {
    OnboardingPageContent(
        page = DefaultOnboardingPages.first(),
    )
}
