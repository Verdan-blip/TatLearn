package ru.tatarcha.tatlearn.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.StreakBadge
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButtonStyle
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.navigation.ResultKey
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.result_accuracy
import tatlearn.shared.generated.resources.result_accuracy_value
import tatlearn.shared.generated.resources.result_continue
import tatlearn.shared.generated.resources.result_fallback_name
import tatlearn.shared.generated.resources.result_retry_mistakes
import tatlearn.shared.generated.resources.result_streak_body
import tatlearn.shared.generated.resources.result_streak_title
import tatlearn.shared.generated.resources.result_subtitle
import tatlearn.shared.generated.resources.result_time
import tatlearn.shared.generated.resources.result_time_value
import tatlearn.shared.generated.resources.result_title
import tatlearn.shared.generated.resources.result_xp
import tatlearn.shared.generated.resources.result_xp_value

private val StatTilePadding = 8.dp
private val StreakBadgeSize = 48.dp
private val MascotSize = 140.dp

@Composable
fun ResultRoute(
    key: ResultKey,
    onContinue: () -> Unit,
    onRetryMistakes: (lessonId: String, stepIds: List<String>) -> Unit,
    viewModel: ResultViewModel = koinViewModel(parameters = { parametersOf(key) }),
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ResultUiEffect.NavigateHome -> onContinue()
                is ResultUiEffect.NavigateToMistakes -> onRetryMistakes(effect.lessonId, effect.stepIds)
            }
        }
    }
    val state by viewModel.state.collectAsState()
    ResultScreen(state, viewModel::onEvent)
}

@Composable
fun ResultScreen(state: ResultUiState, onEvent: (ResultUiEvent) -> Unit) {
    val colors = TamyrTheme.colors
    val spacing = TamyrTheme.spacing
    val name = state.userName.ifBlank { stringResource(Res.string.result_fallback_name) }

    Column(Modifier.fillMaxSize().background(colors.background).safeDrawingPadding()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = spacing.screen),
            verticalArrangement = Arrangement.spacedBy(spacing.xl, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TamyrMascot(MascotPose.Cheer, MascotSize)
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.result_title), style = TamyrTheme.typography.display30, color = colors.textPrimary, textAlign = TextAlign.Center)
                Text(stringResource(Res.string.result_subtitle, name), style = TamyrTheme.typography.body16, color = colors.textSecondary, textAlign = TextAlign.Center)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
                StatTile(stringResource(Res.string.result_xp), stringResource(Res.string.result_xp_value, state.xpEarned), colors.goldText, Modifier.weight(1f))
                StatTile(stringResource(Res.string.result_accuracy), stringResource(Res.string.result_accuracy_value, state.accuracyPercent), colors.primary, Modifier.weight(1f))
                StatTile(
                    stringResource(Res.string.result_time),
                    stringResource(Res.string.result_time_value, formatDuration(state.durationSeconds)),
                    colors.textPrimary,
                    Modifier.weight(1f),
                )
            }
            if (state.streak > 0) {
                TamyrCard(Modifier.fillMaxWidth(), contentPadding = 18.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.l), verticalAlignment = Alignment.CenterVertically) {
                        StreakBadge(state.streak, size = StreakBadgeSize, textStyle = TamyrTheme.typography.title19)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(stringResource(Res.string.result_streak_title, state.streak), style = TamyrTheme.typography.body16Bold, color = colors.textPrimary)
                            Text(stringResource(Res.string.result_streak_body), style = TamyrTheme.typography.caption13, color = colors.textSecondary)
                        }
                    }
                }
            }
        }
        Column(
            Modifier.padding(start = spacing.screen, end = spacing.screen, bottom = spacing.l),
            verticalArrangement = Arrangement.spacedBy(spacing.m),
        ) {
            TamyrButton(stringResource(Res.string.result_continue), { onEvent(ResultUiEvent.ContinueClicked) })
            if (state.mistakes > 0) {
                TamyrButton(
                    stringResource(Res.string.result_retry_mistakes, state.mistakes),
                    { onEvent(ResultUiEvent.RetryMistakesClicked) },
                    style = TamyrButtonStyle.SecondaryAccent,
                )
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    TamyrCard(modifier, shape = TamyrTheme.shapes.tile, contentPadding = 0.dp) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = StatTilePadding, vertical = TamyrTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label.toUpperCase(Locale.current), style = TamyrTheme.typography.label12Bold, color = tint)
            Text(value, style = TamyrTheme.typography.headline24, color = tint)
        }
    }
}

private const val SECONDS_PER_MINUTE = 60

/** м:сс; строки Compose Resources не поддерживают спецификаторы формата с нулями слева. */
internal fun formatDuration(totalSeconds: Int): String =
    "${totalSeconds / SECONDS_PER_MINUTE}:${(totalSeconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')}"

// region Превью

private const val Group = "Result"

@Composable
private fun ResultPreview(state: ResultUiState) = PreviewFrame { ResultScreen(state) {} }

@Preview(name = "With mistakes and streak", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ResultMistakesPreview() = ResultPreview(
    ResultUiState("Алия", xpEarned = 24, accuracyPercent = 90, durationSeconds = 272, mistakes = 1, mistakeStepIds = listOf("quiz-1"), streak = 13),
)

@Preview(name = "Perfect lesson", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ResultPerfectPreview() = ResultPreview(
    ResultUiState("Алия", xpEarned = 36, accuracyPercent = 100, durationSeconds = 305, mistakes = 0, streak = 4),
)

@Preview(name = "No streak, no name", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ResultNoStreakPreview() = ResultPreview(
    ResultUiState("", xpEarned = 12, accuracyPercent = 60, durationSeconds = 65, mistakes = 3, mistakeStepIds = listOf("quiz-0", "quiz-2", "sentence-0")),
)

@Preview(name = "Below pass threshold", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ResultBelowThresholdPreview() = ResultPreview(
    // TODO(design): в заголовке по-прежнему сказано, что урок пройден; отдельного варианта «порог 80% не достигнут» нет.
    ResultUiState("Алия", xpEarned = 6, accuracyPercent = 40, durationSeconds = 130, mistakes = 3, mistakeStepIds = listOf("quiz-0", "quiz-1", "listening-0"), streak = 2),
)

@Preview(name = "Long duration and big numbers", group = Group, widthDp = 390, heightDp = 844)
@Composable
private fun ResultLongPreview() = ResultPreview(
    ResultUiState("Александра-Мөхәммәдия", xpEarned = 1240, accuracyPercent = 100, durationSeconds = 3725, mistakes = 0, streak = 365),
)

// endregion

