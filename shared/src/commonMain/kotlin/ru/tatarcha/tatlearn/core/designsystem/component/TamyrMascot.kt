package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.mascot_cheer
import tatlearn.shared.generated.resources.mascot_correct
import tatlearn.shared.generated.resources.mascot_greeting
import tatlearn.shared.generated.resources.mascot_head_icon
import tatlearn.shared.generated.resources.mascot_progress
import tatlearn.shared.generated.resources.mascot_sad
import tatlearn.shared.generated.resources.mascot_searching
import tatlearn.shared.generated.resources.mascot_sleeping
import tatlearn.shared.generated.resources.mascot_teacher
import tatlearn.shared.generated.resources.mascot_timer
import tatlearn.shared.generated.resources.mascot_tired
import tatlearn.shared.generated.resources.mascot_wrong

/**
 * Позы маскота Мыраужана (пухлый рыжий полосатый кот в тёмно-морской тюбетейке).
 * Источники: картинки Nano Banana в design/mascot/gemini, растровые PNG 768x768 собирает design/mascot/build_mascot_png.py
 * (см. design/mascot-prompt.md). [Correct] и [Wrong] нарисованы по пояс, [HeadIcon] только голова.
 */
enum class MascotPose(val drawable: DrawableResource) {
    Greeting(Res.drawable.mascot_greeting),
    Timer(Res.drawable.mascot_timer),
    Progress(Res.drawable.mascot_progress),
    Sad(Res.drawable.mascot_sad),
    Searching(Res.drawable.mascot_searching),
    Cheer(Res.drawable.mascot_cheer),
    Correct(Res.drawable.mascot_correct),
    Wrong(Res.drawable.mascot_wrong),
    Tired(Res.drawable.mascot_tired),
    Teacher(Res.drawable.mascot_teacher),
    Sleeping(Res.drawable.mascot_sleeping),
    HeadIcon(Res.drawable.mascot_head_icon),
}

/** Декоративная картинка маскота (без описания: смысл несёт текст экрана). */
@Composable
fun TamyrMascot(pose: MascotPose, size: Dp, modifier: Modifier = Modifier) {
    Image(painterResource(pose.drawable), contentDescription = null, modifier = modifier.size(size))
}
