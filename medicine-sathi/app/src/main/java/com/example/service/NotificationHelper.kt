package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.receiver.NotificationActionReceiver

object NotificationHelper {
    const val CHANNEL_MEDICINE = "channel_medicine_reminders"
    const val CHANNEL_APPOINTMENT = "channel_appointment_reminders"
    const val CHANNEL_EXPIRY = "channel_expiry_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val medChannel = NotificationChannel(
                CHANNEL_MEDICINE,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled doses"
                enableVibration(true)
            }

            val aptChannel = NotificationChannel(
                CHANNEL_APPOINTMENT,
                "Appointment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for doctor appointments"
                enableVibration(true)
            }

            val expChannel = NotificationChannel(
                CHANNEL_EXPIRY,
                "Expiry & Inventory Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when medicines are expiring or low in stock"
            }

            notificationManager.createNotificationChannel(medChannel)
            notificationManager.createNotificationChannel(aptChannel)
            notificationManager.createNotificationChannel(expChannel)
        }
    }

    fun showMedicineNotification(
        context: Context,
        notificationId: Int,
        medicineId: Long,
        medicineName: String,
        memberName: String,
        scheduledDate: String,
        scheduledTime: String,
        isPrivate: Boolean
    ) {
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: TAKEN
        val takenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TAKEN
            putExtra("notificationId", notificationId)
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("scheduledDate", scheduledDate)
            putExtra("scheduledTime", scheduledTime)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: SNOOZE (10 mins)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra("notificationId", notificationId)
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("scheduledDate", scheduledDate)
            putExtra("scheduledTime", scheduledTime)
            putExtra("snoozeMinutes", 10)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: SKIP
        val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SKIP
            putExtra("notificationId", notificationId)
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("scheduledDate", scheduledDate)
            putExtra("scheduledTime", scheduledTime)
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 3,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPrivate) "Medicine Sathi Reminder" else "Time to take $medicineName"
        val message = if (isPrivate) "You have a scheduled medicine to take." else "Scheduled dose for $memberName at $scheduledTime"

        val builder = NotificationCompat.Builder(context, CHANNEL_MEDICINE)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Taken", takenPendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze 10m", snoozePendingIntent)
            .addAction(android.R.drawable.ic_delete, "Skip", skipPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun showAppointmentNotification(
        context: Context,
        notificationId: Int,
        doctorName: String,
        hospital: String,
        time: String
    ) {
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_APPOINTMENT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming Appointment")
            .setContentText("Appointment with Dr. $doctorName at $hospital ($time)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun showExpiryNotification(
        context: Context,
        notificationId: Int,
        medicineName: String,
        warningText: String
    ) {
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_EXPIRY)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Medicine Expiry Alert")
            .setContentText("$medicineName: $warningText")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}
