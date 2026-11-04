package ru.tatarcha.tatlearn.feature.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import ru.tatarcha.tatlearn.core.designsystem.component.TamyrCard
import ru.tatarcha.tatlearn.core.designsystem.component.icon.ChevronDownIcon
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.about_license_collapse
import tatlearn.shared.generated.resources.about_license_expand
import tatlearn.shared.generated.resources.about_license_head
import tatlearn.shared.generated.resources.about_license_text
import tatlearn.shared.generated.resources.about_license_title

@Composable
fun AboutLicenseCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TamyrCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = TamyrTheme.spacing.l,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
            Text(
                text = stringResource(Res.string.about_license_title),
                style = TamyrTheme.typography.body16Bold,
                color = TamyrTheme.colors.textPrimary,
            )
            SelectionContainer {
                Text(
                    text = stringResource(if (expanded) Res.string.about_license_text else Res.string.about_license_head),
                    style = TamyrTheme.typography.caption13,
                    color = TamyrTheme.colors.textSecondary,
                )
            }
            Row(
                modifier = Modifier
                    .heightIn(min = TamyrSizes.topBarAction)
                    .clickable(
                        role = Role.Button,
                        onClick = onToggle,
                    ),
                horizontalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(if (expanded) Res.string.about_license_collapse else Res.string.about_license_expand),
                    style = TamyrTheme.typography.body15Bold,
                    color = TamyrTheme.colors.primary,
                )
                ChevronDownIcon(modifier = Modifier.rotate(if (expanded) 180f else 0f))
            }
        }
    }
}

@Preview(name = "Collapsed")
@Composable
private fun AboutLicenseCardCollapsedPreview() = PreviewFrame {
    AboutLicenseCard(
        expanded = false,
        onToggle = {},
    )
}

@Preview(name = "Expanded")
@Composable
private fun AboutLicenseCardExpandedPreview() = PreviewFrame {
    AboutLicenseCard(
        expanded = true,
        onToggle = {},
    )
}
