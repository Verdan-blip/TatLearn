package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.HomeCardKind
import ru.tatarcha.tatlearn.domain.model.HomeContent
import ru.tatarcha.tatlearn.domain.model.ModuleState
import ru.tatarcha.tatlearn.domain.model.RoadmapEntry
import ru.tatarcha.tatlearn.domain.model.RoadmapRow

/** Раскладывает модули главной по рядам ленты (макет 03, 45–49). Не зависит от UI, поэтому проверяется тестами. */
object RoadmapBuilder {

    fun build(content: HomeContent): List<RoadmapRow> {
        val entries = entries(content)
        val rows = mutableListOf<RoadmapRow>()
        entries.chunked(PAIR).forEach { pair ->
            val left = pair[0]
            val right = pair.getOrNull(1)
            when {
                left.state == ModuleState.Current -> {
                    rows += RoadmapRow.Current(left)
                    if (right != null) rows += RoadmapRow.Tiles(left = null, right = right)
                }
                right?.state == ModuleState.Current -> {
                    rows += RoadmapRow.Tiles(left = left, right = null)
                    rows += RoadmapRow.Current(right)
                }
                else -> rows += RoadmapRow.Tiles(left, right)
            }
        }
        return rows
    }

    internal fun entries(content: HomeContent): List<RoadmapEntry> {
        val currentId = content.currentLesson.moduleId.takeIf { content.currentLesson.kind != HomeCardKind.AllDone }
        var hintGiven = false
        return content.modules.mapIndexed { index, module ->
            val state = when {
                module.isLocked -> ModuleState.Locked
                module.id == currentId -> ModuleState.Current
                module.totalLessons > 0 && module.completedLessons == module.totalLessons -> ModuleState.Done
                else -> ModuleState.Open
            }
            val hint = state == ModuleState.Locked && !hintGiven
            if (hint) hintGiven = true
            RoadmapEntry(module = module, number = index + 1, state = state, showsUnlockHint = hint)
        }
    }

    private const val PAIR = 2
}
