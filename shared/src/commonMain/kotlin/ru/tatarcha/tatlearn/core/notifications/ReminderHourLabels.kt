package ru.tatarcha.tatlearn.core.notifications

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tatlearn.shared.generated.resources.Res
import tatlearn.shared.generated.resources.reminder_time

/** Подписи кнопок времени: «08:00», «13:00» (ведущий ноль добавляем в коде, ресурсы его не умеют). */
@Composable
fun reminderHourLabels(hours: List<Int>): List<String> =
    hours.map { hour -> stringResource(Res.string.reminder_time, hour.toString().padStart(2, '0')) }
