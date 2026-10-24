package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.tatarcha.tatlearn.core.common.LoadState
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.error_code
import tatlearn.shared.generated.resources.error_message
import tatlearn.shared.generated.resources.error_offline
import tatlearn.shared.generated.resources.error_retry
import tatlearn.shared.generated.resources.error_title
import tatlearn.shared.generated.resources.loading_message

/** Рисует состояние, подходящее для [LoadState]; экраны 07 и 08 дизайна. */
@Composable
fun <T> LoadStateContent(
    state: LoadState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onOffline: (() -> Unit)? = null,
    loading: @Composable () -> Unit = { LoadingState() },
    content: @Composable (T) -> Unit,
) {
    Box(modifier) {
        when (state) {
            LoadState.Loading -> loading()
            is LoadState.Content -> content(state.data)
            is LoadState.Error -> ErrorState(code = state.code, onRetry = onRetry, onOffline = onOffline)
        }
    }
}

/** Экран 07: скелет по форме контента сверху, пульсирующие ромбы и подпись снизу. */
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = stringResource(Res.string.loading_message),
    skeleton: @Composable () -> Unit = { HomeSkeleton() },
) {
    Column(modifier.fillMaxSize().background(TamyrTheme.colors.background)) {
        skeleton()
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PulsingDiamonds()
            Text(message, color = TamyrTheme.colors.textSecondary, style = TamyrTheme.typography.body14)
        }
    }
}

private val ErrorMascotSize = 110.dp

/**
 * Экран 08. Грустный маскот стоит над заголовком; его можно заменить через [illustration].
 */
@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    code: String? = null,
    onOffline: (() -> Unit)? = null,
    illustration: (@Composable () -> Unit)? = { TamyrMascot(MascotPose.Sad, ErrorMascotSize) },
) {
    val spacing = TamyrTheme.spacing
    Column(modifier.fillMaxSize().background(TamyrTheme.colors.background)) {
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            illustration?.invoke()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.error_title), style = TamyrTheme.typography.headline26, color = TamyrTheme.colors.textPrimary, textAlign = TextAlign.Center)
                Text(stringResource(Res.string.error_message), style = TamyrTheme.typography.body16, color = TamyrTheme.colors.textSecondary, textAlign = TextAlign.Center)
            }
            if (code != null) {
                TamyrCard(shape = TamyrTheme.shapes.answer, contentPadding = 0.dp) {
                    Text(
                        stringResource(Res.string.error_code, code),
                        style = TamyrTheme.typography.mono12,
                        color = TamyrTheme.colors.textSecondary,
                        modifier = Modifier.padding(horizontal = spacing.l, vertical = spacing.m),
                    )
                }
            }
        }
        Column(
            Modifier.padding(start = spacing.screen, end = spacing.screen, bottom = spacing.bottomInset),
            verticalArrangement = Arrangement.spacedBy(spacing.m),
        ) {
            TamyrButton(stringResource(Res.string.error_retry), onRetry)
            if (onOffline != null) {
                TamyrButton(stringResource(Res.string.error_offline), onOffline, style = TamyrButtonStyle.SecondaryAccent)
            }
        }
    }
}

@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, radius: Dp = 6.dp) {
    Box(modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(radius)).background(TamyrTheme.colors.surfaceMuted))
}

/** Заглушка в форме главного экрана, используется в [LoadingState] по умолчанию. */
@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    val spacing = TamyrTheme.spacing
    Column(modifier.padding(horizontal = spacing.screen, vertical = spacing.l), verticalArrangement = Arrangement.spacedBy(spacing.l)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                SkeletonBlock(Modifier.width(90.dp).height(12.dp))
                SkeletonBlock(Modifier.width(120.dp).height(20.dp), radius = 8.dp)
            }
            SkeletonBlock(Modifier.size(46.dp), radius = 23.dp)
        }
        TamyrCard {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.m)) {
                SkeletonBlock(Modifier.fillMaxWidth(0.4f).height(12.dp))
                SkeletonBlock(Modifier.fillMaxWidth(0.65f).height(18.dp), radius = 8.dp)
                SkeletonBlock(Modifier.fillMaxWidth().height(8.dp), radius = 4.dp)
                SkeletonBlock(Modifier.fillMaxWidth().height(46.dp), radius = 12.dp)
            }
        }
        TamyrCard(contentPadding = spacing.m) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.blockGap), verticalAlignment = Alignment.CenterVertically) {
                SkeletonBlock(Modifier.size(TamyrSizes.streakBadgeCompact), radius = 12.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacing.s)) {
                    SkeletonBlock(Modifier.fillMaxWidth(0.6f).height(12.dp))
                    SkeletonBlock(Modifier.fillMaxWidth().height(6.dp), radius = 3.dp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.m)) {
            repeat(2) {
                TamyrCard(Modifier.weight(1f).height(110.dp), shape = TamyrTheme.shapes.tile) {
                    SkeletonBlock(Modifier.size(34.dp), radius = 10.dp)
                    Box(Modifier.height(spacing.s))
                    SkeletonBlock(Modifier.fillMaxWidth(0.7f).height(12.dp))
                }
            }
        }
    }
}

// region Превью

@Preview(name = "Loading", group = "States", widthDp = 390, heightDp = 844)
@Composable
private fun LoadingPreview() = PreviewFrame { LoadingState() }

@Preview(name = "Error · timeout", group = "States", widthDp = 390, heightDp = 844)
@Composable
private fun ErrorTimeoutPreview() = PreviewFrame { ErrorState(onRetry = {}, code = "NET_TIMEOUT") }

@Preview(name = "Error · with offline button", group = "States", widthDp = 390, heightDp = 844)
@Composable
private fun ErrorOfflinePreview() = PreviewFrame { ErrorState(onRetry = {}, code = "NET_ERROR", onOffline = {}) }

@Preview(name = "Error · no code", group = "States", widthDp = 390, heightDp = 844)
@Composable
private fun ErrorNoCodePreview() = PreviewFrame { ErrorState(onRetry = {}) }

@Preview(name = "Error · small phone", group = "States", widthDp = 320, heightDp = 568)
@Composable
private fun ErrorSmallPreview() = PreviewFrame { ErrorState(onRetry = {}, code = "NOT_FOUND", onOffline = {}) }

// endregion

