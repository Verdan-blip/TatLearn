package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/**
 * Лист подтверждения снизу экрана: затемнение, заголовок, пояснение, главное действие «остаться» и тихое «выйти».
 * Занимает весь экран, поэтому кладётся поверх содержимого в общем `Box`.
 * Главная (зелёная) кнопка — [dismissText] с действием [onDismiss]; тихая — [confirmText] с [onConfirm].
 * Нажатие на затемнение вызывает [onScrimClick] (по умолчанию [onDismiss]); [content] — необязательный блок под пояснением.
 */
@Composable
fun TamyrConfirmSheet(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onScrimClick: () -> Unit = onDismiss,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(TamyrMotion.FAST_MS)),
            exit = fadeOut(tween(TamyrMotion.FAST_MS)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TamyrTheme.colors.scrim)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onScrimClick,
                    ),
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(TamyrMotion.STEP_MS)) { it },
            exit = slideOutVertically(tween(TamyrMotion.STEP_MS)) { it },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(TamyrTheme.shapes.sheet)
                    .background(TamyrTheme.colors.surface)
                    .border(TamyrSizes.borderThin, TamyrTheme.colors.border, TamyrTheme.shapes.sheet)
                    .navigationBarsPadding()
                    .padding(
                        start = TamyrTheme.spacing.screen,
                        end = TamyrTheme.spacing.screen,
                        top = TamyrTheme.spacing.m,
                        bottom = TamyrTheme.spacing.l,
                    ),
                verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = TamyrSizes.sheetHandleWidth, height = TamyrSizes.sheetHandleHeight)
                        .clip(TamyrTheme.shapes.progress)
                        .background(TamyrTheme.colors.borderStrong),
                )
                Text(
                    text = title,
                    style = TamyrTheme.typography.title20,
                    color = TamyrTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = TamyrTheme.spacing.xs),
                )
                Text(
                    text = message,
                    style = TamyrTheme.typography.body15,
                    color = TamyrTheme.colors.textSecondary,
                )
                content?.invoke(this)
                TamyrButton(
                    text = dismissText,
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = TamyrTheme.spacing.xs),
                )
                TamyrTextButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "Visible")
@Composable
private fun TamyrConfirmSheetPreview() = PreviewFrame {
    TamyrConfirmSheet(
        visible = true,
        title = "Выйти из урока?",
        message = "Уже проверенные ответы и XP сохранятся, остальное придётся пройти заново.",
        confirmText = "Выйти",
        dismissText = "Остаться",
        onConfirm = {},
        onDismiss = {},
    )
}

@Preview(name = "With content")
@Composable
private fun TamyrConfirmSheetContentPreview() = PreviewFrame {
    TamyrConfirmSheet(
        visible = true,
        title = "Напомнить об уроке?",
        message = "Выбери время, и Мыраужан раз в день напомнит позаниматься.",
        confirmText = "Не сейчас",
        dismissText = "Включить",
        onConfirm = {},
        onDismiss = {},
        content = {
            TamyrChoiceChips(
                options = listOf("08:00", "13:00", "19:00", "21:00"),
                selectedIndex = 2,
                onSelect = {},
            )
        },
    )
}
