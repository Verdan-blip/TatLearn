package ru.tatarcha.tatlearn.feature.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.BrandDiamonds
import ru.tatarcha.tatlearn.core.designsystem.component.ErrorState
import ru.tatarcha.tatlearn.core.designsystem.component.LoadingState
import ru.tatarcha.tatlearn.core.designsystem.component.PulsingDiamonds
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButton
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrButtonStyle
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrProgressBar
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTag
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTagKind
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrTextButton
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import org.jetbrains.compose.resources.StringResource
import tatlearn.shared.generated.resources.Res
import ru.tatarcha.tatlearn.core.common.LoadState
import tatlearn.shared.generated.resources.error_code
import tatlearn.shared.generated.resources.showcase_section_data
import tatlearn.shared.generated.resources.showcase_stats
import tatlearn.shared.generated.resources.showcase_home_loading
import tatlearn.shared.generated.resources.showcase_home_loaded
import tatlearn.shared.generated.resources.showcase_reload_home
import tatlearn.shared.generated.resources.showcase_record_result
import tatlearn.shared.generated.resources.showcase_errors_on
import tatlearn.shared.generated.resources.showcase_errors_off
import tatlearn.shared.generated.resources.showcase_accent
import tatlearn.shared.generated.resources.showcase_card_title
import tatlearn.shared.generated.resources.showcase_close
import tatlearn.shared.generated.resources.showcase_disabled
import tatlearn.shared.generated.resources.showcase_glyphs_lower
import tatlearn.shared.generated.resources.showcase_glyphs_sample
import tatlearn.shared.generated.resources.showcase_glyphs_upper
import tatlearn.shared.generated.resources.showcase_link
import tatlearn.shared.generated.resources.showcase_primary
import tatlearn.shared.generated.resources.showcase_secondary
import tatlearn.shared.generated.resources.showcase_secondary_accent
import tatlearn.shared.generated.resources.showcase_section_buttons
import tatlearn.shared.generated.resources.showcase_section_card
import tatlearn.shared.generated.resources.showcase_section_diamonds
import tatlearn.shared.generated.resources.showcase_section_glyphs
import tatlearn.shared.generated.resources.showcase_section_progress
import tatlearn.shared.generated.resources.showcase_section_states
import tatlearn.shared.generated.resources.showcase_section_tags
import tatlearn.shared.generated.resources.showcase_show_error
import tatlearn.shared.generated.resources.showcase_show_loading
import tatlearn.shared.generated.resources.showcase_tag_build
import tatlearn.shared.generated.resources.showcase_tag_listen
import tatlearn.shared.generated.resources.showcase_tag_write

private enum class Overlay { None, Loading, Error }

/** Только для отладки: каталог всех компонентов дизайн-системы. Открывается кнопкой «UI kit». */
@Composable
fun ShowcaseScreen(viewModel: ShowcaseViewModel, onClose: () -> Unit) {
    val data by viewModel.state.collectAsState()
    var overlay by remember { mutableStateOf(Overlay.None) }
    var progress by remember { mutableStateOf(0.45f) }

    when (overlay) {
        Overlay.Loading -> LoadingState()
        Overlay.Error -> ErrorState(code = "NET_TIMEOUT", onRetry = { overlay = Overlay.None }, onOffline = { overlay = Overlay.None })
        Overlay.None -> Column(
            Modifier
                .fillMaxSize()
                .background(TamyrTheme.colors.background)
                .safeContentPadding()
                .verticalScroll(rememberScrollState())
                .padding(TamyrTheme.spacing.screen),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.l),
        ) {
            TamyrTextButton(stringResource(Res.string.showcase_close), onClose)

            Section(Res.string.showcase_section_data) {
                Text(
                    stringResource(Res.string.showcase_stats, data.stats.xp, data.stats.streak, data.stats.level),
                    style = TamyrTheme.typography.body15,
                    color = TamyrTheme.colors.textPrimary,
                )
                val homeText = when (val home = data.home) {
                    LoadState.Loading -> stringResource(Res.string.showcase_home_loading)
                    is LoadState.Error -> stringResource(Res.string.error_code, home.code.orEmpty())
                    is LoadState.Content -> stringResource(
                        Res.string.showcase_home_loaded,
                        home.data.currentLesson.lesson.number,
                        home.data.currentLesson.lesson.titleTt,
                        home.data.modules.size,
                    )
                }
                Text(homeText, style = TamyrTheme.typography.body15, color = TamyrTheme.colors.textSecondary)
                TamyrButton(stringResource(Res.string.showcase_reload_home), { viewModel.onEvent(ShowcaseUiEvent.LoadHome) }, style = TamyrButtonStyle.Secondary)
                TamyrButton(stringResource(Res.string.showcase_record_result), { viewModel.onEvent(ShowcaseUiEvent.RecordResult) }, style = TamyrButtonStyle.Secondary)
                TamyrButton(
                    stringResource(if (data.simulateErrors) Res.string.showcase_errors_on else Res.string.showcase_errors_off),
                    { viewModel.onEvent(ShowcaseUiEvent.ToggleSimulateErrors) },
                    style = TamyrButtonStyle.SecondaryAccent,
                )
            }
            Section(Res.string.showcase_section_buttons) {
                TamyrButton(stringResource(Res.string.showcase_primary), {})
                TamyrButton(stringResource(Res.string.showcase_accent), {}, style = TamyrButtonStyle.Accent)
                TamyrButton(stringResource(Res.string.showcase_secondary), {}, style = TamyrButtonStyle.Secondary)
                TamyrButton(stringResource(Res.string.showcase_secondary_accent), {}, style = TamyrButtonStyle.SecondaryAccent)
                TamyrButton(stringResource(Res.string.showcase_disabled), {}, enabled = false)
                TamyrTextButton(stringResource(Res.string.showcase_link), {})
            }
            Section(Res.string.showcase_section_tags) {
                Row(horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
                    TamyrTag(stringResource(Res.string.showcase_tag_listen), TamyrTagKind.Success)
                    TamyrTag(stringResource(Res.string.showcase_tag_build), TamyrTagKind.Error)
                }
                TamyrTag(stringResource(Res.string.showcase_tag_write), TamyrTagKind.Gold)
            }
            Section(Res.string.showcase_section_progress) {
                TamyrProgressBar(progress, Modifier.fillMaxWidth())
            }
            Section(Res.string.showcase_section_diamonds) {
                BrandDiamonds()
                PulsingDiamonds()
            }
            Section(Res.string.showcase_section_card) {
                TamyrCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.showcase_card_title), style = TamyrTheme.typography.body17Bold, color = TamyrTheme.colors.textPrimary)
                }
            }
            Section(Res.string.showcase_section_states) {
                TamyrButton(stringResource(Res.string.showcase_show_loading), { overlay = Overlay.Loading }, style = TamyrButtonStyle.Secondary)
                TamyrButton(stringResource(Res.string.showcase_show_error), { overlay = Overlay.Error }, style = TamyrButtonStyle.Secondary)
            }
            // Визуальная проверка, что PT Sans на этой платформе рисует татарские буквы.
            Section(Res.string.showcase_section_glyphs) {
                Text(stringResource(Res.string.showcase_glyphs_lower), style = TamyrTheme.typography.display30, color = TamyrTheme.colors.textPrimary)
                Text(stringResource(Res.string.showcase_glyphs_upper), style = TamyrTheme.typography.display30, color = TamyrTheme.colors.textPrimary)
                Text(stringResource(Res.string.showcase_glyphs_sample), style = TamyrTheme.typography.body16, color = TamyrTheme.colors.textSecondary)
                Text(stringResource(Res.string.showcase_glyphs_sample), style = TamyrTheme.typography.transcription, color = TamyrTheme.colors.textSecondary)
            }
        }
    }
}

@Composable
private fun Section(title: StringResource, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.m)) {
        Text(stringResource(title), style = TamyrTheme.typography.caption13Bold, color = TamyrTheme.colors.textSecondary)
        content()
    }
}
