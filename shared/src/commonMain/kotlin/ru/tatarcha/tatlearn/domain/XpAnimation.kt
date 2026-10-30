package ru.tatarcha.tatlearn.domain

import ru.tatarcha.tatlearn.domain.model.UserStats
import kotlin.math.roundToInt

/** Кадр «набегания» XP: показанные XP, уровень по ним и доля полосы уровня. */
data class XpFrame(val xp: Int, val level: Int, val barFraction: Float)

/**
 * XP растёт от [before] к [after] по [progress] (0..1). Уровень и полоса считаются по показанным XP, поэтому, если урок
 * перевёл через границу уровня, полоса доезжает до конца и тихо начинается заново: отдельного события «новый уровень» нет.
 */
fun xpAnimationFrame(before: Int, after: Int, progress: Float): XpFrame {
    val xp = before + ((after - before) * progress.coerceIn(0f, 1f)).roundToInt()
    return XpFrame(
        xp = xp,
        level = xp / UserStats.XP_PER_LEVEL + 1,
        barFraction = (xp % UserStats.XP_PER_LEVEL) / UserStats.XP_PER_LEVEL.toFloat(),
    )
}
