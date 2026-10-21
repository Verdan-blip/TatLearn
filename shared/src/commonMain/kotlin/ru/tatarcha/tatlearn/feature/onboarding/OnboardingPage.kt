package ru.tatarcha.tatlearn.feature.onboarding

import org.jetbrains.compose.resources.StringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.onboarding_body_1
import tatlearn.shared.generated.resources.onboarding_body_2
import tatlearn.shared.generated.resources.onboarding_body_3
import tatlearn.shared.generated.resources.onboarding_title_1
import tatlearn.shared.generated.resources.onboarding_title_2
import tatlearn.shared.generated.resources.onboarding_title_3

data class OnboardingPage(
    val title: StringResource,
    val body: StringResource,
    val pose: MascotPose,
)

// TODO(design): страниц 2 и 3 нет в макете, тексты — заглушка.
internal val DefaultOnboardingPages = listOf(
    OnboardingPage(
        title = Res.string.onboarding_title_1,
        body = Res.string.onboarding_body_1,
        pose = MascotPose.Greeting,
    ),
    OnboardingPage(
        title = Res.string.onboarding_title_2,
        body = Res.string.onboarding_body_2,
        pose = MascotPose.Timer,
    ),
    OnboardingPage(
        title = Res.string.onboarding_title_3,
        body = Res.string.onboarding_body_3,
        pose = MascotPose.Progress,
    ),
)
