package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.offset
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.icon.CloseIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.toast_close
import tatlearn.shared.generated.resources.toast_icon_error
import tatlearn.shared.generated.resources.toast_icon_info
import tatlearn.shared.generated.resources.toast_icon_success

/** Уведомление поверх экрана. Закрывается крестиком или свайпом вверх; таймер и очередь — на стороне вызывающего. */
@Composable
fun TamyrToast(
    data: TamyrToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = toastPalette(data.kind)
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = with(LocalDensity.current) { TamyrSizes.toastSwipeThreshold.toPx() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(x = 0, y = dragOffset.roundToInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -swipeThreshold) onDismiss() else dragOffset = 0f
                    },
                    onDragCancel = { dragOffset = 0f },
                    onVerticalDrag = { _, delta -> dragOffset = (dragOffset + delta).coerceAtMost(0f) },
                )
            }
            .solidShadow(
                color = palette.shadow,
                offsetY = TamyrSizes.buttonShadow,
                shape = TamyrTheme.shapes.answer,
            )
            .clip(TamyrTheme.shapes.answer)
            .background(palette.container)
            .border(
                width = TamyrSizes.borderThin,
                color = palette.border,
                shape = TamyrTheme.shapes.answer,
            )
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(
                start = TamyrTheme.spacing.l,
                end = TamyrTheme.spacing.s,
                top = TamyrTheme.spacing.xs,
                bottom = TamyrTheme.spacing.xs,
            ),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = TamyrTheme.spacing.xs)
                .size(TamyrSizes.toastIcon)
                .clip(TamyrTheme.shapes.circle)
                .background(palette.icon),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(palette.glyph),
                style = TamyrTheme.typography.body15Bold,
                color = TamyrTheme.colors.surface,
            )
        }
        Text(
            text = data.text,
            style = TamyrTheme.typography.body14Bold.copy(lineHeight = TamyrTheme.typography.body14Bold.fontSize * 1.35f),
            color = palette.content,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = TamyrTheme.spacing.xs),
        )
        val closeDescription = stringResource(Res.string.toast_close)
        Box(
            modifier = Modifier
                .size(TamyrSizes.toastClose)
                .clip(TamyrTheme.shapes.circle)
                .clickable(role = Role.Button, onClick = onDismiss)
                .semantics { contentDescription = closeDescription },
            contentAlignment = Alignment.Center,
        ) {
            CloseIcon(
                size = TamyrSizes.toastCloseIcon,
                tint = palette.content,
            )
        }
    }
}

private class ToastPalette(
    val container: Color,
    val border: Color,
    val shadow: Color,
    val icon: Color,
    val content: Color,
    val glyph: StringResource,
)

@Composable
private fun toastPalette(kind: TamyrToastKind): ToastPalette = when (kind) {
    TamyrToastKind.Info -> ToastPalette(
        container = TamyrTheme.colors.goldContainer,
        border = TamyrTheme.colors.gold,
        shadow = TamyrTheme.colors.borderStrong,
        icon = TamyrTheme.colors.gold,
        content = TamyrTheme.colors.goldText,
        glyph = Res.string.toast_icon_info,
    )
    TamyrToastKind.Error -> ToastPalette(
        container = TamyrTheme.colors.errorContainer,
        border = TamyrTheme.colors.errorBorder,
        shadow = TamyrTheme.colors.accentSoftShadow,
        icon = TamyrTheme.colors.accent,
        content = TamyrTheme.colors.accentShadow,
        glyph = Res.string.toast_icon_error,
    )
    TamyrToastKind.Success -> ToastPalette(
        container = TamyrTheme.colors.successContainer,
        border = TamyrTheme.colors.successBorder,
        shadow = TamyrTheme.colors.successBorder,
        icon = TamyrTheme.colors.primary,
        content = TamyrTheme.colors.primaryShadow,
        glyph = Res.string.toast_icon_success,
    )
}

@Preview(name = "Info, 1 line")
@Composable
private fun TamyrToastInfoPreview() = PreviewFrame {
    Column(Modifier.padding(TamyrTheme.spacing.screen)) {
        TamyrToast(
            data = TamyrToastData(text = "Вход через Google и VK ID пока недоступен", kind = TamyrToastKind.Info),
            onDismiss = {},
        )
    }
}

@Preview(name = "Error, 2 lines")
@Composable
private fun TamyrToastErrorPreview() = PreviewFrame {
    Column(Modifier.padding(TamyrTheme.spacing.screen)) {
        TamyrToast(
            data = TamyrToastData(text = "Не получилось войти. Проверьте интернет и попробуйте ещё раз", kind = TamyrToastKind.Error),
            onDismiss = {},
        )
    }
}

@Preview(name = "Success, 1 line")
@Composable
private fun TamyrToastSuccessPreview() = PreviewFrame {
    Column(Modifier.padding(TamyrTheme.spacing.screen)) {
        TamyrToast(
            data = TamyrToastData(text = "Пароль изменён", kind = TamyrToastKind.Success),
            onDismiss = {},
        )
    }
}
