package com.example.data.local.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberById(id: Long): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE familyMemberId = :memberId ORDER BY name ASC")
    fun getMedicinesByMember(memberId: Long): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: Long): Medicine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("UPDATE medicines SET remainingQuantity = :remaining WHERE id = :id")
    suspend fun updateRemainingQuantity(id: Long, remaining: Int)

    @Query("SELECT * FROM medicines WHERE remainingQuantity <= minQuantityThreshold")
    fun getLowInventoryMedicines(): Flow<List<Medicine>>
}

@Dao
interface MedicationHistoryDao {
    @Query("SELECT * FROM medication_history ORDER BY scheduledDate DESC, scheduledTime DESC, actionTime DESC")
    fun getAllHistory(): Flow<List<MedicationHistory>>

    @Query("SELECT * FROM medication_history WHERE scheduledDate = :date")
    fun getHistoryForDate(date: String): Flow<List<MedicationHistory>>

    @Query("SELECT * FROM medication_history WHERE familyMemberId = :memberId ORDER BY scheduledDate DESC")
    fun getHistoryForMember(memberId: Long): Flow<List<MedicationHistory>>

    @Query("SELECT * FROM medication_history WHERE medicineId = :medicineId AND scheduledDate = :date AND scheduledTime = :time LIMIT 1")
    suspend fun getDoseEntry(medicineId: Long, date: String, time: String): MedicationHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordHistory(history: MedicationHistory): Long

    @Update
    suspend fun updateHistory(history: MedicationHistory)

    @Query("DELETE FROM medication_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)
}

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<Doctor>>

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: Long): Doctor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor): Long

    @Update
    suspend fun updateDoctor(doctor: Doctor)

    @Delete
    suspend fun deleteDoctor(doctor: Doctor)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE familyMemberId = :memberId ORDER BY appointmentDate ASC")
    fun getAppointmentsForMember(memberId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE appointmentDate >= :today ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getUpcomingAppointments(today: String): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY date DESC, createdAt DESC")
    fun getAllPrescriptions(): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions WHERE familyMemberId = :memberId ORDER BY date DESC")
    fun getPrescriptionsForMember(memberId: Long): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: Prescription): Long

    @Update
    suspend fun updatePrescription(prescription: Prescription)

    @Delete
    suspend fun deletePrescription(prescription: Prescription)
}

@Dao
interface CaregiverDao {
    @Query("SELECT * FROM caregivers ORDER BY name ASC")
    fun getAllCaregivers(): Flow<List<Caregiver>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaregiver(caregiver: Caregiver): Long

    @Update
    suspend fun updateCaregiver(caregiver: Caregiver)

    @Delete
    suspend fun deleteCaregiver(caregiver: Caregiver)
}

@Dao
interface AppNotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM app_notifications")
    suspend fun clearAllNotifications()

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)
}
