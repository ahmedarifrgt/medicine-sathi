package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.preference.UserPreferencesRepository
import com.example.data.repository.MedicineRepository
import com.example.service.NotificationHelper

class MedicineSathiApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: MedicineRepository by lazy { MedicineRepository(database) }
    val preferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this)
    }

    companion object {
        lateinit var instance: MedicineSathiApp
            private set
    }
}
