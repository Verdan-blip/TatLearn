package ru.tatarcha.tatlearn.feature.module

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.SkeletonBlock
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

private const val SKELETON_ROWS = 4

/** Экран 21: скелет по форме нового экрана (баннер с «кочкой», строки уроков с узлами). */
@Composable
fun ModuleSkeleton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TamyrTheme.colors.goldContainer)
                .statusBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = TamyrTheme.spacing.xl, bottom = TamyrTheme.spacing.section),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
            ) {
                SkeletonBlock(Modifier.size(width = 150.dp, height = 110.dp), radius = 24.dp)
                SkeletonBlock(Modifier.size(width = 150.dp, height = 18.dp), radius = 9.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                    repeat(SKELETON_ROWS + 1) { SkeletonBlock(Modifier.size(TamyrSizes.echpochmakCard), radius = 6.dp) }
                }
            }
            BackButton(
                onBack = onBack,
                modifier = Modifier.padding(start = TamyrTheme.spacing.m, top = TamyrTheme.spacing.xxs),
            )
        }
        Column(
            modifier = Modifier.padding(start = TamyrTheme.spacing.xl, end = TamyrTheme.spacing.screen, top = TamyrTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(LessonGap),
        ) {
            repeat(SKELETON_ROWS) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.tight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(64.dp), contentAlignment = Alignment.Center) {
                        SkeletonBlock(Modifier.size(LessonNodeSize), radius = 14.dp)
                    }
                    TamyrCard(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = TamyrSizes.lessonRowMinHeight),
                        contentPadding = TamyrTheme.spacing.m,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
                            SkeletonBlock(Modifier.width(140.dp).height(12.dp))
                            SkeletonBlock(Modifier.width(90.dp).height(10.dp), radius = 5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ModuleSkeletonPreview() = PreviewFrame {
    ModuleSkeleton(onBack = {})
}
