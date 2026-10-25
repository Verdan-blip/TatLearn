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
    /** Интервал между однотипными кнопками, например кнопками входа через соцсети. */
    val tight: Dp = 10.dp,
    /** Интервал между блоками формы и верхний отступ шапки на экране входа. */
    val section: Dp = 18.dp,
    val xl: Dp = 20.dp,
    /** Интервал между крупными блоками главной. */
    val blockGap: Dp = 14.dp,
    val sectionInset: Dp = 22.dp,
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
    val diamondLoader = 11.dp
    val diamondGap = 5.dp
    val loaderGap = 8.dp
    val borderThin = 1.dp
    val borderAccent = 2.dp
    val providerMark = 18.dp
    val toastIcon = 26.dp
    val spinner = 22.dp
    val homeAvatar = 46.dp
    val moduleChip = 34.dp
    val lessonProgress = 8.dp
    val moduleProgress = 6.dp
    val lessonButton = 46.dp
    val streakBadgeCompact = 40.dp
    val navIcon = 20.dp
    val topBarAction = 48.dp
    val lessonStatus = 40.dp
    val lessonRowMinHeight = 64.dp
    val checkIcon = 22.dp
    /** Тень плитки сопоставления, пока выбрано слово напротив (на 1dp глубже обычной). */
    val tileLiftShadow = 4.dp
    val sheetHandleWidth = 40.dp
    val sheetHandleHeight = 4.dp
    val closeTargetPadding = 12.dp
    val resultMascot = 140.dp
    val resultMascotCompact = 110.dp
    val thresholdBar = 14.dp
    val thresholdMarkerWidth = 3.dp
    val thresholdMarkerHeight = 22.dp
    val spinnerStroke = 3.dp
    val toastClose = 32.dp
    val toastCloseIcon = 24.dp
    val toastSwipeThreshold = 24.dp
}

/** Тайминги анимаций; макет статичный, поэтому они наши собственные (короткие и спокойные). */
object TamyrMotion {
    const val FAST_MS = 150
    const val STEP_MS = 280
    const val PROGRESS_MS = 400
    const val TOAST_VISIBLE_MS = 5_000L
    const val SPINNER_MS = 900
    /** Покачивание неверной пары в сопоставлении (два цикла ±4dp). */
    const val SHAKE_MS = 300
}
