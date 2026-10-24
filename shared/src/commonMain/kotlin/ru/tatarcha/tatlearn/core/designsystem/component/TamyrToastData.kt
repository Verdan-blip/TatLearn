package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.runtime.Immutable

@Immutable
data class TamyrToastData(
    val text: String,
    val kind: TamyrToastKind,
)
