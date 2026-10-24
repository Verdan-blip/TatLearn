package ru.tatarcha.tatlearn.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AuthScene(
    mode: AuthMode,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(parameters = { parametersOf(mode) }),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthUiEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }
    val state by viewModel.state.collectAsState()
    AuthContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}
