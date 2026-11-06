package ru.tatarcha.tatlearn.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.pt_sans_bold
import tatlearn.shared.generated.resources.pt_sans_italic
import tatlearn.shared.generated.resources.pt_sans_regular

/** Шкала шрифтов из дизайна; в именах — размер в px из макета. */
@Immutable
data class TamyrTypography(
    val headline32: TextStyle,
    /** Слово на карточке нового слова. */
    val display40: TextStyle,
    val display30: TextStyle,
    val headline26: TextStyle,
    val headline24: TextStyle,
    val title22: TextStyle,
    val title20: TextStyle,
    /** Перевод на карточке нового слова. */
    val body20: TextStyle,
    val title19: TextStyle,
    val title18: TextStyle,
    val body17: TextStyle,
    val body17Bold: TextStyle,
    val body16: TextStyle,
    val body16Bold: TextStyle,
    val body15: TextStyle,
    val body15Bold: TextStyle,
    val body14: TextStyle,
    val body14Bold: TextStyle,
    val caption13: TextStyle,
    val caption13Bold: TextStyle,
    val label12: TextStyle,
    val label12Bold: TextStyle,
    /** Тег раздела заглавными (например, «ТЫҢЛА · АУДИРОВАНИЕ»). Верхний регистр задаётся в самом тексте. */
    val tag: TextStyle,
    val micro11Bold: TextStyle,
    /** Транскрипция, например [kumán]. */
    val transcription: TextStyle,
    val mono12: TextStyle,
)

/** PT Sans лежит в composeResources/font; в нём есть ә ө ү җ ң һ (проверено через fontTools). */
@Composable
fun rememberTamyrFontFamily(): FontFamily {
    val regular = Font(Res.font.pt_sans_regular, FontWeight.Normal)
    val bold = Font(Res.font.pt_sans_bold, FontWeight.Bold)
    val italic = Font(Res.font.pt_sans_italic, FontWeight.Normal, FontStyle.Italic)
    return remember(regular, bold, italic) { FontFamily(regular, bold, italic) }
}

fun tamyrTypography(family: FontFamily): TamyrTypography {
    fun style(size: Int, bold: Boolean = false, lineHeightEm: Float = 1.3f) = TextStyle(
        fontFamily = family,
        fontSize = size.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        lineHeight = (size * lineHeightEm).sp,
    )
    return TamyrTypography(
        headline32 = style(32, bold = true, lineHeightEm = 1.15f),
        display40 = style(40, bold = true, lineHeightEm = 1.12f),
        display30 = style(30, bold = true, lineHeightEm = 1.15f),
        headline26 = style(26, bold = true),
        headline24 = style(24, bold = true),
        title22 = style(22, bold = true),
        title20 = style(20, bold = true),
        body20 = style(20),
        title19 = style(19, bold = true),
        title18 = style(18, bold = true),
        body17 = style(17),
        body17Bold = style(17, bold = true),
        body16 = style(16, lineHeightEm = 1.45f),
        body16Bold = style(16, bold = true),
        body15 = style(15, lineHeightEm = 1.4f),
        body15Bold = style(15, bold = true),
        body14 = style(14),
        body14Bold = style(14, bold = true),
        caption13 = style(13),
        caption13Bold = style(13, bold = true),
        label12 = style(12),
        label12Bold = style(12, bold = true),
        tag = style(12, bold = true).copy(letterSpacing = 0.06.em),
        micro11Bold = style(11, bold = true),
        transcription = style(15).copy(fontStyle = FontStyle.Italic),
        mono12 = style(12).copy(fontFamily = FontFamily.Monospace),
    )
}
