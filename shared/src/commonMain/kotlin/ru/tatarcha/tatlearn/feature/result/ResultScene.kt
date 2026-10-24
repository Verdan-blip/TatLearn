package ru.tatarcha.tatlearn.feature.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tatarcha.tatlearn.navigation.ResultKey

@Composable
fun ResultScene(
    key: ResultKey,
    onContinue: () -> Unit,
    onRetryMistakes: (lessonId: String, stepIds: List<String>) -> Unit,
    viewModel: ResultViewModel = koinViewModel(parameters = { parametersOf(key) }),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ResultUiEffect.NavigateHome -> onContinue()
                is ResultUiEffect.NavigateToMistakes -> onRetryMistakes(effect.lessonId, effect.stepIds)
            }
        }
    }
    val state by viewModel.state.collectAsState()
    ResultContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}
