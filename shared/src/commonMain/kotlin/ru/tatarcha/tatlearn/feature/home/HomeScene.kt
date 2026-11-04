package ru.tatarcha.tatlearn.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ru.tatarcha.tatlearn.core.common.ReduceMotion
import ru.tatarcha.tatlearn.feature.profile.ProfileScene

@Composable
fun HomeScene(
    onStartLesson: (lessonId: String) -> Unit,
    onOpenModule: (moduleId: String) -> Unit,
    onEditProfile: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDebug: () -> Unit,
    showDebugTools: Boolean = false,
    viewModel: HomeViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToLesson -> onStartLesson(effect.lessonId)
                is HomeUiEffect.NavigateToModule -> onOpenModule(effect.moduleId)
            }
        }
    }
    val state by viewModel.state.collectAsState()
    val reduceMotion = koinInject<ReduceMotion>()
    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        reduceMotion = remember { reduceMotion.isEnabled() },
        profileTab = { modifier ->
            ProfileScene(
                onEdit = onEditProfile,
                onOpenAbout = onOpenAbout,
                modifier = modifier,
                // Вход в инструменты отладки живёт на вкладке «Профиль» и только в отладочной сборке.
                onOpenDebug = if (showDebugTools) onOpenDebug else null,
            )
        },
    )
}
