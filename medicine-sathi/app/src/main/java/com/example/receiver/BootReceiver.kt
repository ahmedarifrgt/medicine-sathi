package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MedicineSathiApp
import com.example.service.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as? MedicineSathiApp ?: return
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            CoroutineScope(Dispatchers.IO).launch {
                val medicines = app.repository.allMedicines.first()
                for (med in medicines) {
                    val doseTimes = med.doseTimesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    for (time in doseTimes) {
                        AlarmScheduler.scheduleDoseReminder(
                            context = context,
                            medicineId = med.id,
                            medicineName = med.name,
                            memberName = "Family Member",
                            scheduledDate = todayStr,
                            timeString = time
                        )
                    }
                }
            }
        }
    }
}
