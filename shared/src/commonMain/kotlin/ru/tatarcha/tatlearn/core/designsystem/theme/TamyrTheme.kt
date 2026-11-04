package ru.tatarcha.tatlearn.core.designsystem.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalColors = staticCompositionLocalOf<TamyrColors> { error("TamyrTheme not provided") }
private val LocalTypography = staticCompositionLocalOf<TamyrTypography> { error("TamyrTheme not provided") }
private val LocalShapes = staticCompositionLocalOf { TamyrShapes() }
private val LocalSpacing = staticCompositionLocalOf { TamyrSpacing() }

object TamyrTheme {
    val colors: TamyrColors @Composable @ReadOnlyComposable get() = LocalColors.current
    val typography: TamyrTypography @Composable @ReadOnlyComposable get() = LocalTypography.current
    val shapes: TamyrShapes @Composable @ReadOnlyComposable get() = LocalShapes.current
    val spacing: TamyrSpacing @Composable @ReadOnlyComposable get() = LocalSpacing.current
}

/** В дизайне только светлая палитра; тёмная тема пока намеренно не поддерживается. */
@Composable
fun TamyrTheme(content: @Composable () -> Unit) {
    val colors = LightTamyrColors
    val typography = tamyrTypography(rememberTamyrFontFamily())
    // Material нужен лишь как запасной вариант для случайных компонентов M3; наши компоненты читают TamyrTheme.
    val material = lightColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        background = colors.background,
        onBackground = colors.textPrimary,
        surface = colors.surface,
        onSurface = colors.textPrimary,
        error = colors.accent,
        outline = colors.border,
    )
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides typography,
    ) {
        MaterialTheme(colorScheme = material) {
            // Провайдер внутри MaterialTheme: иначе он вернёт стандартную волну Material.
            CompositionLocalProvider(LocalIndication provides TamyrPressIndication) {
                content()
            }
        }
    }
}
