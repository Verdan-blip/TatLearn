package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileEditScene(
    onClose: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProfileEditUiEffect.Close -> onClose()
            }
        }
    }
    val state by viewModel.state.collectAsState()
    // Системный «назад» проходит через ту же проверку несохранённых изменений, что и стрелка.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = { viewModel.onEvent(ProfileEditUiEvent.BackClicked) },
    )
    ProfileEditContent(state = state, onEvent = viewModel::onEvent)
}
