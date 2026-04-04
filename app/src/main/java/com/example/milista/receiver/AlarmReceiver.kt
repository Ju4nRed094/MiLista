package com.example.milista.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.milista.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", 0)
        val title = intent.getStringExtra("title") ?: "Alarma"
        val tonoUriString = intent.getStringExtra("tono_uri")
        val action = intent.action

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (action) {
            ACTION_SNOOZE -> {
                snoozeAlarm(context, alarmId, title, tonoUriString)
                notificationManager.cancel(alarmId)
                return
            }
            ACTION_DISMISS -> {
                notificationManager.cancel(alarmId)
                return
            }
        }

        val channelId = "milista_alarms"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmas de MiLista",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para las alarmas configuradas"
                setSound(null, null)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("alarm_id", alarmId)
            putExtra("title", title)
            putExtra("tono_uri", tonoUriString)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, alarmId, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_SNOOZE
            putExtra("alarm_id", alarmId)
            putExtra("title", title)
            putExtra("tono_uri", tonoUriString)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, alarmId + 1000, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_DISMISS
            putExtra("alarm_id", alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, alarmId + 2000, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("MiLista")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_recent_history, "Posponer (5 min)", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Descartar", dismissPendingIntent)

        val tonoUri = tonoUriString?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        notificationBuilder.setSound(tonoUri)

        notificationManager.notify(alarmId, notificationBuilder.build())
    }

    private fun snoozeAlarm(context: Context, alarmId: Int, title: String, tonoUri: String?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("title", title)
            putExtra("tono_uri", tonoUri)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeTime = System.currentTimeMillis() + 5 * 60 * 1000
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
        }
    }

    companion object {
        const val ACTION_SNOOZE = "com.example.milista.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.example.milista.ACTION_DISMISS"
    }
}
