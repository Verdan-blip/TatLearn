package ru.tatarcha.tatlearn.feature.home.roadmap

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ru.tatarcha.tatlearn.core.designsystem.component.PreviewFrame
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.module_book
import tatlearn.shared.generated.resources.module_book_locked
import tatlearn.shared.generated.resources.module_calendar
import tatlearn.shared.generated.resources.module_calendar_locked
import tatlearn.shared.generated.resources.module_city
import tatlearn.shared.generated.resources.module_city_locked
import tatlearn.shared.generated.resources.module_gate
import tatlearn.shared.generated.resources.module_gate_locked
import tatlearn.shared.generated.resources.module_house
import tatlearn.shared.generated.resources.module_house_locked
import tatlearn.shared.generated.resources.module_nature
import tatlearn.shared.generated.resources.module_nature_locked
import tatlearn.shared.generated.resources.module_samovar
import tatlearn.shared.generated.resources.module_samovar_locked
import tatlearn.shared.generated.resources.module_shop
import tatlearn.shared.generated.resources.module_shop_locked
import tatlearn.shared.generated.resources.module_verbs
import tatlearn.shared.generated.resources.module_verbs_locked

/**
 * Иллюстрация модуля по его `icon` из каталога; закрытому модулю соответствует приглушённая версия.
 * Образы (калитка, книга, дом с наличниками, календарь, самовар, улица, лейка, прилавок, берёза): needs native-speaker review.
 * Исходники и экспорт: design/home.
 */
@Composable
fun ModuleIllustration(
    icon: String,
    locked: Boolean,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(illustrationFor(icon, locked)),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

private fun illustrationFor(icon: String, locked: Boolean): DrawableResource = when (icon) {
    "meeting" -> if (locked) Res.drawable.module_gate_locked else Res.drawable.module_gate
    "numbers" -> if (locked) Res.drawable.module_calendar_locked else Res.drawable.module_calendar
    "food" -> if (locked) Res.drawable.module_samovar_locked else Res.drawable.module_samovar
    "nature" -> if (locked) Res.drawable.module_nature_locked else Res.drawable.module_nature
    "family" -> if (locked) Res.drawable.module_house_locked else Res.drawable.module_house
    "city" -> if (locked) Res.drawable.module_city_locked else Res.drawable.module_city
    "verbs" -> if (locked) Res.drawable.module_verbs_locked else Res.drawable.module_verbs
    "shop" -> if (locked) Res.drawable.module_shop_locked else Res.drawable.module_shop
    else -> if (locked) Res.drawable.module_book_locked else Res.drawable.module_book
}

@Preview
@Composable
private fun ModuleIllustrationPreview() = PreviewFrame {
    ModuleIllustration(
        icon = "family",
        locked = false,
    )
}
