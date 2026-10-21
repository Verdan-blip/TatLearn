package ru.tatarcha.tatlearn.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Шаги отступов, замеченные в макете. Боковой отступ экрана — [screen] (24dp). */
@Immutable
data class TamyrSpacing(
    val xxs: Dp = 4.dp,
    val xs: Dp = 6.dp,
    val s: Dp = 8.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val screen: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val bottomInset: Dp = 36.dp,
)

/** Фиксированные размеры компонентов из макета. */
object TamyrSizes {
    val buttonHeight = 54.dp
    val buttonHeightSecondary = 50.dp
    val buttonShadow = 3.dp
    val progressHeight = 10.dp
    val diamondBrand = 9.dp
    val diamondLife = 10.dp
    val diamondLoader = 11.dp
    val diamondGap = 5.dp
    val livesGap = 4.dp
    val loaderGap = 8.dp
    val borderThin = 1.dp
    val borderAccent = 2.dp
}

/** Тайминги анимаций; макет статичный, поэтому они наши собственные (короткие и спокойные). */
object TamyrMotion {
    const val FAST_MS = 150
    const val STEP_MS = 280
    const val PROGRESS_MS = 400
}
