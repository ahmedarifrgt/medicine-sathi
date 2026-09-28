package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MedicineSathiApp
import com.example.data.model.*
import com.example.service.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class TodayDoseItem(
    val medicineId: Long,
    val medicineName: String,
    val genericName: String,
    val memberId: Long,
    val memberName: String,
    val dosage: String,
    val time: String, // e.g. "08:00"
    val beforeAfterFood: String,
    val status: String, // "UPCOMING", "TAKEN", "MISSED", "SKIPPED", "SNOOZED"
    val remainingQuantity: Int,
    val unit: String
)

data class ExpiryAlertItem(
    val medicine: Medicine,
    val status: String, // "EXPIRED", "TODAY", "WITHIN_7_DAYS", "WITHIN_30_DAYS"
    val daysRemaining: Long
)

enum class Screen {
    HOME,
    MEDICINES,
    CALENDAR,
    FAMILY,
    MORE,
    APPOINTMENTS,
    DOCTORS,
    PRESCRIPTIONS,
    CAREGIVERS,
    INVENTORY,
    HISTORY,
    REPORTS,
    SETTINGS,
    NOTIFICATIONS,
    ABOUT_AUTHOR,
    FAMILY_DETAIL,
    SEARCH
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MedicineSathiApp
    private val repo = app.repository
    val prefs = app.preferencesRepository

    // Base flows
    val members: StateFlow<List<FamilyMember>> = repo.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicines: StateFlow<List<Medicine>> = repo.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<MedicationHistory>> = repo.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctors: StateFlow<List<Doctor>> = repo.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repo.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prescriptions: StateFlow<List<Prescription>> = repo.allPrescriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val caregivers: StateFlow<List<Caregiver>> = repo.allCaregivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repo.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repo.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val lowInventoryMedicines: StateFlow<List<Medicine>> = repo.lowInventoryMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedMemberId = MutableStateFlow<Long?>(null)
    val selectedMemberId: StateFlow<Long?> = _selectedMemberId.asStateFlow()

    // Global Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Calendar selected date (YYYY-MM-DD)
    val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val _calendarSelectedDate = MutableStateFlow(todayDateString)
    val calendarSelectedDate: StateFlow<String> = _calendarSelectedDate.asStateFlow()

    // Today's doses derived state
    val todayDoses: StateFlow<List<TodayDoseItem>> = combine(medicines, history, members) { meds, histList, mems ->
        val memberMap = mems.associateBy { it.id }
        val todayHistMap = histList
            .filter { it.scheduledDate == todayDateString }
            .associateBy { "${it.medicineId}_${it.scheduledTime}" }

        val items = mutableListOf<TodayDoseItem>()
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        for (med in meds) {
            val times = med.doseTimesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val member = memberMap[med.familyMemberId]
            val memberName = member?.fullName ?: "Family"

            for (time in times) {
                val key = "${med.id}_$time"
                val histEntry = todayHistMap[key]

                val status = when {
                    histEntry != null -> histEntry.status
                    time < currentTimeStr -> "MISSED"
                    else -> "UPCOMING"
                }

                items.add(
                    TodayDoseItem(
                        medicineId = med.id,
                        medicineName = med.name,
                        genericName = med.genericName,
                        memberId = med.familyMemberId,
                        memberName = memberName,
                        dosage = "${med.dosage} ${med.unit}",
                        time = time,
                        beforeAfterFood = med.beforeAfterFood,
                        status = status,
                        remainingQuantity = med.remainingQuantity,
                        unit = med.unit
                    )
                )
            }
        }
        items.sortedBy { it.time }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expiring medicines
    val expiringMedicines: StateFlow<List<ExpiryAlertItem>> = medicines.map { meds ->
        val list = mutableListOf<ExpiryAlertItem>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        for (med in meds) {
            if (med.expiryDate.isNotEmpty()) {
                try {
                    val expDate = sdf.parse(med.expiryDate)
                    if (expDate != null) {
                        val diffDays = (expDate.time - now) / (1000 * 60 * 60 * 24)
                        val status = when {
                            diffDays < 0 -> "EXPIRED"
                            diffDays == 0L -> "TODAY"
                            diffDays <= 7 -> "WITHIN_7_DAYS"
                            diffDays <= 30 -> "WITHIN_30_DAYS"
                            else -> null
                        }
                        if (status != null) {
                            list.add(ExpiryAlertItem(med, status, diffDays))
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        list.sortedBy { it.daysRemaining }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation actions
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openFamilyDetail(memberId: Long) {
        _selectedMemberId.value = memberId
        _currentScreen.value = Screen.FAMILY_DETAIL
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCalendarDate(date: String) {
        _calendarSelectedDate.value = date
    }

    // Dose actions
    fun markDoseTaken(item: TodayDoseItem) {
        viewModelScope.launch {
            repo.recordDoseAction(
                medicineId = item.medicineId,
                medicineName = item.medicineName,
                memberId = item.memberId,
                scheduledDate = todayDateString,
                scheduledTime = item.time,
                status = "TAKEN"
            )
        }
    }

    fun markDoseSkipped(item: TodayDoseItem) {
        viewModelScope.launch {
            repo.recordDoseAction(
                medicineId = item.medicineId,
                medicineName = item.medicineName,
                memberId = item.memberId,
                scheduledDate = todayDateString,
                scheduledTime = item.time,
                status = "SKIPPED"
            )
        }
    }

    fun snoozeDose(item: TodayDoseItem, minutes: Int) {
        viewModelScope.launch {
            repo.recordDoseAction(
                medicineId = item.medicineId,
                medicineName = item.medicineName,
                memberId = item.memberId,
                scheduledDate = todayDateString,
                scheduledTime = item.time,
                status = "SNOOZED",
                notes = "Snoozed for $minutes minutes"
            )
            AlarmScheduler.scheduleSnoozeReminder(
                context = app,
                medicineId = item.medicineId,
                medicineName = item.medicineName,
                scheduledDate = todayDateString,
                scheduledTime = item.time,
                notificationId = (item.medicineId.toString() + item.time).hashCode(),
                snoozeMinutes = minutes
            )
        }
    }

    // CRUD: Family Member
    fun saveMember(member: FamilyMember) {
        viewModelScope.launch {
            if (member.id == 0L) {
                repo.insertMember(member)
            } else {
                repo.updateMember(member)
            }
        }
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch {
            repo.deleteMember(member)
            if (_selectedMemberId.value == member.id) {
                _currentScreen.value = Screen.FAMILY
            }
        }
    }

    // CRUD: Medicine
    fun saveMedicine(medicine: Medicine) {
        viewModelScope.launch {
            val medId = if (medicine.id == 0L) {
                repo.insertMedicine(medicine)
            } else {
                repo.updateMedicine(medicine)
                medicine.id
            }

            // Schedule alarms for today if reminders are enabled
            if (prefs.medicineReminders.value) {
                val times = medicine.doseTimesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                for (time in times) {
                    AlarmScheduler.scheduleDoseReminder(
                        context = app,
                        medicineId = medId,
                        medicineName = medicine.name,
                        memberName = "Family",
                        scheduledDate = todayDateString,
                        timeString = time
                    )
                }
            }
        }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repo.deleteMedicine(medicine)
        }
    }

    fun updateMedicineQuantity(medicineId: Long, newQuantity: Int) {
        viewModelScope.launch {
            repo.updateRemainingQuantity(medicineId, newQuantity)
        }
    }

    // CRUD: Doctor
    fun saveDoctor(doctor: Doctor) {
        viewModelScope.launch {
            if (doctor.id == 0L) {
                repo.insertDoctor(doctor)
            } else {
                repo.updateDoctor(doctor)
            }
        }
    }

    fun deleteDoctor(doctor: Doctor) {
        viewModelScope.launch {
            repo.deleteDoctor(doctor)
        }
    }

    // CRUD: Appointment
    fun saveAppointment(appointment: Appointment) {
        viewModelScope.launch {
            if (appointment.id == 0L) {
                repo.insertAppointment(appointment)
            } else {
                repo.updateAppointment(appointment)
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repo.deleteAppointment(appointment)
        }
    }

    // CRUD: Prescription
    fun savePrescription(prescription: Prescription) {
        viewModelScope.launch {
            if (prescription.id == 0L) {
                repo.insertPrescription(prescription)
            } else {
                repo.updatePrescription(prescription)
            }
        }
    }

    fun deletePrescription(prescription: Prescription) {
        viewModelScope.launch {
            repo.deletePrescription(prescription)
        }
    }

    // CRUD: Caregiver
    fun saveCaregiver(caregiver: Caregiver) {
        viewModelScope.launch {
            if (caregiver.id == 0L) {
                repo.insertCaregiver(caregiver)
            } else {
                repo.updateCaregiver(caregiver)
            }
        }
    }

    fun deleteCaregiver(caregiver: Caregiver) {
        viewModelScope.launch {
            repo.deleteCaregiver(caregiver)
        }
    }

    // Notifications
    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repo.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repo.markAllNotificationsAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repo.clearAllNotifications()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repo.deleteNotification(id)
        }
    }

    // Clear entire DB
    fun clearEntireDatabase(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.clearEntireDatabase()
            prefs.clearAllData()
            onDone()
        }
    }

    // Backup to JSON string
    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("app", "Medicine Sathi")
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val memArray = JSONArray()
        members.value.forEach {
            val obj = JSONObject().apply {
                put("fullName", it.fullName)
                put("nickname", it.nickname)
                put("dateOfBirth", it.dateOfBirth)
                put("gender", it.gender)
                put("bloodGroup", it.bloodGroup)
                put("phoneNumber", it.phoneNumber)
                put("relationship", it.relationship)
                put("notes", it.notes)
            }
            memArray.put(obj)
        }
        root.put("familyMembers", memArray)

        val medArray = JSONArray()
        medicines.value.forEach {
            val obj = JSONObject().apply {
                put("name", it.name)
                put("genericName", it.genericName)
                put("medicineType", it.medicineType)
                put("strength", it.strength)
                put("unit", it.unit)
                put("dosage", it.dosage)
                put("initialQuantity", it.initialQuantity)
                put("remainingQuantity", it.remainingQuantity)
                put("frequency", it.frequency)
                put("doseTimes", it.doseTimesJson)
                put("beforeAfterFood", it.beforeAfterFood)
                put("expiryDate", it.expiryDate)
                put("notes", it.notes)
            }
            medArray.put(obj)
        }
        root.put("medicines", medArray)

        return root.toString(2)
    }

    // Generate CSV Reports
    fun generateMedicationReportCsv(): String {
        val sb = StringBuilder()
        sb.append("Medicine,Generic,Type,Dosage,Remaining,Expiry Date,Start Date,End Date\n")
        medicines.value.forEach { m ->
            sb.append("\"${m.name}\",\"${m.genericName}\",\"${m.medicineType}\",\"${m.dosage} ${m.unit}\",\"${m.remainingQuantity}\",\"${m.expiryDate}\",\"${m.startDate}\",\"${m.endDate}\"\n")
        }
        return sb.toString()
    }

    fun generateHistoryReportCsv(): String {
        val sb = StringBuilder()
        sb.append("Date,Time,Medicine,Status,Action Time\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        history.value.forEach { h ->
            sb.append("\"${h.scheduledDate}\",\"${h.scheduledTime}\",\"${h.medicineName}\",\"${h.status}\",\"${sdf.format(Date(h.actionTime))}\"\n")
        }
        return sb.toString()
    }
}
