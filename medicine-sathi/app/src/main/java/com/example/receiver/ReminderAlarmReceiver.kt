package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MedicineSathiApp
import com.example.data.model.AppNotification
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val app = context.applicationContext as? MedicineSathiApp ?: return

        if (action == ACTION_MEDICINE_ALARM) {
            val medicineId = intent.getLongExtra("medicineId", 0L)
            val medicineName = intent.getStringExtra("medicineName") ?: "Medicine"
            val memberName = intent.getStringExtra("memberName") ?: "Family Member"
            val scheduledDate = intent.getStringExtra("scheduledDate") ?: ""
            val scheduledTime = intent.getStringExtra("scheduledTime") ?: ""
            val notificationId = intent.getIntExtra("notificationId", (System.currentTimeMillis() % 100000).toInt())

            val isPrivate = app.preferencesRepository.privateNotifications.value

            NotificationHelper.showMedicineNotification(
                context = context,
                notificationId = notificationId,
                medicineId = medicineId,
                medicineName = medicineName,
                memberName = memberName,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                isPrivate = isPrivate
            )

            // Insert into in-app notification center
            CoroutineScope(Dispatchers.IO).launch {
                app.repository.insertNotification(
                    AppNotification(
                        title = if (isPrivate) "Medicine Sathi Reminder" else "Time for $medicineName",
                        message = "Scheduled dose for $memberName at $scheduledTime",
                        category = "MEDICINE",
                        relatedEntityId = medicineId
                    )
                )
            }
        }
    }

    companion object {
        const val ACTION_MEDICINE_ALARM = "com.example.ACTION_MEDICINE_ALARM"
    }
}
