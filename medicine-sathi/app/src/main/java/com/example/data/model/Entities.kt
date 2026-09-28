package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val nickname: String = "",
    val dateOfBirth: String = "",
    val gender: String = "Not specified",
    val bloodGroup: String = "",
    val phoneNumber: String = "",
    val relationship: String = "Other",
    val colorIndex: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val genericName: String = "",
    val medicineType: String = "Tablet",
    val strength: String = "",
    val unit: String = "mg",
    val dosage: String = "1",
    val initialQuantity: Int = 30,
    val remainingQuantity: Int = 30,
    val minQuantityThreshold: Int = 5,
    val startDate: String = "",
    val endDate: String = "",
    val frequency: String = "Once daily",
    val doseTimesJson: String = "08:00",
    val beforeAfterFood: String = "After food",
    val doctorId: Long? = null,
    val familyMemberId: Long,
    val notes: String = "",
    val prescriptionAttachmentPath: String? = null,
    val expiryDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "medication_history")
data class MedicationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val medicineName: String,
    val familyMemberId: Long,
    val scheduledDate: String, // YYYY-MM-DD
    val scheduledTime: String, // HH:mm
    val actionTime: Long = System.currentTimeMillis(),
    val status: String, // TAKEN, MISSED, SKIPPED, SNOOZED
    val notes: String = ""
)

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val specialty: String = "",
    val hospitalClinic: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = ""
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyMemberId: Long,
    val doctorId: Long? = null,
    val doctorName: String = "",
    val hospitalClinic: String = "",
    val appointmentDate: String = "", // YYYY-MM-DD
    val appointmentTime: String = "", // HH:mm
    val reason: String = "",
    val notes: String = "",
    val reminderBeforeMinutes: Int = 60,
    val status: String = "SCHEDULED" // SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED
)

@Entity(tableName = "prescriptions")
data class Prescription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val familyMemberId: Long,
    val doctorId: Long? = null,
    val date: String = "",
    val notes: String = "",
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "caregivers")
data class Caregiver(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relationship: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val notifyMissed: Boolean = true,
    val notifyReminder: Boolean = true,
    val notifyAppointment: Boolean = true,
    val notifyExpiry: Boolean = true
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val category: String, // MEDICINE, APPOINTMENT, EXPIRY, CAREGIVER
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedEntityId: Long? = null
)
