package ru.tatarcha.tatlearn.feature.intro

import ru.tatarcha.tatlearn.core.common.UiEffect
import ru.tatarcha.tatlearn.core.common.UiEvent
import ru.tatarcha.tatlearn.core.common.UiState
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.NameError
import ru.tatarcha.tatlearn.domain.model.ProfileRules

data class IntroUiState(
    val name: String = "",
    /** На знакомстве выбирается только цвет аватара; позы Мыраужана доступны в профиле. */
    val avatar: Avatar.Color = Avatar.Color(0),
) : UiState {
    /** Пустое имя на знакомстве не ошибка: сохранится имя по умолчанию. */
    val nameError: NameError? get() = ProfileRules.validate(name).takeIf { it == NameError.TooLong }
    val canContinue: Boolean get() = nameError == null

    /** Счётчик «N/20» появляется с [ProfileRules.COUNTER_FROM]-го символа. */
    val counter: Pair<Int, Int>?
        get() = ProfileRules.normalize(name).length.takeIf { it >= ProfileRules.COUNTER_FROM }?.let { it to ProfileRules.MAX_NAME_LENGTH }
}

sealed interface IntroUiEvent : UiEvent {
    data class NameChanged(val name: String) : IntroUiEvent
    data class ColorSelected(val avatar: Avatar) : IntroUiEvent
    data object ContinueClicked : IntroUiEvent
    data object SkipClicked : IntroUiEvent
}

sealed interface IntroUiEffect : UiEffect {
    data object NavigateHome : IntroUiEffect
}
