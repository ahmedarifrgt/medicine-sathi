package com.example.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MedicineSathiApp
import com.example.service.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val app = context.applicationContext as? MedicineSathiApp ?: return

        val notificationId = intent.getIntExtra("notificationId", 0)
        val medicineId = intent.getLongExtra("medicineId", 0L)
        val medicineName = intent.getStringExtra("medicineName") ?: "Medicine"
        val scheduledDate = intent.getStringExtra("scheduledDate") ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val scheduledTime = intent.getStringExtra("scheduledTime") ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        when (action) {
            ACTION_TAKEN -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val med = app.repository.getMedicineById(medicineId)
                    val memberId = med?.familyMemberId ?: 0L
                    app.repository.recordDoseAction(
                        medicineId = medicineId,
                        medicineName = medicineName,
                        memberId = memberId,
                        scheduledDate = scheduledDate,
                        scheduledTime = scheduledTime,
                        status = "TAKEN",
                        notes = "Marked from notification"
                    )
                }
            }
            ACTION_SKIP -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val med = app.repository.getMedicineById(medicineId)
                    val memberId = med?.familyMemberId ?: 0L
                    app.repository.recordDoseAction(
                        medicineId = medicineId,
                        medicineName = medicineName,
                        memberId = memberId,
                        scheduledDate = scheduledDate,
                        scheduledTime = scheduledTime,
                        status = "SKIPPED",
                        notes = "Skipped from notification"
                    )
                }
            }
            ACTION_SNOOZE -> {
                val snoozeMinutes = intent.getIntExtra("snoozeMinutes", 10)
                AlarmScheduler.scheduleSnoozeReminder(
                    context = context,
                    medicineId = medicineId,
                    medicineName = medicineName,
                    scheduledDate = scheduledDate,
                    scheduledTime = scheduledTime,
                    notificationId = notificationId,
                    snoozeMinutes = snoozeMinutes
                )
            }
        }
    }

    companion object {
        const val ACTION_TAKEN = "com.example.ACTION_NOTIFICATION_TAKEN"
        const val ACTION_SKIP = "com.example.ACTION_NOTIFICATION_SKIP"
        const val ACTION_SNOOZE = "com.example.ACTION_NOTIFICATION_SNOOZE"
    }
}
