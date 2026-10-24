package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.button_loading

enum class TamyrButtonStyle {
    /** Зелёная заливка, главный призыв к действию. */
    Primary,

    /** Терракотовая заливка (например, «Понятно» после неверного ответа). */
    Accent,

    /** Кремовая с рамкой, тёмный текст (вход через соцсети). */
    Secondary,

    /** Кремовая с рамкой, терракотовый текст (офлайн-режим). */
    SecondaryAccent,
}

@Composable
fun TamyrButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TamyrButtonStyle = TamyrButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = TamyrTheme.colors
    val shape = TamyrTheme.shapes.button
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val loadingDescription = stringResource(Res.string.button_loading)

    val container: Color
    val content: Color
    val shadow: Color?
    val border: Color?
    when {
        !enabled -> { container = colors.surfaceMuted; content = colors.textDisabled; shadow = null; border = null }
        style == TamyrButtonStyle.Primary -> { container = colors.primary; content = colors.onPrimary; shadow = colors.primaryShadow; border = null }
        style == TamyrButtonStyle.Accent -> { container = colors.accent; content = colors.onAccent; shadow = colors.accentShadow; border = null }
        style == TamyrButtonStyle.Secondary -> { container = colors.surface; content = colors.textPrimary; shadow = null; border = colors.border }
        else -> { container = colors.surface; content = colors.accent; shadow = null; border = colors.border }
    }
    val secondary = border != null
    val height = if (secondary) TamyrSizes.buttonHeightSecondary else TamyrSizes.buttonHeight

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Нажатие: кнопка «тонет» в собственной тени, как физическая клавиша.
            .graphicsLayer { translationY = if (pressed && shadow != null) TamyrSizes.buttonShadow.toPx() else 0f }
            .then(if (shadow != null && !pressed) Modifier.solidShadow(shadow, TamyrSizes.buttonShadow, shape) else Modifier)
            .height(height)
            .clip(shape)
            .background(container)
            .then(if (border != null) Modifier.border(BorderStroke(TamyrSizes.borderThin, border), shape) else Modifier)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .semantics { if (loading) stateDescription = loadingDescription }
            .padding(horizontal = TamyrTheme.spacing.l),
        contentAlignment = Alignment.Center,
    ) {
        // Подпись остаётся в дереве под прозрачностью: так кнопка не меняет ширину, а TalkBack/VoiceOver читает действие.
        Text(
            text = text,
            color = content,
            style = if (secondary) TamyrTheme.typography.body16Bold else TamyrTheme.typography.body17Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(if (loading) 0f else 1f),
        )
        if (loading) TamyrSpinner(color = content)
    }
}

/** Жирная терракотовая ссылка без рамки, например «У меня уже есть аккаунт». */
@Composable
fun TamyrTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = TamyrTheme.colors.accentText,
) {
    Text(
        text = text,
        color = color,
        style = TamyrTheme.typography.body15Bold,
        textAlign = TextAlign.Center,
        modifier = modifier.clickable(role = Role.Button, onClick = onClick).padding(TamyrTheme.spacing.s),
    )
}

@Preview(name = "Loading")
@Composable
private fun TamyrButtonLoadingPreview() = PreviewFrame {
    Column(
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.l),
    ) {
        TamyrButton(
            text = "Керергә · Войти",
            onClick = {},
            loading = true,
        )
        TamyrButton(
            text = "Аңладым · Понятно",
            onClick = {},
            style = TamyrButtonStyle.Accent,
            loading = true,
        )
    }
}
