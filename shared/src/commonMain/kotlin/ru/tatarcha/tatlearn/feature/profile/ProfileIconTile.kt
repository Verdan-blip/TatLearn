package ru.tatarcha.tatlearn.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

@Composable
internal fun ProfileIconTile(icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(TamyrSizes.iconTile)
            .clip(TamyrTheme.shapes.chip)
            .background(TamyrTheme.colors.successContainer),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}
