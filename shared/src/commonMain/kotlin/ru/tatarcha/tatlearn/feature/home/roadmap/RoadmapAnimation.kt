package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.domain.model.ProgressGain

private const val FULL = 100
private val InOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

/**
 * Анимация главной после урока (design/home-animations.md, D и E). Две формы одного прироста:
 * [isLessonGain]: прибавился урок в текущем модуле, эчпочмак «румянится», счётчик меняется, всплывает «+1 урок»;
 * [isModuleDone]: модуль закончен, на его плитке опускается печать «Тәмам», тропа до нового модуля заливается золотом,
 * у новой карточки вспыхивает кольцо и срабатывает один лёгкий гаптик.
 * Пока [running] ложно, лента показывает обычное состояние; одновременно идёт одна анимация.
 */
@Stable
class RoadmapAnimation(val gain: ProgressGain?, private val reduceMotion: Boolean) {
    val isLessonGain: Boolean = gain?.card?.let { it.toCompleted > it.fromCompleted } == true
    val isModuleDone: Boolean = gain != null && gain.card == null && gain.toPercent >= FULL

    /** Заливка нового эчпочмака текущей карточки снизу вверх, 0..1. */
    val eggFill = Animatable(0f)
    val ring = Animatable(0f)
    val pulse = Animatable(1f)
    val sparks = Animatable(0f)
    val chip = Animatable(0f)

    /** Счётчик «N/M» карточки меняется в середине «румянца»; до этого показывает старое значение. */
    var counterReached by mutableStateOf(!isLessonGain)

    /** Печать «Тәмам» на плитке завершённого модуля: 0 (нет) .. 1 (стоит). */
    val stamp = Animatable(if (isModuleDone) 0f else 1f)

    /** Сколько ромбов тропы до нового модуля уже золотые, 0..1. */
    val trail = Animatable(if (isModuleDone) 0f else 1f)

    var running by mutableStateOf(gain != null)
        private set

    val isActive: Boolean get() = gain != null

    internal suspend fun play(awaitVisible: suspend () -> Boolean, onUnlocked: () -> Unit) {
        if (gain == null) return
        // Карточки нет на экране дольше 3 секунд: итог без движения.
        val visible = awaitVisible()
        if (!visible) {
            settle()
            return
        }
        when {
            reduceMotion -> reduced()
            isLessonGain -> lessonGain()
            isModuleDone -> moduleDone(onUnlocked)
        }
        settle()
    }

    private suspend fun lessonGain() {
        delay(TamyrMotion.GAIN_DELAY_MS.toLong())
        coroutineScope {
            launch { ring.animateTo(1f, tween(TamyrMotion.GAIN_RING_IN_MS)) }
            eggFill.animateTo(1f, tween(TamyrMotion.GAIN_GROW_MS, easing = InOut))
        }
        counterReached = true
        coroutineScope {
            launch {
                pulse.animateTo(PULSE_PEAK, spring(dampingRatio = PULSE_DAMPING, stiffness = Spring.StiffnessMedium))
                pulse.animateTo(1f, spring(dampingRatio = PULSE_DAMPING, stiffness = Spring.StiffnessMedium))
            }
            launch { sparks.animateTo(1f, tween(SPARKS_MS)) }
            launch {
                delay(CHIP_AFTER_PULSE_MS)
                chip.animateTo(1f, tween(TamyrMotion.GAIN_CHIP_IN_MS))
                delay(TamyrMotion.GAIN_CHIP_HOLD_MS.toLong())
                chip.animateTo(0f, tween(TamyrMotion.GAIN_CHIP_OUT_MS))
            }
            launch {
                delay(RING_OUT_AFTER_MS)
                ring.animateTo(0f, tween(TamyrMotion.GAIN_RING_OUT_MS))
            }
        }
    }

    private suspend fun moduleDone(onUnlocked: () -> Unit) {
        delay(TamyrMotion.GAIN_DELAY_MS.toLong())
        stamp.animateTo(1f, tween(STAMP_MS, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
        delay(STAMP_GAP_MS)
        trail.animateTo(1f, tween(TRAIL_MS, easing = InOut))
        onUnlocked()
        ring.animateTo(1f, tween(TamyrMotion.GAIN_RING_IN_MS))
        delay(RING_HOLD_MS)
        ring.animateTo(0f, tween(TamyrMotion.GAIN_RING_OUT_MS))
    }

    /** «Уменьшить анимацию»: итог сразу, подсветка держится [TamyrMotion.GAIN_REDUCED_HOLD_MS]. */
    private suspend fun reduced() {
        eggFill.snapTo(1f)
        counterReached = true
        stamp.snapTo(1f)
        trail.snapTo(1f)
        ring.snapTo(1f)
        if (isLessonGain) chip.snapTo(1f)
        delay(TamyrMotion.GAIN_REDUCED_HOLD_MS.toLong())
    }

    private suspend fun settle() {
        ring.snapTo(0f)
        chip.snapTo(0f)
        sparks.snapTo(0f)
        stamp.snapTo(1f)
        trail.snapTo(1f)
        counterReached = true
        running = false
    }

    companion object {
        private const val PULSE_PEAK = 1.15f
        private const val PULSE_DAMPING = 0.6f
        private const val SPARKS_MS = 400
        private const val CHIP_AFTER_PULSE_MS = 100L
        private const val RING_OUT_AFTER_MS = 1100L
        private const val STAMP_MS = 250
        private const val STAMP_GAP_MS = 150L
        private const val TRAIL_MS = 800
        private const val RING_HOLD_MS = 500L
    }
}

/** Создаёт анимацию под прирост [gain] и проигрывает её один раз; [onStarted] и [onFinished] сообщают модели экрана. */
@Composable
fun rememberRoadmapAnimation(
    gain: ProgressGain?,
    reduceMotion: Boolean,
    awaitVisible: suspend () -> Boolean,
    onUnlocked: () -> Unit,
    onStarted: () -> Unit,
    onFinished: () -> Unit,
): RoadmapAnimation {
    val animation = remember(gain, reduceMotion) { RoadmapAnimation(gain, reduceMotion) }
    val started by rememberUpdatedState(onStarted)
    val finished by rememberUpdatedState(onFinished)
    val visible by rememberUpdatedState(awaitVisible)
    val unlocked by rememberUpdatedState(onUnlocked)
    LaunchedEffect(animation) {
        if (gain == null) return@LaunchedEffect
        started()
        animation.play(awaitVisible = { visible() }, onUnlocked = { unlocked() })
        finished()
    }
    return animation
}
