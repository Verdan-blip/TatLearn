package ru.tatarcha.tatlearn.feature.module

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tatarcha.tatlearn.navigation.ModuleKey

@Composable
fun ModuleScene(
    key: ModuleKey,
    onBack: () -> Unit,
    onStartLesson: (lessonId: String) -> Unit,
    onOpenModule: (moduleId: String) -> Unit,
    viewModel: ModuleViewModel = koinViewModel(parameters = { parametersOf(key) }),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ModuleUiEffect.NavigateBack -> onBack()
                is ModuleUiEffect.NavigateToLesson -> onStartLesson(effect.lessonId)
                is ModuleUiEffect.NavigateToModule -> onOpenModule(effect.moduleId)
            }
        }
    }
    val state by viewModel.state.collectAsState()
    ModuleContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}
