package ru.tatarcha.tatlearn.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.LoadingState
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.SkeletonBlock
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private val OptionHeight = 56.dp

/**
 * Скелет экрана урока: шапка с крестиком и полосой, тег, заголовок, карточка задания и варианты ответа.
 * Показывается, пока грузится урок, вместо скелета главной (макет 07 по форме упражнения).
 */
@Composable
fun ExerciseSkeleton() {
    val spacing = TamyrTheme.spacing
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = spacing.screen, end = spacing.screen, top = spacing.l),
            horizontalArrangement = Arrangement.spacedBy(spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(Modifier.size(20.dp), radius = 10.dp)
            SkeletonBlock(Modifier.weight(1f).height(TamyrSizes.progressHeight), radius = 5.dp)
        }
        Column(Modifier.padding(horizontal = spacing.screen).padding(top = spacing.xxl), verticalArrangement = Arrangement.spacedBy(spacing.l)) {
            SkeletonBlock(Modifier.width(96.dp).height(24.dp), radius = 8.dp)
            SkeletonBlock(Modifier.width(200.dp).height(22.dp), radius = 8.dp)
            TamyrCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                    SkeletonBlock(Modifier.fillMaxWidth(0.5f).height(28.dp), radius = 8.dp)
                    SkeletonBlock(Modifier.fillMaxWidth(0.3f).height(12.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                repeat(OPTIONS) { SkeletonBlock(Modifier.fillMaxWidth().height(OptionHeight), radius = 13.dp) }
            }
        }
    }
}

private const val OPTIONS = 4

@Preview(name = "Lesson loading", widthDp = 390, heightDp = 844)
@Composable
private fun LessonLoadingPreview() = PreviewFrame { LoadingState(skeleton = { ExerciseSkeleton() }) }
