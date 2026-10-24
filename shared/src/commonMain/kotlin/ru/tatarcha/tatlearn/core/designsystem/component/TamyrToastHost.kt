package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Место показа уведомления: сверху экрана, поверх шапки. Новое уведомление заменяет предыдущее. */
@Composable
fun TamyrToastHost(
    toast: TamyrToastData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = toast,
        modifier = modifier
            .fillMaxWidth()
            .safeDrawingPadding()
            .padding(
                horizontal = TamyrTheme.spacing.screen,
                vertical = TamyrTheme.spacing.l,
            ),
        contentAlignment = Alignment.TopCenter,
        transitionSpec = {
            ContentTransform(
                targetContentEnter = slideInVertically(tween(TamyrMotion.STEP_MS)) { -it } + fadeIn(tween(TamyrMotion.FAST_MS)),
                initialContentExit = slideOutVertically(tween(TamyrMotion.STEP_MS)) { -it } + fadeOut(tween(TamyrMotion.FAST_MS)),
            )
        },
        label = "toast",
    ) { current ->
        if (current != null) {
            TamyrToast(
                data = current,
                onDismiss = onDismiss,
            )
        }
    }
}

@Preview(name = "Toast shown")
@Composable
private fun TamyrToastHostPreview() = PreviewFrame {
    TamyrToastHost(
        toast = TamyrToastData(text = "Пароль изменён", kind = TamyrToastKind.Success),
        onDismiss = {},
    )
}
