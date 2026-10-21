package ru.tatarcha.tatlearn.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTextButton
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.onboarding_next
import tatlearn.shared.generated.resources.onboarding_skip
import tatlearn.shared.generated.resources.onboarding_start

@Composable
fun OnboardingContent(
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { uiState.pages.size },
    )
    val currentOnEvent by rememberUpdatedState(onEvent)

    // Двусторонняя синхронизация: свайп шлёт PageChanged, смена currentPage в состоянии прокручивает пейджер.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> currentOnEvent(OnboardingUiEvent.PageChanged(page)) }
    }
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TamyrTheme.colors.background)
            .safeDrawingPadding(),
    ) {
        OnboardingTopBar(
            canGoBack = uiState.canGoBack,
            onBackClick = { onEvent(OnboardingUiEvent.BackClicked) },
            onCloseClick = { onEvent(OnboardingUiEvent.SkipClicked) },
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { index ->
            OnboardingPageContent(page = uiState.pages[index])
        }

        PagerDots(
            current = uiState.currentPage,
            count = uiState.pages.size,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Column(
            modifier = Modifier.padding(
                start = TamyrTheme.spacing.screen,
                end = TamyrTheme.spacing.screen,
                top = TamyrTheme.spacing.l,
                bottom = TamyrTheme.spacing.l,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)
        ) {
            TamyrButton(
                text = stringResource(
                    if (uiState.isLastPage) Res.string.onboarding_start else Res.string.onboarding_next,
                ),
                onClick = { onEvent(OnboardingUiEvent.NextClicked) },
            )

            TamyrTextButton(
                text = stringResource(Res.string.onboarding_skip),
                onClick = { onEvent(OnboardingUiEvent.SkipClicked) },
            )
        }
    }
}

@Preview(name = "Страница 1")
@Composable
private fun OnboardingContentFirstPagePreview() = PreviewFrame {
    OnboardingContent(
        uiState = OnboardingUiState(currentPage = 0),
        onEvent = {},
    )
}

@Preview(name = "Страница 2")
@Composable
private fun OnboardingContentSecondPagePreview() = PreviewFrame {
    OnboardingContent(
        uiState = OnboardingUiState(currentPage = 1),
        onEvent = {},
    )
}

@Preview(name = "Страница 3")
@Composable
private fun OnboardingContentThirdPagePreview() = PreviewFrame {
    OnboardingContent(
        uiState = OnboardingUiState(currentPage = 2),
        onEvent = {},
    )
}
