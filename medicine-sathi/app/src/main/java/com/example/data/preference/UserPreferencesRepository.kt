package com.example.data.preference

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("medicine_sathi_prefs", Context.MODE_PRIVATE)

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _privateNotifications = MutableStateFlow(prefs.getBoolean(KEY_PRIVATE_NOTIF, false))
    val privateNotifications: StateFlow<Boolean> = _privateNotifications.asStateFlow()

    private val _medicineReminders = MutableStateFlow(prefs.getBoolean(KEY_MED_REMINDERS, true))
    val medicineReminders: StateFlow<Boolean> = _medicineReminders.asStateFlow()

    private val _appointmentReminders = MutableStateFlow(prefs.getBoolean(KEY_APT_REMINDERS, true))
    val appointmentReminders: StateFlow<Boolean> = _appointmentReminders.asStateFlow()

    private val _expiryReminders = MutableStateFlow(prefs.getBoolean(KEY_EXP_REMINDERS, true))
    val expiryReminders: StateFlow<Boolean> = _expiryReminders.asStateFlow()

    private val _caregiverNotifications = MutableStateFlow(prefs.getBoolean(KEY_CG_NOTIF, true))
    val caregiverNotifications: StateFlow<Boolean> = _caregiverNotifications.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _appPin = MutableStateFlow(prefs.getString(KEY_APP_PIN, "") ?: "")
    val appPin: StateFlow<String> = _appPin.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, completed).apply()
        _isOnboardingCompleted.value = completed
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME, mode).apply()
        _themeMode.value = mode
    }

    fun setPrivateNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVATE_NOTIF, enabled).apply()
        _privateNotifications.value = enabled
    }

    fun setMedicineReminders(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MED_REMINDERS, enabled).apply()
        _medicineReminders.value = enabled
    }

    fun setAppointmentReminders(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APT_REMINDERS, enabled).apply()
        _appointmentReminders.value = enabled
    }

    fun setExpiryReminders(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EXP_REMINDERS, enabled).apply()
        _expiryReminders.value = enabled
    }

    fun setCaregiverNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CG_NOTIF, enabled).apply()
        _caregiverNotifications.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun setAppPin(pin: String) {
        prefs.edit().putString(KEY_APP_PIN, pin).apply()
        _appPin.value = pin
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
        _isOnboardingCompleted.value = false
        _language.value = "en"
        _themeMode.value = "SYSTEM"
        _privateNotifications.value = false
        _medicineReminders.value = true
        _appointmentReminders.value = true
        _expiryReminders.value = true
        _caregiverNotifications.value = true
        _soundEnabled.value = true
        _vibrationEnabled.value = true
        _appPin.value = ""
    }

    companion object {
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME = "app_theme"
        private const val KEY_PRIVATE_NOTIF = "private_notifications"
        private const val KEY_MED_REMINDERS = "medicine_reminders"
        private const val KEY_APT_REMINDERS = "appointment_reminders"
        private const val KEY_EXP_REMINDERS = "expiry_reminders"
        private const val KEY_CG_NOTIF = "caregiver_notifications"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_APP_PIN = "app_pin"
    }
}
