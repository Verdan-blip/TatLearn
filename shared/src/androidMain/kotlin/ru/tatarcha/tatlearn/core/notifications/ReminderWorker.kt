package ru.tatarcha.tatlearn.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Показывает ежедневное напоминание и ставит следующее на завтра. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val hour = inputData.getInt(KEY_HOUR, -1)
        val texts = ReminderTexts(
            title = inputData.getString(KEY_TITLE).orEmpty(),
            body = inputData.getString(KEY_BODY).orEmpty(),
            channelName = inputData.getString(KEY_CHANNEL).orEmpty(),
        )
        if (hour < 0) return Result.failure()
        showReminderNotification(applicationContext, texts)
        enqueueReminder(applicationContext, hour, texts)
        return Result.success()
    }
}

private const val CHANNEL_ID = "lesson_reminder"
private const val NOTIFICATION_ID = 1

internal fun showReminderNotification(context: Context, texts: ReminderTexts) {
    val manager = NotificationManagerCompat.from(context)
    if (!manager.areNotificationsEnabled()) return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, texts.channelName, NotificationManager.IMPORTANCE_DEFAULT),
        )
    }
    val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
    val tap = launch?.let { PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT) }
    // TODO(design): маленькая иконка уведомления сейчас иконка приложения; нужна одноцветная.
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(context.applicationInfo.icon)
        .setContentTitle(texts.title)
        .setContentText(texts.body)
        .setContentIntent(tap)
        .setAutoCancel(true)
        .build()
    // Разрешение проверено выше через areNotificationsEnabled.
    @Suppress("MissingPermission")
    manager.notify(NOTIFICATION_ID, notification)
}
