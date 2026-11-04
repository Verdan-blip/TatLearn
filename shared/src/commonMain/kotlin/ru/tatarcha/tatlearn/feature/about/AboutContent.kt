package ru.tatarcha.tatlearn.feature.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.MascotPose
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrMascot
import ru.tatarcha.tatlearn.core.designsystem.component.icon.ChevronBackIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.about_back_description
import tatlearn.shared.generated.resources.about_license_text
import tatlearn.shared.generated.resources.about_license_title
import tatlearn.shared.generated.resources.about_tagline
import tatlearn.shared.generated.resources.about_title
import tatlearn.shared.generated.resources.about_voice_text
import tatlearn.shared.generated.resources.about_voice_title
import tatlearn.shared.generated.resources.app_name

private val MascotSize = 120.dp

// TODO(design): экран «О приложении» не нарисован в макете, собран из компонентов дизайн-системы.
@Composable
fun AboutContent(onBack: () -> Unit) {
    val backDescription = stringResource(Res.string.about_back_description)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TamyrTheme.colors.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = TamyrTheme.spacing.m,
                    end = TamyrTheme.spacing.screen,
                    top = TamyrTheme.spacing.s,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xxs),
        ) {
            Box(
                modifier = Modifier
                    .size(TamyrSizes.topBarAction)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = backDescription },
                contentAlignment = Alignment.Center,
            ) {
                ChevronBackIcon()
            }
            Text(
                text = stringResource(Res.string.about_title),
                style = TamyrTheme.typography.title22,
                color = TamyrTheme.colors.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(
                    horizontal = TamyrTheme.spacing.screen,
                    vertical = TamyrTheme.spacing.l,
                ),
            verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.l),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s),
            ) {
                TamyrMascot(
                    pose = MascotPose.Greeting,
                    size = MascotSize,
                )
                Text(
                    text = stringResource(Res.string.app_name),
                    style = TamyrTheme.typography.headline24,
                    color = TamyrTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(Res.string.about_tagline),
                    style = TamyrTheme.typography.body14,
                    color = TamyrTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            AboutCard(
                title = stringResource(Res.string.about_voice_title),
                text = stringResource(Res.string.about_voice_text),
            )
            AboutCard(
                title = stringResource(Res.string.about_license_title),
                text = stringResource(Res.string.about_license_text),
            )
        }
    }
}

@Composable
private fun AboutCard(
    title: String,
    text: String,
) {
    TamyrCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = TamyrTheme.spacing.m,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
            Text(
                text = title,
                style = TamyrTheme.typography.body16Bold,
                color = TamyrTheme.colors.textPrimary,
            )
            Text(
                text = text,
                style = TamyrTheme.typography.caption13,
                color = TamyrTheme.colors.textSecondary,
            )
        }
    }
}

@Preview(name = "About")
@Composable
private fun AboutContentPreview() = PreviewFrame {
    AboutContent(onBack = {})
}
