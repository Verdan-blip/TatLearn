package ru.tatarcha.tatlearn.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrSizes
import ru.tatarcha.tatlearn.core.designsystem.theme.TamyrTheme

/** Однострочное поле с подписью: рамка 1dp, 2dp зелёная в фокусе, терракотовая при ошибке. */
@Composable
fun TamyrTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    errorText: String? = null,
    singleLine: Boolean = true,
    minHeight: Dp = FieldHeight,
    textStyle: TextStyle = TamyrTheme.typography.body16,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    // Хранит курсор/выделение у себя, пока вызывающий код работает только с обычным текстом.
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    if (fieldValue.text != value) fieldValue = fieldValue.copy(text = value)
    TamyrTextField(
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            if (it.text != value) onValueChange(it.text)
        },
        modifier = modifier,
        label = label,
        errorText = errorText,
        singleLine = singleLine,
        minHeight = minHeight,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailing = trailing,
    )
}

/** Вариант с доступом к выделению, чтобы вызывающий код мог вставлять текст в позицию курсора (ряд татарских букв). */
@Composable
fun TamyrTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    errorText: String? = null,
    singleLine: Boolean = true,
    minHeight: Dp = FieldHeight,
    textStyle: TextStyle = TamyrTheme.typography.body16,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = TamyrTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor = when {
        errorText != null -> colors.accent
        focused -> colors.primary
        else -> colors.border
    }
    val borderWidth = if (focused || errorText != null) TamyrSizes.borderAccent else TamyrSizes.borderThin

    Column(modifier, verticalArrangement = Arrangement.spacedBy(TamyrTheme.spacing.s)) {
        if (label != null) {
            Text(label, style = TamyrTheme.typography.body14Bold, color = colors.textSecondary)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            interactionSource = interaction,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = textStyle.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .background(colors.surface, TamyrTheme.shapes.answer)
                        .border(borderWidth, borderColor, TamyrTheme.shapes.answer)
                        .padding(horizontal = TamyrTheme.spacing.l, vertical = TamyrTheme.spacing.s),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                ) {
                    Box(Modifier.weight(1f)) { inner() }
                    trailing?.invoke()
                }
            },
        )
        if (errorText != null) {
            // TODO(design): стиля ошибки валидации нет в макете.
            Text(errorText, style = TamyrTheme.typography.caption13, color = colors.accent)
        }
    }
}

private val FieldHeight = 52.dp
