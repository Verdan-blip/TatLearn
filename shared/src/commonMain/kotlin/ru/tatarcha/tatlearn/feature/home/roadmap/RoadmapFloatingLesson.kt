package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.icon.ChevronDownIcon
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.roadmap_to_current_down
import tatlearn.shared.generated.resources.roadmap_to_current_up

private const val UP_ROTATION = 180f

/**
 * Плавающая кнопка урока: видна, пока кнопка в карточке текущего модуля не помещается на экран. Тап по кнопке начинает урок,
 * стрелка прокручивает ленту к карточке (вверх, если карточка выше экрана, вниз, если ниже).
 */
@Composable
fun RoadmapFloatingLesson(
    lessonText: String,
    pointsUp: Boolean,
    onStart: () -> Unit,
    onScrollToCurrent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(if (pointsUp) Res.string.roadmap_to_current_up else Res.string.roadmap_to_current_down)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, TamyrTheme.colors.background, TamyrTheme.colors.background)))
            .padding(
                start = TamyrTheme.spacing.screen,
                end = TamyrTheme.spacing.screen,
                top = TamyrTheme.spacing.xl,
                bottom = TamyrTheme.spacing.m,
            ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.tight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(TamyrSizes.buttonHeight)
                    .solidShadow(TamyrTheme.colors.border, TamyrSizes.buttonShadow, TamyrTheme.shapes.button)
                    .clip(TamyrTheme.shapes.button)
                    .background(TamyrTheme.colors.surface)
                    .border(TamyrSizes.borderThin, TamyrTheme.colors.border, TamyrTheme.shapes.button)
                    .clickable(role = Role.Button, onClick = onScrollToCurrent)
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                ChevronDownIcon(modifier = Modifier.rotate(if (pointsUp) UP_ROTATION else 0f))
            }
            TamyrButton(
                text = lessonText,
                onClick = onStart,
                modifier = Modifier.weight(1f).height(TamyrSizes.buttonHeight),
            )
        }
    }
}

@Preview
@Composable
private fun RoadmapFloatingLessonPreview() = PreviewFrame {
    RoadmapFloatingLesson(
        lessonText = "Урок 4 · Туганнар",
        pointsUp = true,
        onStart = {},
        onScrollToCurrent = {},
    )
}
