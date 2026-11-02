package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import ru.tatarcha.tatlearn.domain.model.Avatar
import ru.tatarcha.tatlearn.domain.model.AvatarPose
import ru.tatarcha.tatlearn.domain.model.ProfileRules

/** Фон и цвет буквы для цвета аватара; золото получает тёмную букву (светлая не проходит по контрасту). */
internal data class AvatarPalette(val background: Color, val content: Color)

@Composable
internal fun avatarPalette(index: Int): AvatarPalette {
    val colors = TamyrTheme.colors
    return when (index) {
        1 -> AvatarPalette(colors.accent, colors.onAccent)
        2 -> AvatarPalette(colors.gold, colors.textPrimary)
        3 -> AvatarPalette(colors.textSecondary, colors.onPrimary)
        else -> AvatarPalette(colors.primary, colors.onPrimary)
    }
}

/**
 * Кадр головы позы в системе координат 400×400: PNG маскота (768×768, кот обрезан по контуру) растягивается на эту сетку.
 * Отрицательные значения допустимы: кадр может выходить за край картинки (это прозрачное поле).
 */
internal data class PoseCrop(val x: Float, val y: Float, val size: Float)

internal val AvatarPose.mascot: MascotPose
    get() = when (this) {
        AvatarPose.Greeting -> MascotPose.Greeting
        AvatarPose.Cheer -> MascotPose.Cheer
        AvatarPose.Searching -> MascotPose.Searching
        AvatarPose.Sleeping -> MascotPose.Sleeping
        AvatarPose.Correct -> MascotPose.Correct
        AvatarPose.Timer -> MascotPose.Timer
    }

internal val AvatarPose.crop: PoseCrop
    get() = when (this) {
        // Кадры подобраны под рыжего кота из Nano Banana (02.11.2026): голова целиком, уши внутри круга.
        AvatarPose.Greeting -> PoseCrop(46f, -16f, 268f)
        AvatarPose.Cheer -> PoseCrop(99f, 40f, 200f)
        AvatarPose.Searching -> PoseCrop(53f, -10f, 250f)
        AvatarPose.Sleeping -> PoseCrop(-16f, 94f, 262f)
        AvatarPose.Correct -> PoseCrop(48f, -8f, 280f)
        AvatarPose.Timer -> PoseCrop(65f, -9f, 232f)
    }

private const val SOURCE_SIZE = 400f

/**
 * Аватар профиля: цветной круг с первой буквой имени (пустое имя даёт букву имени по умолчанию) или голова Мыраужана в круге.
 * Обводка 2dp цвета `textSecondary` нужна, чтобы светлые цвета (золото) не сливались с фоном.
 */
@Composable
fun ProfileAvatar(avatar: Avatar, name: String, size: Dp, modifier: Modifier = Modifier) {
    val colors = TamyrTheme.colors
    val border = Modifier.border(TamyrSizes.borderAccent, colors.textSecondary, TamyrTheme.shapes.circle)
    when (avatar) {
        is Avatar.Color -> {
            val palette = avatarPalette(avatar.index)
            Box(
                modifier.size(size).clip(TamyrTheme.shapes.circle).background(palette.background).then(border),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ProfileRules.initial(name).ifEmpty { ProfileRules.initial(ProfileRules.DEFAULT_NAME) },
                    style = TextStyle(fontSize = (size.value * LETTER_RATIO).sp, fontWeight = TamyrTheme.typography.body17Bold.fontWeight, fontFamily = TamyrTheme.typography.body17Bold.fontFamily),
                    color = palette.content,
                )
            }
        }
        is Avatar.Pose -> {
            val crop = avatar.pose.crop
            val scale = SOURCE_SIZE / crop.size
            val full = size * scale
            // requiredSize крупнее родителя Compose центрирует (а не прижимает к левому верхнему углу), поэтому смещаем от центра:
            // центр кадра головы должен оказаться в центре круга.
            val shiftX = full * ((SOURCE_SIZE / 2 - (crop.x + crop.size / 2)) / SOURCE_SIZE)
            val shiftY = full * ((SOURCE_SIZE / 2 - (crop.y + crop.size / 2)) / SOURCE_SIZE)
            Box(
                modifier.size(size).clip(TamyrTheme.shapes.circle).background(colors.surfaceMuted).then(border),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(avatar.pose.mascot.drawable),
                    contentDescription = null,
                    modifier = Modifier.requiredSize(full).offset(x = shiftX, y = shiftY),
                )
            }
        }
    }
}

private const val LETTER_RATIO = 0.42f

@Preview(name = "Avatars", widthDp = 390, heightDp = 200)
@Composable
private fun ProfileAvatarPreview() = PreviewFrame {
    Row(
        Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(4) { ProfileAvatar(Avatar.Color(it), "Алия", 56.dp) }
        ProfileAvatar(Avatar.Pose(AvatarPose.Searching), "Алия", 56.dp)
        ProfileAvatar(Avatar.Pose(AvatarPose.Sleeping), "Алия", 56.dp)
    }
}
