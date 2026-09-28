package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class MedicineRepository(private val database: AppDatabase) {

    // Family Members
    val allMembers: Flow<List<FamilyMember>> = database.familyMemberDao().getAllMembers()
    suspend fun getMemberById(id: Long): FamilyMember? = database.familyMemberDao().getMemberById(id)
    suspend fun insertMember(member: FamilyMember): Long = database.familyMemberDao().insertMember(member)
    suspend fun updateMember(member: FamilyMember) = database.familyMemberDao().updateMember(member)
    suspend fun deleteMember(member: FamilyMember) = database.familyMemberDao().deleteMember(member)

    // Medicines
    val allMedicines: Flow<List<Medicine>> = database.medicineDao().getAllMedicines()
    fun getMedicinesByMember(memberId: Long): Flow<List<Medicine>> = database.medicineDao().getMedicinesByMember(memberId)
    suspend fun getMedicineById(id: Long): Medicine? = database.medicineDao().getMedicineById(id)
    suspend fun insertMedicine(medicine: Medicine): Long = database.medicineDao().insertMedicine(medicine)
    suspend fun updateMedicine(medicine: Medicine) = database.medicineDao().updateMedicine(medicine)
    suspend fun deleteMedicine(medicine: Medicine) = database.medicineDao().deleteMedicine(medicine)
    suspend fun updateRemainingQuantity(id: Long, qty: Int) = database.medicineDao().updateRemainingQuantity(id, qty)
    val lowInventoryMedicines: Flow<List<Medicine>> = database.medicineDao().getLowInventoryMedicines()

    // History
    val allHistory: Flow<List<MedicationHistory>> = database.medicationHistoryDao().getAllHistory()
    fun getHistoryForDate(date: String): Flow<List<MedicationHistory>> = database.medicationHistoryDao().getHistoryForDate(date)
    fun getHistoryForMember(memberId: Long): Flow<List<MedicationHistory>> = database.medicationHistoryDao().getHistoryForMember(memberId)

    suspend fun recordDoseAction(
        medicineId: Long,
        medicineName: String,
        memberId: Long,
        scheduledDate: String,
        scheduledTime: String,
        status: String,
        notes: String = ""
    ) {
        val entry = MedicationHistory(
            medicineId = medicineId,
            medicineName = medicineName,
            familyMemberId = memberId,
            scheduledDate = scheduledDate,
            scheduledTime = scheduledTime,
            actionTime = System.currentTimeMillis(),
            status = status,
            notes = notes
        )
        database.medicationHistoryDao().recordHistory(entry)

        // If taken, decrement medicine quantity if > 0
        if (status.equals("TAKEN", ignoreCase = true)) {
            val med = database.medicineDao().getMedicineById(medicineId)
            if (med != null && med.remainingQuantity > 0) {
                val newQty = (med.remainingQuantity - 1).coerceAtLeast(0)
                database.medicineDao().updateRemainingQuantity(medicineId, newQty)
                if (newQty <= med.minQuantityThreshold) {
                    // Create notification for low stock
                    database.appNotificationDao().insertNotification(
                        AppNotification(
                            title = "Low Inventory: ${med.name}",
                            message = "${med.name} is running low ($newQty ${med.unit} left). Please restock soon.",
                            category = "MEDICINE",
                            relatedEntityId = med.id
                        )
                    )
                }
            }
        }
    }

    // Doctors
    val allDoctors: Flow<List<Doctor>> = database.doctorDao().getAllDoctors()
    suspend fun getDoctorById(id: Long): Doctor? = database.doctorDao().getDoctorById(id)
    suspend fun insertDoctor(doctor: Doctor): Long = database.doctorDao().insertDoctor(doctor)
    suspend fun updateDoctor(doctor: Doctor) = database.doctorDao().updateDoctor(doctor)
    suspend fun deleteDoctor(doctor: Doctor) = database.doctorDao().deleteDoctor(doctor)

    // Appointments
    val allAppointments: Flow<List<Appointment>> = database.appointmentDao().getAllAppointments()
    fun getAppointmentsForMember(memberId: Long): Flow<List<Appointment>> = database.appointmentDao().getAppointmentsForMember(memberId)
    fun getUpcomingAppointments(): Flow<List<Appointment>> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return database.appointmentDao().getUpcomingAppointments(todayStr)
    }
    suspend fun insertAppointment(appointment: Appointment): Long = database.appointmentDao().insertAppointment(appointment)
    suspend fun updateAppointment(appointment: Appointment) = database.appointmentDao().updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: Appointment) = database.appointmentDao().deleteAppointment(appointment)

    // Prescriptions
    val allPrescriptions: Flow<List<Prescription>> = database.prescriptionDao().getAllPrescriptions()
    fun getPrescriptionsForMember(memberId: Long): Flow<List<Prescription>> = database.prescriptionDao().getPrescriptionsForMember(memberId)
    suspend fun insertPrescription(prescription: Prescription): Long = database.prescriptionDao().insertPrescription(prescription)
    suspend fun updatePrescription(prescription: Prescription) = database.prescriptionDao().updatePrescription(prescription)
    suspend fun deletePrescription(prescription: Prescription) = database.prescriptionDao().deletePrescription(prescription)

    // Caregivers
    val allCaregivers: Flow<List<Caregiver>> = database.caregiverDao().getAllCaregivers()
    suspend fun insertCaregiver(caregiver: Caregiver): Long = database.caregiverDao().insertCaregiver(caregiver)
    suspend fun updateCaregiver(caregiver: Caregiver) = database.caregiverDao().updateCaregiver(caregiver)
    suspend fun deleteCaregiver(caregiver: Caregiver) = database.caregiverDao().deleteCaregiver(caregiver)

    // Notifications
    val allNotifications: Flow<List<AppNotification>> = database.appNotificationDao().getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = database.appNotificationDao().getUnreadCount()
    suspend fun insertNotification(notification: AppNotification): Long = database.appNotificationDao().insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = database.appNotificationDao().markAsRead(id)
    suspend fun markAllNotificationsAsRead() = database.appNotificationDao().markAllAsRead()
    suspend fun clearAllNotifications() = database.appNotificationDao().clearAllNotifications()
    suspend fun deleteNotification(id: Long) = database.appNotificationDao().deleteNotification(id)

    // Clear everything (Reset database)
    suspend fun clearEntireDatabase() {
        database.clearAllTables()
    }
}
