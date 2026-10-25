package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

/** Содержимое вкладки «Профиль» внутри главной; сама вкладка и меню принадлежат главной. */
@Composable
fun ProfileScene(
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDebug: (() -> Unit)? = null,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileUiEffect.NavigateToEdit -> onEdit()
            }
        }
    }
    val state by viewModel.state.collectAsState()
    ProfileContent(state = state, onEvent = viewModel::onEvent, modifier = modifier, onOpenDebug = onOpenDebug)
}
