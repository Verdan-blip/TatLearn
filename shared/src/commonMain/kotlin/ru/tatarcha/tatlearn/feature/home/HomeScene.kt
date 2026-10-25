package ru.tatarcha.tatlearn.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScene(
    onStartLesson: (lessonId: String) -> Unit,
    onOpenModule: (moduleId: String) -> Unit,
    onOpenDebug: () -> Unit,
    showDebugTools: Boolean = false,
    viewModel: HomeViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToLesson -> onStartLesson(effect.lessonId)
                is HomeUiEffect.NavigateToModule -> onOpenModule(effect.moduleId)
                HomeUiEffect.NavigateToDebug -> onOpenDebug()
            }
        }
    }
    val state by viewModel.state.collectAsState()
    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        showDebugTools = showDebugTools,
    )
}
