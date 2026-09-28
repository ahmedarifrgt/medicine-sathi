package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.receiver.ReminderAlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    fun scheduleDoseReminder(
        context: Context,
        medicineId: Long,
        medicineName: String,
        memberName: String,
        scheduledDate: String, // YYYY-MM-DD
        timeString: String // HH:mm
    ) {
        val parts = timeString.split(":")
        if (parts.size < 2) return

        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            val dateParts = scheduledDate.split("-")
            if (dateParts.size == 3) {
                set(Calendar.YEAR, dateParts[0].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
            }
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            // Already in past for today, don't schedule past
            return
        }

        val requestCode = (medicineId.toString() + hour + minute).hashCode()

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_MEDICINE_ALARM
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("memberName", memberName)
            putExtra("scheduledDate", scheduledDate)
            putExtra("scheduledTime", timeString)
            putExtra("notificationId", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun scheduleSnoozeReminder(
        context: Context,
        medicineId: Long,
        medicineName: String,
        scheduledDate: String,
        scheduledTime: String,
        notificationId: Int,
        snoozeMinutes: Int
    ) {
        val triggerAt = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_MEDICINE_ALARM
            putExtra("medicineId", medicineId)
            putExtra("medicineName", medicineName)
            putExtra("memberName", "Family Member")
            putExtra("scheduledDate", scheduledDate)
            putExtra("scheduledTime", scheduledTime)
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }
}
