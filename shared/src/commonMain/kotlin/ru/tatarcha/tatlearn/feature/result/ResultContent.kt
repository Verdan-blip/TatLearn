package ru.tatarcha.tatlearn.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.StreakBadge
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButtonStyle
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTextButton
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.exercise.XpRules
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.result_accuracy
import tatlearn.shared.generated.resources.result_accuracy_value
import tatlearn.shared.generated.resources.result_continue
import tatlearn.shared.generated.resources.result_failed_reward
import tatlearn.shared.generated.resources.result_failed_reward_streak
import tatlearn.shared.generated.resources.result_failed_subtitle
import tatlearn.shared.generated.resources.result_failed_title
import tatlearn.shared.generated.resources.result_fallback_name
import tatlearn.shared.generated.resources.result_home
import tatlearn.shared.generated.resources.result_missing_answers
import tatlearn.shared.generated.resources.result_missing_mistakes
import tatlearn.shared.generated.resources.result_retry_mistakes
import tatlearn.shared.generated.resources.result_streak_body
import tatlearn.shared.generated.resources.result_streak_title
import tatlearn.shared.generated.resources.result_subtitle
import tatlearn.shared.generated.resources.result_threshold
import tatlearn.shared.generated.resources.result_time
import tatlearn.shared.generated.resources.result_time_value
import tatlearn.shared.generated.resources.result_title
import tatlearn.shared.generated.resources.result_xp
import tatlearn.shared.generated.resources.result_xp_value

private val StatTilePadding = 8.dp
private val StreakBadgeSize = 48.dp
private const val PERCENT = 100f

@Composable
fun ResultContent(
    state: ResultUiState,
    onEvent: (ResultUiEvent) -> Unit,
) {
    val name = state.userName.ifBlank { stringResource(Res.string.result_fallback_name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TamyrTheme.colors.background)
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = TamyrTheme.spacing.screen),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xl, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.passed) {
                PassedSummary(state = state, name = name)
            } else {
                FailedSummary(state = state, name = name)
            }
        }
        Column(
            modifier = Modifier.padding(
                start = TamyrTheme.spacing.screen,
                end = TamyrTheme.spacing.screen,
                bottom = TamyrTheme.spacing.l,
            ),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.passed) {
                TamyrButton(
                    text = stringResource(Res.string.result_continue),
                    onClick = { onEvent(ResultUiEvent.ContinueClicked) },
                )
                if (state.mistakes > 0) {
                    TamyrButton(
                        text = stringResource(Res.string.result_retry_mistakes, state.mistakes),
                        onClick = { onEvent(ResultUiEvent.RetryMistakesClicked) },
                        style = TamyrButtonStyle.SecondaryAccent,
                    )
                }
            } else {
                TamyrButton(
                    text = stringResource(Res.string.result_retry_mistakes, state.mistakes),
                    onClick = { onEvent(ResultUiEvent.RetryMistakesClicked) },
                )
                TamyrTextButton(
                    text = stringResource(Res.string.result_home),
                    onClick = { onEvent(ResultUiEvent.ContinueClicked) },
                    color = TamyrTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun PassedSummary(
    state: ResultUiState,
    name: String,
) {
    TamyrMascot(
        pose = MascotPose.Cheer,
        size = TamyrSizes.resultMascot,
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.result_title),
            style = TamyrTheme.typography.display30,
            color = TamyrTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.result_subtitle, name),
            style = TamyrTheme.typography.body16,
            color = TamyrTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m),
    ) {
        StatTile(
            label = stringResource(Res.string.result_xp),
            value = stringResource(Res.string.result_xp_value, state.xpEarned),
            tint = TamyrTheme.colors.goldText,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            label = stringResource(Res.string.result_accuracy),
            value = stringResource(Res.string.result_accuracy_value, state.accuracyPercent),
            tint = TamyrTheme.colors.primary,
            modifier = Modifier.weight(1f),
        )
        StatTile(
            label = stringResource(Res.string.result_time),
            value = stringResource(Res.string.result_time_value, formatDuration(state.durationSeconds)),
            tint = TamyrTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
    if (state.streak > 0 && state.streakExtended) {
        TamyrCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = TamyrTheme.spacing.section,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.l),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StreakBadge(
                    days = state.streak,
                    size = StreakBadgeSize,
                    textStyle = TamyrTheme.typography.title19,
                )
                Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
                    Text(
                        text = stringResource(Res.string.result_streak_title, state.streak),
                        style = TamyrTheme.typography.body16Bold,
                        color = TamyrTheme.colors.textPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.result_streak_body),
                        style = TamyrTheme.typography.caption13,
                        color = TamyrTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun FailedSummary(
    state: ResultUiState,
    name: String,
) {
    TamyrMascot(
        pose = MascotPose.Tired,
        size = TamyrSizes.resultMascotCompact,
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.result_failed_title, name),
            style = TamyrTheme.typography.headline26,
            color = TamyrTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.result_failed_subtitle),
            style = TamyrTheme.typography.body16,
            color = TamyrTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
    ThresholdCard(state = state)
    Text(
        text = if (state.streak > 0) {
            stringResource(Res.string.result_failed_reward_streak, state.xpEarned, state.streak)
        } else {
            stringResource(Res.string.result_failed_reward, state.xpEarned)
        },
        style = TamyrTheme.typography.body14,
        color = TamyrTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ThresholdCard(state: ResultUiState) {
    val threshold = XpRules.PASS_THRESHOLD_PERCENT / PERCENT
    TamyrCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = TamyrTheme.spacing.section,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = stringResource(Res.string.result_accuracy),
                    style = TamyrTheme.typography.body14Bold,
                    color = TamyrTheme.colors.textSecondary,
                )
                Text(
                    text = stringResource(Res.string.result_accuracy_value, state.accuracyPercent),
                    style = TamyrTheme.typography.headline24,
                    color = TamyrTheme.colors.textPrimary,
                )
            }
            Text(
                text = stringResource(Res.string.result_accuracy_value, XpRules.PASS_THRESHOLD_PERCENT),
                style = TamyrTheme.typography.label12Bold,
                color = TamyrTheme.colors.textPrimary,
                modifier = Modifier.atFraction(threshold),
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TamyrSizes.thresholdBar)
                        .clip(TamyrTheme.shapes.progress)
                        .background(TamyrTheme.colors.borderStrong),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(state.accuracyPercent / PERCENT)
                            .height(TamyrSizes.thresholdBar)
                            .background(TamyrTheme.colors.primary),
                    )
                }
                Box(
                    modifier = Modifier
                        .atFraction(threshold)
                        .offset(y = -(TamyrSizes.thresholdMarkerHeight - TamyrSizes.thresholdBar) / 2)
                        .size(width = TamyrSizes.thresholdMarkerWidth, height = TamyrSizes.thresholdMarkerHeight)
                        .clip(TamyrTheme.shapes.progress)
                        .background(TamyrTheme.colors.textPrimary),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (state.isMistakesRun) {
                        stringResource(Res.string.result_missing_mistakes, state.missingAnswers)
                    } else {
                        stringResource(Res.string.result_missing_answers, state.missingAnswers)
                    },
                    style = TamyrTheme.typography.caption13,
                    color = TamyrTheme.colors.textSecondary,
                )
                Text(
                    text = stringResource(Res.string.result_threshold, XpRules.PASS_THRESHOLD_PERCENT),
                    style = TamyrTheme.typography.caption13Bold,
                    color = TamyrTheme.colors.textPrimary,
                )
            }
        }
    }
}

/** Ставит элемент так, чтобы его центр лежал на доле [fraction] ширины родителя. */
private fun Modifier.atFraction(fraction: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(constraints.maxWidth, placeable.height) {
        placeable.place((constraints.maxWidth * fraction - placeable.width / 2f).roundToInt(), 0)
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    TamyrCard(
        modifier = modifier,
        shape = TamyrTheme.shapes.tile,
        contentPadding = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StatTilePadding, vertical = TamyrTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label.toUpperCase(Locale.current),
                style = TamyrTheme.typography.label12Bold,
                color = tint,
            )
            Text(
                text = value,
                style = TamyrTheme.typography.headline24,
                color = tint,
            )
        }
    }
}

private const val SECONDS_PER_MINUTE = 60

/** м:сс; строки Compose Resources не поддерживают спецификаторы формата с нулями слева. */
internal fun formatDuration(totalSeconds: Int): String =
    "${totalSeconds / SECONDS_PER_MINUTE}:${(totalSeconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')}"

private const val PREVIEW_GROUP = "Result"

@Composable
private fun ResultPreview(state: ResultUiState) = PreviewFrame {
    ResultContent(
        state = state,
        onEvent = {},
    )
}

@Preview(name = "With mistakes and streak", group = PREVIEW_GROUP)
@Composable
private fun ResultMistakesPreview() = ResultPreview(
    ResultUiState("Алия", xpEarned = 24, accuracyPercent = 90, durationSeconds = 272, mistakes = 1, mistakeStepIds = listOf("quiz-1"), streak = 13, streakExtended = true),
)

@Preview(name = "Perfect lesson", group = PREVIEW_GROUP)
@Composable
private fun ResultPerfectPreview() = ResultPreview(
    ResultUiState("Алия", xpEarned = 36, accuracyPercent = 100, durationSeconds = 305, mistakes = 0, streak = 4, streakExtended = true),
)

@Preview(name = "Not passed", group = PREVIEW_GROUP)
@Composable
private fun ResultNotPassedPreview() = ResultPreview(
    ResultUiState(
        userName = "Алия",
        xpEarned = 21,
        accuracyPercent = 70,
        durationSeconds = 272,
        mistakes = 3,
        mistakeStepIds = listOf("quiz-0", "quiz-1", "listening-0"),
        passed = false,
        missingAnswers = 1,
        streak = 13,
    ),
)

@Preview(name = "Not passed, no name, no streak, far from threshold", group = PREVIEW_GROUP)
@Composable
private fun ResultNotPassedFarPreview() = ResultPreview(
    ResultUiState(
        userName = "",
        xpEarned = 6,
        accuracyPercent = 30,
        durationSeconds = 130,
        mistakes = 7,
        mistakeStepIds = List(7) { "quiz-$it" },
        passed = false,
        missingAnswers = 5,
    ),
)

@Preview(name = "Mistakes run, not passed yet", group = PREVIEW_GROUP)
@Composable
private fun ResultMistakesRunPreview() = ResultPreview(
    ResultUiState(
        userName = "Алия",
        xpEarned = 3,
        accuracyPercent = 67,
        durationSeconds = 70,
        mistakes = 1,
        mistakeStepIds = listOf("quiz-1"),
        passed = false,
        missingAnswers = 1,
        isMistakesRun = true,
        streak = 2,
    ),
)

@Preview(name = "No streak, no name", group = PREVIEW_GROUP)
@Composable
private fun ResultNoStreakPreview() = ResultPreview(
    ResultUiState("", xpEarned = 12, accuracyPercent = 85, durationSeconds = 65, mistakes = 1, mistakeStepIds = listOf("quiz-0")),
)

@Preview(name = "Long duration and big numbers", group = PREVIEW_GROUP)
@Composable
private fun ResultLongPreview() = ResultPreview(
    ResultUiState("Александра-Мөхәммәдия", xpEarned = 1240, accuracyPercent = 100, durationSeconds = 3725, mistakes = 0, streak = 365, streakExtended = true),
)
