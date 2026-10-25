package ru.tatarcha.tatlearn.feature.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun IntroScene(
    onDone: () -> Unit,
    viewModel: IntroViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                IntroUiEffect.NavigateHome -> onDone()
            }
        }
    }
    val state by viewModel.state.collectAsState()
    IntroContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}
