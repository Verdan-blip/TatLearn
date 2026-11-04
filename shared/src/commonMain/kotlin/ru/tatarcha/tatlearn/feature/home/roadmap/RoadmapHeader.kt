package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.Diamond
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.UserStats
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.home_greeting
import tatlearn.shared.generated.resources.roadmap_stats_description
import tatlearn.shared.generated.resources.roadmap_streak
import tatlearn.shared.generated.resources.roadmap_xp

private val ChipHeight = 40.dp
private val StreakPillHeight = 28.dp
private val StreakDiamond = 9.dp

/** Шапка ленты: приветствие с именем и чип «Серия · XP». Новичку без серии и опыта чипа нет: нулевая серия выглядит упрёком. */
@Composable
fun RoadmapHeader(
    userName: String,
    stats: UserStats,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = TamyrTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
        ) {
            Text(
                text = stringResource(Res.string.home_greeting),
                style = TamyrTheme.typography.body14,
                color = TamyrTheme.colors.textSecondary,
            )
            Text(
                text = userName,
                style = TamyrTheme.typography.headline24,
                color = TamyrTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (stats.streak > 0 || stats.xp > 0) StatsChip(stats = stats)
    }
}

@Composable
private fun StatsChip(stats: UserStats) {
    val description = stringResource(Res.string.roadmap_stats_description, stats.streak, stats.xp)
    Row(
        modifier = Modifier
            .semantics { contentDescription = description }
            .height(ChipHeight)
            .solidShadow(TamyrTheme.colors.border, TamyrSizes.buttonShadow, TamyrTheme.shapes.circle)
            .clip(TamyrTheme.shapes.circle)
            .background(TamyrTheme.colors.surface)
            .border(TamyrSizes.borderThin, TamyrTheme.colors.border, TamyrTheme.shapes.circle)
            .padding(start = TamyrTheme.spacing.xs, end = TamyrTheme.spacing.blockGap)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .height(StreakPillHeight)
                .clip(TamyrTheme.shapes.circle)
                .background(TamyrTheme.colors.goldContainer)
                .padding(horizontal = TamyrTheme.spacing.s),
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Diamond(color = TamyrTheme.colors.gold, size = StreakDiamond)
            Text(
                text = stringResource(Res.string.roadmap_streak, stats.streak),
                style = TamyrTheme.typography.caption13Bold,
                color = TamyrTheme.colors.goldText,
                softWrap = false,
            )
        }
        Text(
            text = stringResource(Res.string.roadmap_xp, stats.xp),
            style = TamyrTheme.typography.body14Bold,
            color = TamyrTheme.colors.primary,
            softWrap = false,
        )
    }
}

@Preview
@Composable
private fun RoadmapHeaderPreview() = PreviewFrame {
    RoadmapHeader(
        userName = "Алия",
        stats = UserStats(xp = 340, streak = 12),
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
    )
}

@Preview(name = "New user, no chip")
@Composable
private fun RoadmapHeaderNewUserPreview() = PreviewFrame {
    RoadmapHeader(
        userName = "Алия",
        stats = UserStats.Empty,
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
    )
}

@Preview(name = "Long name")
@Composable
private fun RoadmapHeaderLongNamePreview() = PreviewFrame {
    RoadmapHeader(
        userName = "Александра-Мөхәммәдия",
        stats = UserStats(xp = 1990, streak = 100),
        modifier = Modifier.padding(TamyrTheme.spacing.screen),
    )
}
