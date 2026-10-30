package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.CardGain
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.ProgressGain

/**
 * Помнит, какой прогресс главная уже показала пользователю, чтобы после урока проиграть «доезд» ровно один раз.
 * Живёт на время процесса: при холодном старте сравнивать не с чем, анимации нет.
 * Снимок обновляется только когда экран реально показал состояние ([commit]): старый экран главной под уроком
 * тоже получает новый прогресс, но не должен «съесть» анимацию раньше нового.
 */
class HomeProgressTracker {
    private var percents: Map<String, Int>? = null
    private var cardModuleId: String? = null
    private var cardCompleted: Int = 0

    /** Прирост относительно показанного снимка или null (нет снимка, ничего не выросло). Ничего не меняет. */
    fun gainFor(home: HomeContent): ProgressGain? {
        val shown = percents ?: return null
        val grown = home.modules.firstOrNull { module -> module.progressPercent > (shown[module.id] ?: module.progressPercent) } ?: return null
        val current = home.currentLesson
        val card = if (current.moduleId == grown.id && cardModuleId == grown.id) {
            CardGain(fromCompleted = cardCompleted, toCompleted = current.completedLessons, total = current.totalLessons)
        } else {
            null
        }
        return ProgressGain(moduleId = grown.id, fromPercent = shown.getValue(grown.id), toPercent = grown.progressPercent, card = card)
    }

    /** Запоминает, что пользователь увидел [home] (или начал видеть анимацию к нему). */
    fun commit(home: HomeContent) {
        percents = home.modules.associate { it.id to it.progressPercent }
        cardModuleId = home.currentLesson.moduleId
        cardCompleted = home.currentLesson.completedLessons
    }
}
