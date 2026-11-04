package ru.tatarcha.tatlearn.feature.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun AboutScene(onBack: () -> Unit) {
    // Развёрнутость лицензии: чисто визуальное состояние, у экрана нет ViewModel.
    var licenseExpanded by rememberSaveable { mutableStateOf(false) }
    AboutContent(
        licenseExpanded = licenseExpanded,
        onLicenseToggle = { licenseExpanded = !licenseExpanded },
        onBack = onBack,
    )
}
