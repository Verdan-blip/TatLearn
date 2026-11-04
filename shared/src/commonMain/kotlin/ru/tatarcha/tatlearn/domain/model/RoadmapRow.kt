package ru.tatarcha.tatlearn.domain.model

/** Состояние модуля на ленте: пройден, открыт и идёт сейчас, открыт без прохождения «сейчас» или закрыт. */
enum class ModuleState { Done, Current, Open, Locked }

/**
 * Модуль на ленте с номером (с единицы, по порядку курса) и состоянием.
 * [showsUnlockHint]: подпись «Откроется после …» есть только у ближайшего закрытого модуля, остальным хватает замка.
 */
data class RoadmapEntry(
    val module: LearningModule,
    val number: Int,
    val state: ModuleState,
    val showsUnlockHint: Boolean = false,
)

/**
 * Ряд ленты. Сетка фиксирована: нечётные модули слева, чётные справа, пары (1, 2), (3, 4)… Карточка текущего модуля
 * занимает ряд целиком, его соседа по паре оставляем в своей колонке рядом (под карточкой или над ней), поэтому плитки
 * остальных модулей при смене текущего не переезжают.
 */
sealed interface RoadmapRow {
    data class Tiles(val left: RoadmapEntry?, val right: RoadmapEntry?) : RoadmapRow
    data class Current(val entry: RoadmapEntry) : RoadmapRow
}
