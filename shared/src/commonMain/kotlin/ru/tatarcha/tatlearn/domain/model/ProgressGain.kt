package ru.tatarcha.tatlearn.domain.model

/**
 * Прирост прогресса, который главная показывает после урока: модуль [moduleId] вырос с [fromPercent] до [toPercent].
 * [card] не null, если карточка «Продолжить» показывает тот же модуль (у неё свой счётчик «N / M»).
 */
data class ProgressGain(
    val moduleId: String,
    val fromPercent: Int,
    val toPercent: Int,
    val card: CardGain?,
)

data class CardGain(val fromCompleted: Int, val toCompleted: Int, val total: Int) {
    /** Подпись «+1 урок» показывается, только когда прибавился ровно один урок. */
    val showsChip: Boolean get() = toCompleted - fromCompleted == 1
}
