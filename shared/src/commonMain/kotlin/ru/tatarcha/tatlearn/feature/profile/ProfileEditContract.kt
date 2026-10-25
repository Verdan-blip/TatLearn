package ru.tatarcha.tatlearn.feature.profile

import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.NameError
import ru.tatarcha.tatlearn.domain.model.ProfileRules

/** Экран изменения профиля (макеты 27, 30, 31, 32). [initialName] и [initialAvatar] нужны, чтобы знать, есть ли несохранённые изменения. */
data class ProfileEditUiState(
    val name: String = "",
    val avatar: Avatar = Avatar.Default,
    val initialName: String = "",
    val initialAvatar: Avatar = Avatar.Default,
    /** Лист «Выйти без сохранения?». */
    val exitSheet: Boolean = false,
) : UiState {
    val nameError: NameError? get() = ProfileRules.validate(name)
    val hasChanges: Boolean get() = ProfileRules.normalize(name) != initialName || avatar != initialAvatar

    /** «Сохранить» активна только при изменениях и допустимом имени. */
    val canSave: Boolean get() = hasChanges && nameError == null

    /** Счётчик «N/20» появляется с [ProfileRules.COUNTER_FROM]-го символа. */
    val counter: Pair<Int, Int>?
        get() = ProfileRules.normalize(name).length.takeIf { it >= ProfileRules.COUNTER_FROM }?.let { it to ProfileRules.MAX_NAME_LENGTH }
}

sealed interface ProfileEditUiEvent : UiEvent {
    data class NameChanged(val name: String) : ProfileEditUiEvent
    data class AvatarSelected(val avatar: Avatar) : ProfileEditUiEvent
    data object SaveClicked : ProfileEditUiEvent

    /** Стрелка «назад» или системный жест: с несохранёнными изменениями открывает лист подтверждения. */
    data object BackClicked : ProfileEditUiEvent
    data object ExitConfirmed : ProfileEditUiEvent
    data object ExitDismissed : ProfileEditUiEvent
}

sealed interface ProfileEditUiEffect : UiEffect {
    data object Close : ProfileEditUiEffect
}
