package com.linksink.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.linksink.MainActivity
import com.linksink.R
import com.linksink.model.Link

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "link_reminders"
        private const val CHANNEL_NAME = "Link Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for unread links"
        const val NOTIFICATION_ID_LINK_REMINDER = 1001
        const val EXTRA_LINK_ID = "extra_link_id"
        const val ACTION_OPEN_LINK = "com.linksink.ACTION_OPEN_LINK"
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildLinkReminderNotification(link: Link): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_LINK
            putExtra(EXTRA_LINK_ID, link.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            link.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = link.title ?: link.domain
        val contentText = "You have an unread link: $title"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Link Reminder")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return builder.build()
    }

    fun showNotification(notificationId: Int, notification: Notification) {
        notificationManager.notify(notificationId, notification)
    }
}
