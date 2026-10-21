package ru.tatarcha.tatlearn.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.viewmodel.koinViewModel
import ru.tatarcha.tatlearn.feature.auth.AuthMode

@Composable
fun OnboardingScene(
    onNavigateToAuth: (AuthMode) -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is OnboardingUiEffect.NavigateToAuth -> onNavigateToAuth(effect.mode)
            }
        }
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = uiState.canGoBack,
        onBackCompleted = { viewModel.onEvent(OnboardingUiEvent.BackClicked) },
    )

    OnboardingContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
