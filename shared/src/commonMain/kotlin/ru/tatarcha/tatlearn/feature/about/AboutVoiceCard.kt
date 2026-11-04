package ru.tatarcha.tatlearn.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.about_voice_link
import tatlearn.shared.generated.resources.about_voice_text
import tatlearn.shared.generated.resources.about_voice_title

private const val SILERO_URL = "https://github.com/snakers4/silero-models"

@Composable
fun AboutVoiceCard(modifier: Modifier = Modifier) {
    val link = stringResource(Res.string.about_voice_link)
    val text = stringResource(Res.string.about_voice_text, link)
    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = TamyrTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
        ),
    )
    val annotated = remember(text, link, linkStyles) {
        val start = text.indexOf(link)
        buildAnnotatedString {
            append(text.substring(0, start))
            withLink(LinkAnnotation.Url(url = SILERO_URL, styles = linkStyles)) { append(link) }
            append(text.substring(start + link.length))
        }
    }
    TamyrCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = TamyrTheme.spacing.l,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
            Text(
                text = stringResource(Res.string.about_voice_title),
                style = TamyrTheme.typography.body16Bold,
                color = TamyrTheme.colors.textPrimary,
            )
            Text(
                text = annotated,
                style = TamyrTheme.typography.caption13,
                color = TamyrTheme.colors.textSecondary,
            )
        }
    }
}

@Preview
@Composable
private fun AboutVoiceCardPreview() = PreviewFrame {
    AboutVoiceCard()
}
