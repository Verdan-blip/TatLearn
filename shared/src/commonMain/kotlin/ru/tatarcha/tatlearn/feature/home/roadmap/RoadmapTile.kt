package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.dashedBorder
import ru.tatarcha.tatlearn.core.designsystem.component.icon.CheckIcon
import ru.tatarcha.tatlearn.core.designsystem.component.solidShadow
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrMotion
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.LearningModule
import ru.tatarcha.tatlearn.domain.model.ModuleState
import ru.tatarcha.tatlearn.domain.model.RoadmapEntry
import ru.tatarcha.tatlearn.domain.model.moduleTitle
import ru.tatarcha.tatlearn.feature.home.LockIcon
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.roadmap_fraction
import tatlearn.shared.generated.resources.roadmap_module_current_description
import tatlearn.shared.generated.resources.roadmap_module_done
import tatlearn.shared.generated.resources.roadmap_module_done_description
import tatlearn.shared.generated.resources.roadmap_module_locked_description
import tatlearn.shared.generated.resources.roadmap_module_open_description
import tatlearn.shared.generated.resources.roadmap_unlock_hint

private val BaseShadow = 5.dp
private val SealShift = 88.dp
private val NumberShift = 2.dp
private val IllustrationShift = 2.dp
private val SealDiamond = 20.dp
private val SealCheck = 15.dp
private const val LOCK_TILT_FIRST = 14f
private const val LOCK_TILT_SECOND = 10f
private const val LOCK_TILT_THIRD = 6f
private const val SEAL_START_SCALE = 1.5f

/** Плитка-«кочка» модуля: иллюстрация на основании, номер, печать «Тәмам» или замок, подпись. Нажатие открывает модуль. */
@Composable
fun RoadmapTile(
    entry: RoadmapEntry,
    totalModules: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sealProgress: () -> Float = { 1f },
    reduceMotion: Boolean = false,
) {
    val module = entry.module
    val locked = entry.state == ModuleState.Locked
    val shake = remember { Animatable(0f) }
    var shakes by remember { mutableIntStateOf(0) }
    LaunchedEffect(shakes) {
        if (shakes > 0 && !reduceMotion) {
            shake.animateTo(-LOCK_TILT_FIRST, tween(TamyrMotion.SHAKE_MS / 4, easing = FastOutSlowInEasing))
            shake.animateTo(LOCK_TILT_SECOND, tween(TamyrMotion.SHAKE_MS / 4, easing = FastOutSlowInEasing))
            shake.animateTo(-LOCK_TILT_THIRD, tween(TamyrMotion.SHAKE_MS / 4, easing = FastOutSlowInEasing))
            shake.animateTo(0f, tween(TamyrMotion.SHAKE_MS / 4, easing = FastOutSlowInEasing))
        }
    }
    val title = moduleTitle(module.titleTt, module.titleRu)
    val description = when (entry.state) {
        ModuleState.Done -> stringResource(Res.string.roadmap_module_done_description, title, entry.number, totalModules, module.completedLessons, module.totalLessons)
        ModuleState.Current -> stringResource(Res.string.roadmap_module_current_description, title, entry.number, totalModules, module.completedLessons, module.totalLessons)
        ModuleState.Open -> stringResource(Res.string.roadmap_module_open_description, title, entry.number, totalModules, module.completedLessons, module.totalLessons)
        ModuleState.Locked -> stringResource(Res.string.roadmap_module_locked_description, title, entry.number, totalModules)
    }
    Column(
        modifier = modifier
            .width(TamyrSizes.moduleLabelWidth)
            .heightIn(min = TamyrSizes.topBarAction)
            .clickable(role = Role.Button) {
                if (locked) shakes++
                onClick()
            }
            .semantics(mergeDescendants = true) { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
    ) {
        TileBlock(entry = entry, locked = locked, sealProgress = sealProgress, lockTilt = { shake.value })
        TileLabel(entry = entry, title = title)
    }
}

@Composable
private fun TileBlock(
    entry: RoadmapEntry,
    locked: Boolean,
    sealProgress: () -> Float,
    lockTilt: () -> Float,
) {
    val lift = TamyrSizes.moduleIllustrationLift
    val baseModifier = Modifier
        .size(width = TamyrSizes.moduleBaseWidth, height = TamyrSizes.moduleBaseHeight)
        .solidShadow(
            color = if (locked) TamyrTheme.colors.borderStrong else TamyrTheme.colors.successBorder,
            offsetY = BaseShadow,
            shape = TamyrTheme.shapes.moduleBase,
        )
        .clip(TamyrTheme.shapes.moduleBase)
        .background(if (locked) TamyrTheme.colors.surfaceMuted else TamyrTheme.colors.successContainer)
    Box(modifier = Modifier.size(width = TamyrSizes.moduleBlockWidth, height = lift + TamyrSizes.moduleBaseHeight + BaseShadow)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = lift)
                .then(baseModifier)
                .then(
                    if (locked) {
                        Modifier.dashedBorder(color = TamyrTheme.colors.contour, shape = TamyrTheme.shapes.moduleBase)
                    } else {
                        Modifier.border(TamyrSizes.borderThin, TamyrTheme.colors.successBorder, TamyrTheme.shapes.moduleBase)
                    },
                ),
        )
        ModuleIllustration(
            icon = entry.module.icon,
            locked = locked,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = IllustrationShift)
                .size(width = TamyrSizes.moduleIllustrationWidth, height = TamyrSizes.moduleIllustrationHeight),
        )
        NumberBadge(
            number = entry.number,
            locked = locked,
            modifier = Modifier.offset(x = NumberShift, y = lift + 30.dp),
        )
        when (entry.state) {
            ModuleState.Done -> DoneSeal(progress = sealProgress, modifier = Modifier.offset(x = SealShift, y = lift + 26.dp))
            ModuleState.Locked -> LockBadge(tilt = lockTilt, modifier = Modifier.offset(x = SealShift, y = lift + 26.dp))
            ModuleState.Current, ModuleState.Open -> Unit
        }
    }
}

@Composable
private fun NumberBadge(
    number: Int,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(TamyrSizes.moduleNumberBadge)
            .clip(TamyrTheme.shapes.circle)
            .background(if (locked) TamyrTheme.colors.surface else TamyrTheme.colors.primary)
            .border(
                width = if (locked) 1.5.dp else TamyrSizes.borderAccent,
                color = if (locked) TamyrTheme.colors.contour else TamyrTheme.colors.surface,
                shape = TamyrTheme.shapes.circle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = TamyrTheme.typography.caption13Bold,
            color = if (locked) TamyrTheme.colors.textSecondary else TamyrTheme.colors.onPrimary,
        )
    }
}

@Composable
private fun DoneSeal(
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(TamyrSizes.moduleSeal)
            .graphicsLayer {
                // Печать опускается: из увеличенной и прозрачной в обычную (анимация E2).
                val value = progress()
                scaleX = SEAL_START_SCALE - (SEAL_START_SCALE - 1f) * value
                scaleY = scaleX
                alpha = value
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(SealDiamond)
                .graphicsLayer { rotationZ = 45f }
                .clip(TamyrTheme.shapes.tag)
                .background(TamyrTheme.colors.gold)
                .border(TamyrSizes.borderAccent, TamyrTheme.colors.surface, TamyrTheme.shapes.tag),
        )
        CheckIcon(
            size = SealCheck,
            tint = TamyrTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun LockBadge(
    tilt: () -> Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(TamyrSizes.moduleSeal)
            .graphicsLayer {
                rotationZ = tilt()
                transformOrigin = TransformOrigin(0.5f, 0.2f)
            }
            .clip(TamyrTheme.shapes.circle)
            .background(TamyrTheme.colors.surface)
            .border(1.5.dp, TamyrTheme.colors.contour, TamyrTheme.shapes.circle),
        contentAlignment = Alignment.Center,
    ) {
        LockIcon(tint = TamyrTheme.colors.textSecondary)
    }
}

@Composable
private fun TileLabel(
    entry: RoadmapEntry,
    title: String,
) {
    val module = entry.module
    val locked = entry.state == ModuleState.Locked
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs / 2),
    ) {
        Text(
            text = title,
            style = TamyrTheme.typography.body15Bold,
            color = if (locked) TamyrTheme.colors.textSecondary else TamyrTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (locked && entry.showsUnlockHint && module.lockedByTitleTt != null) {
            Text(
                text = stringResource(Res.string.roadmap_unlock_hint, moduleTitle(module.lockedByTitleTt, module.lockedByTitleRu ?: module.lockedByTitleTt)),
                style = TamyrTheme.typography.caption13,
                color = TamyrTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        val progress = when (entry.state) {
            ModuleState.Done -> stringResource(Res.string.roadmap_module_done, module.completedLessons, module.totalLessons)
            ModuleState.Current, ModuleState.Open, ModuleState.Locked -> stringResource(Res.string.roadmap_fraction, module.completedLessons, module.totalLessons)
        }
        Text(
            text = progress,
            style = TamyrTheme.typography.caption13Bold,
            color = if (entry.state == ModuleState.Done) TamyrTheme.colors.primary else TamyrTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

private fun previewEntry(state: ModuleState, number: Int, hint: Boolean = false) = RoadmapEntry(
    module = LearningModule(
        id = "m$number",
        titleTt = "Гаилә",
        titleRu = "Семья",
        icon = "family",
        progressPercent = 0,
        lockedBy = if (state == ModuleState.Locked) "prev" else null,
        lockedByTitleTt = "Алфавит",
        lockedByTitleRu = "Алфавит",
        completedLessons = if (state == ModuleState.Done) 5 else 0,
        totalLessons = 5,
    ),
    number = number,
    state = state,
    showsUnlockHint = hint,
)

@Preview(name = "Done")
@Composable
private fun RoadmapTileDonePreview() = PreviewFrame {
    RoadmapTile(entry = previewEntry(ModuleState.Done, 2), totalModules = 9, onClick = {})
}

@Preview(name = "Locked with hint")
@Composable
private fun RoadmapTileLockedPreview() = PreviewFrame {
    RoadmapTile(entry = previewEntry(ModuleState.Locked, 4, hint = true), totalModules = 9, onClick = {})
}

@Preview(name = "Locked")
@Composable
private fun RoadmapTileLockedPlainPreview() = PreviewFrame {
    RoadmapTile(entry = previewEntry(ModuleState.Locked, 5), totalModules = 9, onClick = {})
}
