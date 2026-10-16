package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: Int = 1,
    val schoolCode: String,
    val schoolName: String,
    val fullName: String,
    val role: String, // "Student", "Teacher", or "Admin"
    val gradeSection: String,
    val studentIdNumber: String,
    val feesPaid: Boolean = false,
    val isAccountClosed: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val verifiedAt: Long = System.currentTimeMillis(),
    val feeAmount: Int = 40000,
    val feePaidOnline: Boolean = false,
    val feeTransactionId: String? = null,
    val feePaidTimestamp: Long? = null,
    val feePaymentMethod: String? = null
)

@Entity(tableName = "student_access_locks")
data class StudentAccessLockEntity(
    @PrimaryKey val studentCode: String,
    val remainingSeconds: Int = 120,
    val isPermanentlyLocked: Boolean = false,
    val overrideFeesPaid: Boolean = false,
    val oneTimeAccessGrantedCount: Int = 0,
    val feeAmount: Int = 40000,
    val feePaidOnline: Boolean = false,
    val feeTransactionId: String? = null,
    val feePaidTimestamp: Long? = null,
    val feePaymentMethod: String? = null
)

data class FeeReceiptInfo(
    val receiptNumber: String,
    val studentName: String,
    val studentCode: String,
    val gradeSection: String,
    val schoolName: String,
    val amount: Int = 40000,
    val paymentMethod: String,
    val transactionId: String,
    val paymentDate: String,
    val status: String = "PAID & VERIFIED"
)

data class RegisteredStudentAccount(
    val studentCode: String,
    val fullName: String,
    val gradeSection: String,
    val schoolCode: String = "MRSMiraRoadSchool",
    val defaultFeesPaid: Boolean = false,
    val feeAmount: Int = 40000
)

data class RegisteredTeacherAccount(
    val teacherCode: String,
    val fullName: String,
    val department: String = "Faculty • MRS Mira Road School",
    val schoolCode: String = "MRSMiraRoadSchool"
)

data class RegisteredAdminAccount(
    val adminCode: String,
    val fullName: String,
    val designation: String = "Super Administrator • MRS Mira Road School",
    val schoolCode: String = "MRSMiraRoadSchool"
)

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val dateLabel: String,
    val topicCovered: String,
    val summaryNotes: String,
    val homeworkTask: String,
    val teacherName: String,
    val isAcknowledged: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val category: String, // "Urgent", "Academic", "Events", "Sports & Clubs"
    val authorName: String,
    val authorRole: String,
    val attachmentUri: String? = null,
    val attachmentName: String? = null,
    val attachmentType: String? = null, // "IMAGE" or "PDF"
    val attachmentSize: String? = null,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String, // "Mon", "Tue", "Wed", "Thu", "Fri"
    val periodNumber: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val teacherName: String,
    val roomNumber: String,
    val sessionType: String, // "Lecture", "Lab", "Seminar", "Workshop"
    val accentHex: Long = 0xFF1A2E4C
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val description: String,
    val dueDateLabel: String,
    val maxPoints: Int = 100,
    val status: String, // "Pending", "Submitted", "Graded"
    val submissionNote: String = "",
    val attachedUri: String? = null,
    val attachedFileName: String? = null,
    val attachedFileType: String? = null, // "IMAGE" or "PDF"
    val attachedFileSize: String? = null,
    val awardedPoints: Int? = null,
    val teacherFeedback: String = "",
    val teacherName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val participantName: String,
    val participantRole: String,
    val subjectDepartment: String,
    val avatarInitials: String,
    val officeHours: String,
    val unreadCount: Int = 0,
    val lastMessagePreview: String,
    val lastMessageTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threadId: Int,
    val senderName: String,
    val isFromCurrentUser: Boolean,
    val messageText: String,
    val attachmentUri: String? = null,
    val attachmentName: String? = null,
    val attachmentType: String? = null, // "IMAGE" or "PDF"
    val attachmentSize: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class VerifiedSchoolInfo(
    val code: String,
    val name: String,
    val campusBadge: String,
    val motto: String
)
