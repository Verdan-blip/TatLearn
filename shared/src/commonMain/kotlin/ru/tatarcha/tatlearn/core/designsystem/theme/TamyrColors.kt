package ru.tatarcha.tatlearn.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Семантические цветовые токены, извлечённые из design/Tatar App Screens.dc.html. */
@Immutable
data class TamyrColors(
    val background: Color,
    val surface: Color,
    /** Блоки скелета, дорожка прогресса, неактивные заливки. */
    val surfaceMuted: Color,
    val border: Color,
    /** Неактивные точки пейджера. */
    val borderStrong: Color,
    /** Пунктирная строка ответа в сборке предложения. */
    val dashedLine: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,

    val primary: Color,
    /** Сплошная «3D»-тень под главными элементами. */
    val primaryShadow: Color,
    val onPrimary: Color,

    val accent: Color,
    val accentShadow: Color,
    val accentSoftShadow: Color,
    val onAccent: Color,

    val gold: Color,
    val goldText: Color,
    val goldContainer: Color,

    val successContainer: Color,
    val successBorder: Color,
    val errorContainer: Color,
    val errorBorder: Color,

    val cardShadow: Color,
)

val LightTamyrColors = TamyrColors(
    background = Color(0xFFF3EEDF),
    surface = Color(0xFFFFFCF3),
    surfaceMuted = Color(0xFFEAE3CB),
    border = Color(0xFFE5DEC6),
    borderStrong = Color(0xFFD9CFB2),
    dashedLine = Color(0xFFC9C0A4),

    textPrimary = Color(0xFF3D3A2D),
    textSecondary = Color(0xFF5A5642),
    textDisabled = Color(0xFFA39D85),

    primary = Color(0xFF3B6B45),
    primaryShadow = Color(0xFF2C5234),
    onPrimary = Color(0xFFF3EEDF),

    accent = Color(0xFFB5522F),
    accentShadow = Color(0xFF8A4326),
    accentSoftShadow = Color(0xFFE0C9BC),
    onAccent = Color(0xFFFFFCF3),

    gold = Color(0xFFC99A34),
    goldText = Color(0xFF8A6F1F),
    goldContainer = Color(0xFFEFE6C8),

    successContainer = Color(0xFFD9EFD8),
    successBorder = Color(0xFFA9CBA8),
    errorContainer = Color(0xFFF4DFD4),
    errorBorder = Color(0xFFDFB9A5),

    cardShadow = Color(0x243D3A2D), // rgba(61,58,45,.14)
)
