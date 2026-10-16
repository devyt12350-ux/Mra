package com.example.data.repository

import com.example.data.local.AnnouncementEntity
import com.example.data.local.AssignmentEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatThreadEntity
import com.example.data.local.DiaryEntryEntity
import com.example.data.local.FeeReceiptInfo
import com.example.data.local.RegisteredAdminAccount
import com.example.data.local.RegisteredStudentAccount
import com.example.data.local.RegisteredTeacherAccount
import com.example.data.local.ScholarDao
import com.example.data.local.StudentAccessLockEntity
import com.example.data.local.TimetableSlotEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.VerifiedSchoolInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScholarRepository(private val dao: ScholarDao) {

    val activeAccount: Flow<UserAccountEntity?> = dao.observeActiveAccount()
    val diaryEntries: Flow<List<DiaryEntryEntity>> = dao.observeDiaryEntries()
    val announcements: Flow<List<AnnouncementEntity>> = dao.observeAnnouncements()
    val timetableSlots: Flow<List<TimetableSlotEntity>> = dao.observeTimetableSlots()
    val assignments: Flow<List<AssignmentEntity>> = dao.observeAssignments()
    val chatThreads: Flow<List<ChatThreadEntity>> = dao.observeChatThreads()
    val allStudentLocks: Flow<List<StudentAccessLockEntity>> = dao.observeAllStudentLocks()

    fun observeMessages(threadId: Int): Flow<List<ChatMessageEntity>> =
        dao.observeMessagesForThread(threadId)

    val officialSchools = listOf(
        VerifiedSchoolInfo(
            code = "MRSMiraRoadSchool",
            name = "MRS Mira Road School",
            campusBadge = "Mira Road Campus • Official Student & Faculty Portal",
            motto = "Knowledge, Discipline & Excellence"
        )
    )

    val registeredStudents = listOf(
        RegisteredStudentAccount(
            studentCode = "MRS53SY202",
            fullName = "Zayan Khan",
            gradeSection = "Grade 10 • Section A",
            schoolCode = "MRSMiraRoadSchool",
            defaultFeesPaid = true
        ),
        RegisteredStudentAccount(
            studentCode = "MRS548272S",
            fullName = "Reyansh Verma",
            gradeSection = "Grade 10 • Section B",
            schoolCode = "MRSMiraRoadSchool",
            defaultFeesPaid = false
        ),
        RegisteredStudentAccount(
            studentCode = "MRS00BH212",
            fullName = "Aarav Sharma",
            gradeSection = "Grade 9 • Section A",
            schoolCode = "MRSMiraRoadSchool",
            defaultFeesPaid = true
        )
    )

    val registeredTeachers = listOf(
        RegisteredTeacherAccount(
            teacherCode = "26MRS29003",
            fullName = "DEV ANIL PRAJAPATI",
            department = "Faculty • MRS Mira Road School",
            schoolCode = "MRSMiraRoadSchool"
        )
    )

    val registeredAdmins = listOf(
        RegisteredAdminAccount(
            adminCode = "MRSADMIN01",
            fullName = "Dr. Rajeshwar Sharma",
            designation = "Campus Super Administrator • MRS Mira Road School",
            schoolCode = "MRSMiraRoadSchool"
        ),
        RegisteredAdminAccount(
            adminCode = "ADMIN01",
            fullName = "Administrator Office",
            designation = "Principal System Administrator • MRS Mira Road School",
            schoolCode = "MRSMiraRoadSchool"
        )
    )

    fun findStudentByCode(rawCode: String): RegisteredStudentAccount? {
        val cleaned = rawCode.trim()
        return registeredStudents.find { it.studentCode.equals(cleaned, ignoreCase = true) }
    }

    fun findTeacherByCode(rawCode: String): RegisteredTeacherAccount? {
        val cleaned = rawCode.trim()
        return registeredTeachers.find { it.teacherCode.equals(cleaned, ignoreCase = true) }
    }

    fun findAdminByCode(rawCode: String): RegisteredAdminAccount? {
        val cleaned = rawCode.trim()
        return registeredAdmins.find { it.adminCode.equals(cleaned, ignoreCase = true) }
    }

    fun validateSchoolCode(rawCode: String): VerifiedSchoolInfo? {
        val trimmed = rawCode.trim()
        officialSchools.find { it.code.equals(trimmed, ignoreCase = true) }?.let { return it }
        if (trimmed.replace(" ", "").equals("MRSMiraRoadSchool", ignoreCase = true)) {
            return officialSchools.first()
        }
        return null
    }

    suspend fun getOrCreateStudentLock(studentCode: String): StudentAccessLockEntity {
        val key = studentCode.trim().uppercase()
        val existing = dao.getStudentAccessLock(key)
        if (existing != null) {
            return existing
        }
        val student = findStudentByCode(key)
        val defaultPaid = student?.defaultFeesPaid ?: false
        val created = StudentAccessLockEntity(
            studentCode = key,
            remainingSeconds = 120,
            isPermanentlyLocked = false,
            overrideFeesPaid = defaultPaid,
            feeAmount = 40000,
            feePaidOnline = false
        )
        dao.saveStudentAccessLock(created)
        return created
    }

    suspend fun updateStudentLock(studentCode: String, remainingSeconds: Int, isPermanentlyLocked: Boolean) {
        val key = studentCode.trim().uppercase()
        val existing = dao.getStudentAccessLock(key)
        dao.saveStudentAccessLock(
            StudentAccessLockEntity(
                studentCode = key,
                remainingSeconds = remainingSeconds.coerceAtLeast(0),
                isPermanentlyLocked = isPermanentlyLocked,
                overrideFeesPaid = existing?.overrideFeesPaid ?: false,
                oneTimeAccessGrantedCount = existing?.oneTimeAccessGrantedCount ?: 0,
                feeAmount = existing?.feeAmount ?: 40000,
                feePaidOnline = existing?.feePaidOnline ?: false,
                feeTransactionId = existing?.feeTransactionId,
                feePaidTimestamp = existing?.feePaidTimestamp,
                feePaymentMethod = existing?.feePaymentMethod
            )
        )
    }

    suspend fun grantStudentOneTimeAccess(studentCode: String, seconds: Int = 120) {
        val key = studentCode.trim().uppercase()
        val existing = dao.getStudentAccessLock(key)
        val newLock = StudentAccessLockEntity(
            studentCode = key,
            remainingSeconds = seconds,
            isPermanentlyLocked = false,
            overrideFeesPaid = existing?.overrideFeesPaid ?: false,
            oneTimeAccessGrantedCount = (existing?.oneTimeAccessGrantedCount ?: 0) + 1,
            feeAmount = existing?.feeAmount ?: 40000,
            feePaidOnline = existing?.feePaidOnline ?: false,
            feeTransactionId = existing?.feeTransactionId,
            feePaidTimestamp = existing?.feePaidTimestamp,
            feePaymentMethod = existing?.feePaymentMethod
        )
        dao.saveStudentAccessLock(newLock)
    }

    suspend fun closeStudentAccess(studentCode: String) {
        val key = studentCode.trim().uppercase()
        val existing = dao.getStudentAccessLock(key)
        val newLock = StudentAccessLockEntity(
            studentCode = key,
            remainingSeconds = 0,
            isPermanentlyLocked = true,
            overrideFeesPaid = false,
            oneTimeAccessGrantedCount = existing?.oneTimeAccessGrantedCount ?: 0,
            feeAmount = existing?.feeAmount ?: 40000,
            feePaidOnline = false,
            feeTransactionId = null,
            feePaidTimestamp = null,
            feePaymentMethod = null
        )
        dao.saveStudentAccessLock(newLock)
    }

    suspend fun openStudentFullAccess(studentCode: String) {
        val key = studentCode.trim().uppercase()
        val existing = dao.getStudentAccessLock(key)
        val newLock = StudentAccessLockEntity(
            studentCode = key,
            remainingSeconds = 999999,
            isPermanentlyLocked = false,
            overrideFeesPaid = true,
            oneTimeAccessGrantedCount = (existing?.oneTimeAccessGrantedCount ?: 0) + 1,
            feeAmount = existing?.feeAmount ?: 40000,
            feePaidOnline = existing?.feePaidOnline ?: false,
            feeTransactionId = existing?.feeTransactionId,
            feePaidTimestamp = existing?.feePaidTimestamp,
            feePaymentMethod = existing?.feePaymentMethod
        )
        dao.saveStudentAccessLock(newLock)
    }

    suspend fun payStudentFeesFromApp(
        studentCode: String,
        amount: Int = 40000,
        paymentMethod: String = "UPI (Online App Payment)"
    ): FeeReceiptInfo {
        val key = studentCode.trim().uppercase()
        val existing = dao.getStudentAccessLock(key) ?: getOrCreateStudentLock(key)
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val dateString = dateFormat.format(Date(now))
        val txnId = "MRS-FEE-" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(now)) + "-" + (100000..999999).random()
        val receiptNumber = "REC-MRS-2026-" + (10000..99999).random()

        val updatedLock = existing.copy(
            remainingSeconds = 999999,
            isPermanentlyLocked = false,
            overrideFeesPaid = true,
            feeAmount = amount,
            feePaidOnline = true,
            feeTransactionId = txnId,
            feePaidTimestamp = now,
            feePaymentMethod = paymentMethod
        )
        dao.saveStudentAccessLock(updatedLock)

        // If the active user matches this student, update account in DB immediately
        val currentActive = dao.observeActiveAccount().firstOrNull()
        if (currentActive != null && currentActive.studentIdNumber.equals(key, ignoreCase = true)) {
            dao.saveUserAccount(
                currentActive.copy(
                    feesPaid = true,
                    isAccountClosed = false,
                    feeAmount = amount,
                    feePaidOnline = true,
                    feeTransactionId = txnId,
                    feePaidTimestamp = now,
                    feePaymentMethod = paymentMethod
                )
            )
        }

        val studentInfo = findStudentByCode(key)
        return FeeReceiptInfo(
            receiptNumber = receiptNumber,
            studentName = studentInfo?.fullName ?: currentActive?.fullName ?: key,
            studentCode = key,
            gradeSection = studentInfo?.gradeSection ?: currentActive?.gradeSection ?: "Grade 10",
            schoolName = "MRS Mira Road School",
            amount = amount,
            paymentMethod = paymentMethod,
            transactionId = txnId,
            paymentDate = dateString,
            status = "PAID & VERIFIED"
        )
    }

    suspend fun setStudentFeesPaid(studentCode: String, feesPaid: Boolean, amount: Int = 40000) {
        val key = studentCode.trim().uppercase()
        val existing = dao.getStudentAccessLock(key) ?: getOrCreateStudentLock(key)
        val newLock = existing.copy(
            overrideFeesPaid = feesPaid,
            isPermanentlyLocked = if (feesPaid) false else existing.isPermanentlyLocked,
            feeAmount = amount,
            feePaidOnline = if (feesPaid) existing.feePaidOnline else false,
            feeTransactionId = if (feesPaid) (existing.feeTransactionId ?: ("ADMIN-CLR-" + System.currentTimeMillis() % 100000)) else null,
            feePaidTimestamp = if (feesPaid) (existing.feePaidTimestamp ?: System.currentTimeMillis()) else null
        )
        dao.saveStudentAccessLock(newLock)

        val currentActive = dao.observeActiveAccount().firstOrNull()
        if (currentActive != null && currentActive.studentIdNumber.equals(key, ignoreCase = true)) {
            dao.saveUserAccount(
                currentActive.copy(
                    feesPaid = feesPaid,
                    isAccountClosed = !feesPaid && currentActive.isAccountClosed,
                    feeAmount = amount,
                    feePaidOnline = newLock.feePaidOnline,
                    feeTransactionId = newLock.feeTransactionId,
                    feePaidTimestamp = newLock.feePaidTimestamp
                )
            )
        }
    }

    suspend fun closeAllStudents() {
        for (student in registeredStudents) {
            closeStudentAccess(student.studentCode)
        }
    }

    suspend fun openAllStudentsOneTime(seconds: Int = 120) {
        for (student in registeredStudents) {
            grantStudentOneTimeAccess(student.studentCode, seconds)
        }
    }

    suspend fun openAllStudentsFull() {
        for (student in registeredStudents) {
            openStudentFullAccess(student.studentCode)
        }
    }

    suspend fun deleteDiaryEntry(id: Int) = dao.deleteDiaryEntry(id)
    suspend fun deleteAnnouncement(id: Int) = dao.deleteAnnouncement(id)
    suspend fun deleteAssignment(id: Int) = dao.deleteAssignment(id)

    suspend fun saveAccountAndSeedIfNeeded(account: UserAccountEntity) {
        dao.saveUserAccount(account)
        if (dao.getDiaryCount() == 0) {
            seedInitialSchoolData()
        }
    }

    suspend fun ensureSeeded() {
        if (dao.getDiaryCount() == 0) {
            seedInitialSchoolData()
        }
    }

    suspend fun updateAccount(account: UserAccountEntity) {
        dao.saveUserAccount(account)
    }

    suspend fun signOutAccount() {
        dao.clearUserAccount()
    }

    suspend fun addDiaryEntry(entry: DiaryEntryEntity) {
        dao.insertDiaryEntry(entry)
    }

    suspend fun toggleDiaryAcknowledged(entry: DiaryEntryEntity) {
        dao.updateDiaryEntry(entry.copy(isAcknowledged = !entry.isAcknowledged))
    }

    suspend fun addAnnouncement(announcement: AnnouncementEntity) {
        dao.insertAnnouncement(announcement)
    }

    suspend fun toggleAnnouncementPin(announcement: AnnouncementEntity) {
        dao.updateAnnouncement(announcement.copy(isPinned = !announcement.isPinned))
    }

    suspend fun addTimetableSlot(slot: TimetableSlotEntity) {
        dao.insertTimetableSlot(slot)
    }

    suspend fun deleteTimetableSlot(slotId: Int) {
        dao.deleteTimetableSlot(slotId)
    }

    suspend fun addAssignment(assignment: AssignmentEntity) {
        dao.insertAssignment(assignment)
    }

    suspend fun submitAssignment(
        assignment: AssignmentEntity,
        submissionNote: String,
        attachedUri: String? = null,
        attachedFileName: String? = null,
        attachedFileType: String? = null,
        attachedFileSize: String? = null
    ) {
        dao.updateAssignment(
            assignment.copy(
                status = "Submitted",
                submissionNote = submissionNote,
                attachedUri = attachedUri ?: assignment.attachedUri,
                attachedFileName = attachedFileName ?: assignment.attachedFileName,
                attachedFileType = attachedFileType ?: assignment.attachedFileType,
                attachedFileSize = attachedFileSize ?: assignment.attachedFileSize,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun gradeAssignment(
        assignment: AssignmentEntity,
        awardedPoints: Int,
        feedback: String
    ) {
        dao.updateAssignment(
            assignment.copy(
                status = "Graded",
                awardedPoints = awardedPoints,
                teacherFeedback = feedback,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun sendMessage(
        threadId: Int,
        senderName: String,
        isFromCurrentUser: Boolean,
        messageText: String,
        attachmentUri: String? = null,
        attachmentName: String? = null,
        attachmentType: String? = null,
        attachmentSize: String? = null
    ) {
        val now = System.currentTimeMillis()
        dao.insertChatMessage(
            ChatMessageEntity(
                threadId = threadId,
                senderName = senderName,
                isFromCurrentUser = isFromCurrentUser,
                messageText = messageText,
                attachmentUri = attachmentUri,
                attachmentName = attachmentName,
                attachmentType = attachmentType,
                attachmentSize = attachmentSize,
                timestamp = now
            )
        )
        dao.getChatThreadById(threadId)?.let { thread ->
            val updatedUnread = if (isFromCurrentUser) 0 else thread.unreadCount + 1
            dao.updateChatThread(
                thread.copy(
                    lastMessagePreview = if (attachmentName != null) "📎 $attachmentName: $messageText" else messageText,
                    lastMessageTime = now,
                    unreadCount = updatedUnread
                )
            )
        }
    }

    suspend fun markThreadRead(threadId: Int) {
        dao.getChatThreadById(threadId)?.let { thread ->
            if (thread.unreadCount > 0) {
                dao.updateChatThread(thread.copy(unreadCount = 0))
            }
        }
    }

    suspend fun createNewThread(
        participantName: String,
        participantRole: String,
        subjectDepartment: String,
        officeHours: String,
        initialMessage: String,
        senderName: String
    ): Int {
        val initials = participantName
            .split(" ")
            .filter { it.isNotBlank() && !it.endsWith(".") }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "TR" }
        val now = System.currentTimeMillis()
        val newId = dao.insertChatThread(
            ChatThreadEntity(
                participantName = participantName,
                participantRole = participantRole,
                subjectDepartment = subjectDepartment,
                avatarInitials = initials,
                officeHours = officeHours,
                unreadCount = 0,
                lastMessagePreview = initialMessage,
                lastMessageTime = now
            )
        ).toInt()
        dao.insertChatMessage(
            ChatMessageEntity(
                threadId = newId,
                senderName = senderName,
                isFromCurrentUser = true,
                messageText = initialMessage,
                timestamp = now
            )
        )
        return newId
    }

    private suspend fun seedInitialSchoolData() {
        val now = System.currentTimeMillis()
        dao.insertDiaryEntries(
            listOf(
                DiaryEntryEntity(
                    subject = "Mathematics",
                    dateLabel = "Today • Oct 7",
                    topicCovered = "Quadratic Functions & Vertex Form Transformations",
                    summaryNotes = "Derived vertex form f(x) = a(x - h)² + k from standard form using completing the square. Analyzed axis of symmetry and parabola opening direction.",
                    homeworkTask = "Complete Exercise 4B (Problems 1–12) on page 118 and sketch 3 parabola graphs.",
                    teacherName = "DEV ANIL PRAJAPATI",
                    isAcknowledged = false,
                    timestamp = now - 3600_000L
                ),
                DiaryEntryEntity(
                    subject = "Physics",
                    dateLabel = "Today • Oct 7",
                    topicCovered = "Newton's Second Law & Inclined Plane Free-Body Diagrams",
                    summaryNotes = "Resolved gravitational force vectors into parallel (mg sin θ) and perpendicular (mg cos θ) components with kinetic friction.",
                    homeworkTask = "Prepare Lab Report #3 data table on friction coefficients before Thursday's lab.",
                    teacherName = "Prof. Marcus Thorne",
                    isAcknowledged = true,
                    timestamp = now - 7200_000L
                ),
                DiaryEntryEntity(
                    subject = "Literature",
                    dateLabel = "Yesterday • Oct 6",
                    topicCovered = "Rhetorical Devices in Shakespeare's Julius Caesar (Act III)",
                    summaryNotes = "Compared Brutus's prose appeal to stoic reason with Mark Antony's iambic pentameter appeal to emotion and irony.",
                    homeworkTask = "Annotate lines 74–108 of Act III Scene 2 and write a 200-word reflection.",
                    teacherName = "Ms. Clara Lin",
                    isAcknowledged = true,
                    timestamp = now - 86_400_000L
                ),
                DiaryEntryEntity(
                    subject = "Computer Science",
                    dateLabel = "Oct 5",
                    topicCovered = "Binary Search Trees & Recursive Traversal Algorithms",
                    summaryNotes = "Implemented In-Order, Pre-Order, and Post-Order tree traversals and analyzed O(log n) average lookup complexity.",
                    homeworkTask = "Submit dry-run trace diagram for inserting [42, 18, 65, 12, 30, 77] into an empty BST.",
                    teacherName = "Mr. Devon Kapoor",
                    isAcknowledged = false,
                    timestamp = now - 172_800_000L
                )
            )
        )

        dao.insertAnnouncements(
            listOf(
                AnnouncementEntity(
                    title = "Mid-Term Science & Robotics Symposium Schedule",
                    body = "All Grade 9–12 project teams at MRS Mira Road School must set up their display boards in the Grand Auditorium by 8:15 AM this Friday.",
                    category = "Urgent",
                    authorName = "Principal Arthur Pendelton",
                    authorRole = "School Administration",
                    attachmentName = "Symposium_Floor_Plan_2026.pdf",
                    attachmentType = "PDF",
                    attachmentSize = "480 KB",
                    isPinned = true,
                    timestamp = now - 1800_000L
                ),
                AnnouncementEntity(
                    title = "Updated Library Extended Study Hours for October",
                    body = "The Central Digital & Reference Library will remain open until 6:30 PM Monday through Thursday to support students preparing for mid-semester assessments.",
                    category = "Academic",
                    authorName = "Mrs. Evelyn Okafor",
                    authorRole = "Head Librarian",
                    isPinned = true,
                    timestamp = now - 14_400_000L
                ),
                AnnouncementEntity(
                    title = "Varsity Track & Field Tryouts — North Stadium",
                    body = "Sprint, relay, and long-jump trials will take place on Wednesday from 3:45 PM to 5:15 PM. Bring hydration and proper track footwear.",
                    category = "Sports & Clubs",
                    authorName = "Coach Julian Alvarez",
                    authorRole = "Athletics Director",
                    isPinned = false,
                    timestamp = now - 140_000_000L
                )
            )
        )

        dao.insertTimetableSlots(
            listOf(
                // Monday
                TimetableSlotEntity(dayOfWeek = "Mon", periodNumber = 1, startTime = "08:30 AM", endTime = "09:25 AM", subject = "Mathematics", teacherName = "DEV ANIL PRAJAPATI", roomNumber = "Room 302", sessionType = "Lecture", accentHex = 0xFF1A2E4C),
                TimetableSlotEntity(dayOfWeek = "Mon", periodNumber = 2, startTime = "09:30 AM", endTime = "10:25 AM", subject = "Physics", teacherName = "Prof. Marcus Thorne", roomNumber = "Physics Lab B", sessionType = "Lab", accentHex = 0xFF1F6E60),
                TimetableSlotEntity(dayOfWeek = "Mon", periodNumber = 3, startTime = "10:45 AM", endTime = "11:40 AM", subject = "Literature", teacherName = "Ms. Clara Lin", roomNumber = "Humanities 104", sessionType = "Seminar", accentHex = 0xFFB86814),
                TimetableSlotEntity(dayOfWeek = "Mon", periodNumber = 4, startTime = "11:45 AM", endTime = "12:40 PM", subject = "Computer Science", teacherName = "Mr. Devon Kapoor", roomNumber = "Innovation Lab 2", sessionType = "Workshop", accentHex = 0xFF5B3E96),
                TimetableSlotEntity(dayOfWeek = "Mon", periodNumber = 5, startTime = "01:30 PM", endTime = "02:25 PM", subject = "World History", teacherName = "Dr. Samuel Bellamy", roomNumber = "Room 210", sessionType = "Lecture", accentHex = 0xFF9E3A34),
                // Tuesday
                TimetableSlotEntity(dayOfWeek = "Tue", periodNumber = 1, startTime = "08:30 AM", endTime = "09:25 AM", subject = "Chemistry", teacherName = "Dr. Priya Nair", roomNumber = "Chem Lab A", sessionType = "Lab", accentHex = 0xFF1F6E60),
                TimetableSlotEntity(dayOfWeek = "Tue", periodNumber = 2, startTime = "09:30 AM", endTime = "10:25 AM", subject = "Mathematics", teacherName = "DEV ANIL PRAJAPATI", roomNumber = "Room 302", sessionType = "Lecture", accentHex = 0xFF1A2E4C),
                TimetableSlotEntity(dayOfWeek = "Tue", periodNumber = 3, startTime = "10:45 AM", endTime = "11:40 AM", subject = "Computer Science", teacherName = "Mr. Devon Kapoor", roomNumber = "Innovation Lab 2", sessionType = "Workshop", accentHex = 0xFF5B3E96),
                TimetableSlotEntity(dayOfWeek = "Tue", periodNumber = 4, startTime = "11:45 AM", endTime = "12:40 PM", subject = "Literature", teacherName = "Ms. Clara Lin", roomNumber = "Humanities 104", sessionType = "Seminar", accentHex = 0xFFB86814),
                // Wednesday
                TimetableSlotEntity(dayOfWeek = "Wed", periodNumber = 1, startTime = "08:30 AM", endTime = "09:25 AM", subject = "Physics", teacherName = "Prof. Marcus Thorne", roomNumber = "Room 308", sessionType = "Lecture", accentHex = 0xFF1F6E60),
                TimetableSlotEntity(dayOfWeek = "Wed", periodNumber = 2, startTime = "09:30 AM", endTime = "10:25 AM", subject = "World History", teacherName = "Dr. Samuel Bellamy", roomNumber = "Room 210", sessionType = "Seminar", accentHex = 0xFF9E3A34),
                TimetableSlotEntity(dayOfWeek = "Wed", periodNumber = 3, startTime = "10:45 AM", endTime = "11:40 AM", subject = "Mathematics", teacherName = "DEV ANIL PRAJAPATI", roomNumber = "Room 302", sessionType = "Workshop", accentHex = 0xFF1A2E4C),
                TimetableSlotEntity(dayOfWeek = "Wed", periodNumber = 4, startTime = "01:30 PM", endTime = "02:25 PM", subject = "Chemistry", teacherName = "Dr. Priya Nair", roomNumber = "Room 205", sessionType = "Lecture", accentHex = 0xFF1F6E60),
                // Thursday
                TimetableSlotEntity(dayOfWeek = "Thu", periodNumber = 1, startTime = "08:30 AM", endTime = "09:25 AM", subject = "Computer Science", teacherName = "Mr. Devon Kapoor", roomNumber = "Innovation Lab 2", sessionType = "Lab", accentHex = 0xFF5B3E96),
                TimetableSlotEntity(dayOfWeek = "Thu", periodNumber = 2, startTime = "09:30 AM", endTime = "10:25 AM", subject = "Literature", teacherName = "Ms. Clara Lin", roomNumber = "Humanities 104", sessionType = "Lecture", accentHex = 0xFFB86814),
                TimetableSlotEntity(dayOfWeek = "Thu", periodNumber = 3, startTime = "10:45 AM", endTime = "11:40 AM", subject = "Physics", teacherName = "Prof. Marcus Thorne", roomNumber = "Physics Lab B", sessionType = "Lab", accentHex = 0xFF1F6E60),
                // Friday
                TimetableSlotEntity(dayOfWeek = "Fri", periodNumber = 1, startTime = "08:30 AM", endTime = "09:25 AM", subject = "Mathematics", teacherName = "DEV ANIL PRAJAPATI", roomNumber = "Room 302", sessionType = "Seminar", accentHex = 0xFF1A2E4C),
                TimetableSlotEntity(dayOfWeek = "Fri", periodNumber = 2, startTime = "09:30 AM", endTime = "10:25 AM", subject = "World History", teacherName = "Dr. Samuel Bellamy", roomNumber = "Auditorium B", sessionType = "Seminar", accentHex = 0xFF9E3A34),
                TimetableSlotEntity(dayOfWeek = "Fri", periodNumber = 3, startTime = "10:45 AM", endTime = "12:15 PM", subject = "STEM Symposium", teacherName = "All Faculty", roomNumber = "Grand Hall", sessionType = "Workshop", accentHex = 0xFFB86814)
            )
        )

        dao.insertAssignments(
            listOf(
                AssignmentEntity(
                    title = "Quadratic Optimization & Parabola Modeling Set",
                    subject = "Mathematics",
                    description = "Solve the 5 real-world projectile trajectory and bridge arch problems using vertex form equations. Show all algebraic steps and attach coordinate sketches.",
                    dueDateLabel = "Due Tomorrow, 11:59 PM",
                    maxPoints = 100,
                    status = "Pending",
                    teacherName = "DEV ANIL PRAJAPATI",
                    timestamp = now - 3000_000L
                ),
                AssignmentEntity(
                    title = "Inclined Plane & Kinetic Friction Lab Report",
                    subject = "Physics",
                    description = "Analyze experimental force sensor measurements for wooden and aluminum blocks at 15°, 30°, and 45° inclines. Compute percentage error against theoretical μk.",
                    dueDateLabel = "Due Oct 10, 5:00 PM",
                    maxPoints = 50,
                    status = "Pending",
                    teacherName = "Prof. Marcus Thorne",
                    timestamp = now - 10_000_000L
                ),
                AssignmentEntity(
                    title = "Rhetorical Analysis Essay: Act III Orations",
                    subject = "Literature",
                    description = "Write a 600-word comparative analysis examining how ethos, logos, and pathos shift crowd allegiance in Julius Caesar Act III Scene 2.",
                    dueDateLabel = "Submitted Oct 6",
                    maxPoints = 100,
                    status = "Submitted",
                    submissionNote = "Uploaded final essay draft with annotated citations from Folger edition lines 74-145.",
                    attachedFileName = "Julius_Caesar_ActIII_Rhetorical_Analysis.pdf",
                    attachedFileType = "PDF",
                    attachedFileSize = "340 KB",
                    teacherName = "Ms. Clara Lin",
                    timestamp = now - 50_000_000L
                ),
                AssignmentEntity(
                    title = "Binary Search Tree Balancing & Complexity Lab",
                    subject = "Computer Science",
                    description = "Implement recursive insertion, search, and height calculation in Kotlin, and compare worst-case skewed trees with balanced AVL rotations.",
                    dueDateLabel = "Graded Oct 4",
                    maxPoints = 100,
                    status = "Graded",
                    submissionNote = "Submitted Kotlin BST implementation with unit tests and time-complexity analysis.",
                    attachedFileName = "BST_Tree_Balance_Diagram.png",
                    attachedFileType = "IMAGE",
                    attachedFileSize = "420 KB",
                    awardedPoints = 96,
                    teacherFeedback = "Outstanding recursive implementation and clear Big-O derivation! Consider handling duplicate keys explicitly.",
                    teacherName = "Mr. Devon Kapoor",
                    timestamp = now - 120_000_000L
                )
            )
        )

        val t1Id = dao.insertChatThread(
            ChatThreadEntity(
                participantName = "DEV ANIL PRAJAPATI",
                participantRole = "Senior Mathematics Teacher",
                subjectDepartment = "Mathematics Department",
                avatarInitials = "DP",
                officeHours = "Mon–Thu • 2:30 PM – 4:00 PM",
                unreadCount = 1,
                lastMessagePreview = "Don't forget to label the vertex coordinates (h, k) on problem #4!",
                lastMessageTime = now - 900_000L
            )
        ).toInt()

        val t2Id = dao.insertChatThread(
            ChatThreadEntity(
                participantName = "Prof. Marcus Thorne",
                participantRole = "AP Physics & Robotics Mentor",
                subjectDepartment = "Science Department",
                avatarInitials = "MT",
                officeHours = "Tue & Fri • 1:30 PM – 3:30 PM",
                unreadCount = 0,
                lastMessagePreview = "Your group's force sensor calibration looked great in lab today.",
                lastMessageTime = now - 18_000_000L
            )
        ).toInt()

        val t3Id = dao.insertChatThread(
            ChatThreadEntity(
                participantName = "Ms. Clara Lin",
                participantRole = "English Literature & Debate Chair",
                subjectDepartment = "Humanities Department",
                avatarInitials = "CL",
                officeHours = "Daily • 12:45 PM – 1:30 PM",
                unreadCount = 1,
                lastMessagePreview = "I received your Julius Caesar essay draft—reviewing it this evening!",
                lastMessageTime = now - 42_000_000L
            )
        ).toInt()

        dao.insertChatMessages(
            listOf(
                ChatMessageEntity(
                    threadId = t1Id,
                    senderName = "DEV ANIL PRAJAPATI",
                    isFromCurrentUser = false,
                    messageText = "Hello! I uploaded today's class diary notes on Quadratic Vertex Form.",
                    timestamp = now - 3600_000L
                ),
                ChatMessageEntity(
                    threadId = t1Id,
                    senderName = "Student",
                    isFromCurrentUser = true,
                    messageText = "Thank you Sir! For problem #4 on the bridge arch, should we use meters or feet for the vertex height?",
                    timestamp = now - 1800_000L
                ),
                ChatMessageEntity(
                    threadId = t1Id,
                    senderName = "DEV ANIL PRAJAPATI",
                    isFromCurrentUser = false,
                    messageText = "Please use meters so it matches the standard SI units. Don't forget to label the vertex coordinates (h, k) on problem #4!",
                    timestamp = now - 900_000L
                ),
                ChatMessageEntity(
                    threadId = t2Id,
                    senderName = "Prof. Marcus Thorne",
                    isFromCurrentUser = false,
                    messageText = "Your group's force sensor calibration looked great in lab today. Let me know if you need the raw CSV data.",
                    timestamp = now - 18_000_000L
                ),
                ChatMessageEntity(
                    threadId = t3Id,
                    senderName = "Ms. Clara Lin",
                    isFromCurrentUser = false,
                    messageText = "I received your Julius Caesar essay draft—reviewing it this evening!",
                    timestamp = now - 42_000_000L
                )
            )
        )
    }
}
