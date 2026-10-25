package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.icon.CheckIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.AvatarColors
import ru.tatarcha.tatlearn.domain.model.AvatarPose
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.avatar_color_0
import tatlearn.shared.generated.resources.avatar_color_1
import tatlearn.shared.generated.resources.avatar_color_2
import tatlearn.shared.generated.resources.avatar_color_3
import tatlearn.shared.generated.resources.avatar_colors_label
import tatlearn.shared.generated.resources.avatar_pose_cheer
import tatlearn.shared.generated.resources.avatar_pose_correct
import tatlearn.shared.generated.resources.avatar_pose_greeting
import tatlearn.shared.generated.resources.avatar_pose_searching
import tatlearn.shared.generated.resources.avatar_pose_sleeping
import tatlearn.shared.generated.resources.avatar_pose_timer
import tatlearn.shared.generated.resources.avatar_poses_label

private val SlotSize = 48.dp
private val InnerSize = 36.dp
private val RingWidth = 3.dp
private val BadgeSize = 18.dp
private const val GRID_COLUMNS = 6

private val colorDescriptions: List<StringResource> = listOf(
    Res.string.avatar_color_0, Res.string.avatar_color_1, Res.string.avatar_color_2, Res.string.avatar_color_3,
)

private fun poseDescription(pose: AvatarPose): StringResource = when (pose) {
    AvatarPose.Greeting -> Res.string.avatar_pose_greeting
    AvatarPose.Cheer -> Res.string.avatar_pose_cheer
    AvatarPose.Searching -> Res.string.avatar_pose_searching
    AvatarPose.Sleeping -> Res.string.avatar_pose_sleeping
    AvatarPose.Correct -> Res.string.avatar_pose_correct
    AvatarPose.Timer -> Res.string.avatar_pose_timer
}

/**
 * Выбор аватара (макеты 25 и 32): ряд цветов с буквой и, если [showPoses], ряд поз Мыраужана; обе строки на одной сетке из 6 колонок.
 * Цвета и позы образуют одну радиогруппу: выбран ровно один вариант. Выбор виден кольцом и значком-галочкой, а не только цветом.
 */
@Composable
fun AvatarPicker(
    selected: Avatar,
    name: String,
    onSelect: (Avatar) -> Unit,
    showPoses: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
        PickerRow(label = stringResource(Res.string.avatar_colors_label)) {
            repeat(AvatarColors.COUNT) { index ->
                val avatar = Avatar.Color(index)
                val palette = avatarPalette(index)
                Slot(
                    selected = selected == avatar,
                    description = stringResource(colorDescriptions[index]),
                    onClick = { onSelect(avatar) },
                ) {
                    Box(
                        Modifier.size(InnerSize).clip(TamyrTheme.shapes.circle).background(palette.background)
                            .border(TamyrSizes.borderAccent, TamyrTheme.colors.textSecondary, TamyrTheme.shapes.circle),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected == avatar) CheckIcon(size = CHECK_SIZE, tint = palette.content)
                    }
                }
            }
            repeat(GRID_COLUMNS - AvatarColors.COUNT) { Box(Modifier.size(SlotSize)) }
        }
        if (showPoses) {
            PickerRow(label = stringResource(Res.string.avatar_poses_label)) {
                AvatarPose.entries.forEach { pose ->
                    val avatar = Avatar.Pose(pose)
                    Slot(
                        selected = selected == avatar,
                        description = stringResource(poseDescription(pose)),
                        onClick = { onSelect(avatar) },
                    ) {
                        ProfileAvatar(avatar, name, InnerSize)
                        if (selected == avatar) {
                            Box(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = BadgeOffset, y = BadgeOffset)
                                    .size(BadgeSize)
                                    .clip(TamyrTheme.shapes.circle)
                                    .background(TamyrTheme.colors.primary)
                                    .border(TamyrSizes.borderAccent, TamyrTheme.colors.background, TamyrTheme.shapes.circle),
                                contentAlignment = Alignment.Center,
                            ) { CheckIcon(size = BADGE_CHECK_SIZE) }
                        }
                    }
                }
            }
        }
    }
}

private val CHECK_SIZE = 18.dp
private val BADGE_CHECK_SIZE = 11.dp
private val BadgeOffset = 2.dp

@Composable
private fun PickerRow(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs)) {
        Text(label, style = TamyrTheme.typography.body14Bold, color = TamyrTheme.colors.textSecondary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { content() }
    }
}

/** Слот 48×48 dp: внутри кружок 36 dp, выбранный получает кольцо `textPrimary` 3 dp без сдвига сетки. */
@Composable
private fun Slot(selected: Boolean, description: String, onClick: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    val ring = if (selected) Modifier.border(RingWidth, TamyrTheme.colors.textPrimary, TamyrTheme.shapes.circle) else Modifier
    Box(
        Modifier
            .size(SlotSize)
            .clip(TamyrTheme.shapes.circle)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = description }
            .then(ring),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Preview(name = "Avatar picker", widthDp = 390, heightDp = 280)
@Composable
private fun AvatarPickerPreview() = PreviewFrame {
    AvatarPicker(selected = Avatar.Pose(AvatarPose.Searching), name = "Алия", onSelect = {}, showPoses = true, modifier = Modifier.padding(24.dp))
}

@Preview(name = "Avatar picker · colors only", widthDp = 390, heightDp = 200)
@Composable
private fun AvatarPickerColorsPreview() = PreviewFrame {
    AvatarPicker(selected = Avatar.Color(2), name = "Алия", onSelect = {}, showPoses = false, modifier = Modifier.padding(24.dp))
}
