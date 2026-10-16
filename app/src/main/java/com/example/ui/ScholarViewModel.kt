package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AnnouncementEntity
import com.example.data.local.AssignmentEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatThreadEntity
import com.example.data.local.DiaryEntryEntity
import com.example.data.local.FeeReceiptInfo
import com.example.data.local.RegisteredStudentAccount
import com.example.data.local.ScholarDatabase
import com.example.data.local.StudentAccessLockEntity
import com.example.data.local.TimetableSlotEntity
import com.example.data.local.UserAccountEntity
import com.example.data.local.VerifiedSchoolInfo
import com.example.data.repository.ScholarRepository
import com.example.util.SchoolNotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ScholarViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val UNPAID_FEES_MESSAGE = "You have limited time access. Please contact the school Admin"
    }

    private val repository: ScholarRepository

    val activeAccount: StateFlow<UserAccountEntity?>
    val diaryEntries: StateFlow<List<DiaryEntryEntity>>
    val announcements: StateFlow<List<AnnouncementEntity>>
    val timetableSlots: StateFlow<List<TimetableSlotEntity>>
    val assignments: StateFlow<List<AssignmentEntity>>
    val chatThreads: StateFlow<List<ChatThreadEntity>>
    val allStudentLocks: StateFlow<List<StudentAccessLockEntity>>

    private val _selectedThreadId = MutableStateFlow<Int?>(null)
    val selectedThreadId: StateFlow<Int?> = _selectedThreadId.asStateFlow()

    val currentThreadMessages: StateFlow<List<ChatMessageEntity>>

    private val _selectedSubjectFilter = MutableStateFlow("All")
    val selectedSubjectFilter: StateFlow<String> = _selectedSubjectFilter.asStateFlow()

    private val _selectedAnnouncementCategory = MutableStateFlow("All")
    val selectedAnnouncementCategory: StateFlow<String> = _selectedAnnouncementCategory.asStateFlow()

    private val _selectedDayOfWeek = MutableStateFlow("Mon")
    val selectedDayOfWeek: StateFlow<String> = _selectedDayOfWeek.asStateFlow()

    private val _selectedAssignmentFilter = MutableStateFlow("All")
    val selectedAssignmentFilter: StateFlow<String> = _selectedAssignmentFilter.asStateFlow()

    private val _inAppBannerAlert = MutableStateFlow<String?>(null)
    val inAppBannerAlert: StateFlow<String?> = _inAppBannerAlert.asStateFlow()

    private val _limitedTimeSecondsLeft = MutableStateFlow(120)
    val limitedTimeSecondsLeft: StateFlow<Int> = _limitedTimeSecondsLeft.asStateFlow()

    private val _latestFeeReceipt = MutableStateFlow<FeeReceiptInfo?>(null)
    val latestFeeReceipt: StateFlow<FeeReceiptInfo?> = _latestFeeReceipt.asStateFlow()

    fun dismissFeeReceipt() {
        _latestFeeReceipt.value = null
    }

    init {
        val dao = ScholarDatabase.getInstance(application).scholarDao()
        repository = ScholarRepository(dao)
        SchoolNotificationHelper.createNotificationChannel(application)

        activeAccount = repository.activeAccount.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        diaryEntries = repository.diaryEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        announcements = repository.announcements.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        timetableSlots = repository.timetableSlots.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        assignments = repository.assignments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        chatThreads = repository.chatThreads.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allStudentLocks = repository.allStudentLocks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentThreadMessages = _selectedThreadId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeMessages(id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.ensureSeeded()
        }

        // Live sync student account with lock table when Admin modifies access
        viewModelScope.launch {
            repository.allStudentLocks.collect { locks ->
                val acct = activeAccount.value
                if (acct != null && acct.role == "Student") {
                    val myLock = locks.find { it.studentCode.equals(acct.studentIdNumber, ignoreCase = true) }
                    if (myLock != null) {
                        _limitedTimeSecondsLeft.value = myLock.remainingSeconds
                        val effectiveFeesPaid = acct.feesPaid || myLock.overrideFeesPaid
                        val shouldBeClosed = myLock.isPermanentlyLocked || (!effectiveFeesPaid && myLock.remainingSeconds <= 0)
                        if (acct.isAccountClosed != shouldBeClosed || acct.feesPaid != effectiveFeesPaid) {
                            repository.updateAccount(acct.copy(isAccountClosed = shouldBeClosed, feesPaid = effectiveFeesPaid))
                        }
                    }
                }
            }
        }

        // Strict 1-time 2-minute countdown timer ONLY for accounts with unpaid fees (Reyansh Verma MRS548272S)
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val acct = activeAccount.value
                if (acct != null && acct.role == "Student" && !acct.feesPaid && !acct.isAccountClosed) {
                    val currentRemaining = _limitedTimeSecondsLeft.value
                    if (currentRemaining <= 1) {
                        _limitedTimeSecondsLeft.value = 0
                        repository.updateStudentLock(acct.studentIdNumber, 0, true)
                        repository.updateAccount(acct.copy(isAccountClosed = true))
                        triggerPushAndBanner(
                            title = "Access Locked",
                            body = UNPAID_FEES_MESSAGE,
                            category = "ADMIN"
                        )
                    } else {
                        val nextRemaining = currentRemaining - 1
                        _limitedTimeSecondsLeft.value = nextRemaining
                        if (nextRemaining % 10 == 0) {
                            repository.updateStudentLock(acct.studentIdNumber, nextRemaining, false)
                        }
                    }
                }
            }
        }
    }

    fun findStudentByCode(code: String): RegisteredStudentAccount? = repository.findStudentByCode(code)

    fun findTeacherByCode(code: String): com.example.data.local.RegisteredTeacherAccount? = repository.findTeacherByCode(code)

    fun findAdminByCode(code: String): com.example.data.local.RegisteredAdminAccount? = repository.findAdminByCode(code)

    fun getRegisteredStudents(): List<RegisteredStudentAccount> = repository.registeredStudents

    fun validateSchoolCode(code: String): VerifiedSchoolInfo? = repository.validateSchoolCode(code)

    fun registerVerifiedAccount(
        schoolInfo: VerifiedSchoolInfo,
        fullName: String,
        role: String,
        gradeSection: String,
        studentIdNumber: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val normalizedCode = studentIdNumber.trim().uppercase()
            val matchedStudent = repository.findStudentByCode(normalizedCode)
            val studentLock = if (role == "Student") repository.getOrCreateStudentLock(normalizedCode) else null

            val isFeesPaid = when (role) {
                "Teacher", "Admin", "Administrator" -> true
                else -> (studentLock?.overrideFeesPaid == true) || (matchedStudent?.defaultFeesPaid ?: true)
            }

            val isLockedAlready = if (role == "Student" && !isFeesPaid) {
                _limitedTimeSecondsLeft.value = studentLock?.remainingSeconds ?: 120
                (studentLock?.isPermanentlyLocked == true) || ((studentLock?.remainingSeconds ?: 120) <= 0)
            } else {
                _limitedTimeSecondsLeft.value = 999999
                false
            }

            val account = UserAccountEntity(
                id = 1,
                schoolCode = schoolInfo.code,
                schoolName = schoolInfo.name,
                fullName = fullName.trim(),
                role = role,
                gradeSection = gradeSection.trim(),
                studentIdNumber = normalizedCode,
                feesPaid = isFeesPaid,
                isAccountClosed = isLockedAlready,
                notificationsEnabled = true,
                feeAmount = studentLock?.feeAmount ?: 40000,
                feePaidOnline = studentLock?.feePaidOnline ?: false,
                feeTransactionId = studentLock?.feeTransactionId,
                feePaidTimestamp = studentLock?.feePaidTimestamp,
                feePaymentMethod = studentLock?.feePaymentMethod
            )
            repository.saveAccountAndSeedIfNeeded(account)
            if (role == "Student" && !isFeesPaid) {
                triggerPushAndBanner(
                    title = schoolInfo.name,
                    body = UNPAID_FEES_MESSAGE,
                    category = "ADMIN"
                )
            } else {
                triggerPushAndBanner(
                    title = "Verified • ${schoolInfo.code}",
                    body = "Welcome to ${schoolInfo.name}, ${fullName.trim()}!",
                    category = "SECURITY"
                )
            }
            onComplete()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _selectedThreadId.value = null
            repository.signOutAccount()
        }
    }

    fun dismissBannerAlert() {
        _inAppBannerAlert.value = null
    }

    fun setSubjectFilter(subject: String) {
        _selectedSubjectFilter.value = subject
    }

    fun setAnnouncementCategory(category: String) {
        _selectedAnnouncementCategory.value = category
    }

    fun setSelectedDayOfWeek(day: String) {
        _selectedDayOfWeek.value = day
    }

    fun setAssignmentFilter(filter: String) {
        _selectedAssignmentFilter.value = filter
    }

    fun addDiaryEntry(
        subject: String,
        dateLabel: String,
        topicCovered: String,
        summaryNotes: String,
        homeworkTask: String,
        teacherName: String,
        notifyStudents: Boolean
    ) {
        viewModelScope.launch {
            repository.addDiaryEntry(
                DiaryEntryEntity(
                    subject = subject,
                    dateLabel = dateLabel,
                    topicCovered = topicCovered,
                    summaryNotes = summaryNotes,
                    homeworkTask = homeworkTask,
                    teacherName = teacherName
                )
            )
            if (notifyStudents) {
                triggerPushAndBanner(
                    title = "New Class Diary: $subject",
                    body = "$teacherName posted notes on '$topicCovered'. Homework: $homeworkTask",
                    category = "CLASS DIARY"
                )
            } else {
                _inAppBannerAlert.value = "Class diary entry added for $subject"
            }
        }
    }

    fun toggleDiaryAcknowledged(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.toggleDiaryAcknowledged(entry)
        }
    }

    fun addAnnouncement(
        title: String,
        body: String,
        category: String,
        authorName: String,
        authorRole: String,
        isPinned: Boolean,
        sendPush: Boolean,
        attachment: com.example.util.UploadedFileInfo? = null
    ) {
        viewModelScope.launch {
            repository.addAnnouncement(
                AnnouncementEntity(
                    title = title,
                    body = body,
                    category = category,
                    authorName = authorName,
                    authorRole = authorRole,
                    isPinned = isPinned,
                    attachmentUri = attachment?.uriString,
                    attachmentName = attachment?.fileName,
                    attachmentType = attachment?.fileType,
                    attachmentSize = attachment?.fileSizeFormatted
                )
            )
            val attachSuffix = if (attachment != null) " [Attached ${attachment.fileType}]" else ""
            if (sendPush) {
                triggerPushAndBanner(
                    title = "[$category] $title$attachSuffix",
                    body = body,
                    category = "ANNOUNCEMENT"
                )
            } else {
                _inAppBannerAlert.value = "Announcement published to school board"
            }
        }
    }

    fun toggleAnnouncementPin(announcement: AnnouncementEntity) {
        viewModelScope.launch {
            repository.toggleAnnouncementPin(announcement)
        }
    }

    fun addTimetableSlot(
        dayOfWeek: String,
        periodNumber: Int,
        startTime: String,
        endTime: String,
        subject: String,
        teacherName: String,
        roomNumber: String,
        sessionType: String
    ) {
        viewModelScope.launch {
            val accent = when (subject.lowercase()) {
                "mathematics" -> 0xFF1A2E4C
                "physics", "chemistry" -> 0xFF1F6E60
                "literature" -> 0xFFB86814
                "computer science" -> 0xFF5B3E96
                else -> 0xFF9E3A34
            }
            repository.addTimetableSlot(
                TimetableSlotEntity(
                    dayOfWeek = dayOfWeek,
                    periodNumber = periodNumber,
                    startTime = startTime,
                    endTime = endTime,
                    subject = subject,
                    teacherName = teacherName,
                    roomNumber = roomNumber,
                    sessionType = sessionType,
                    accentHex = accent
                )
            )
            _inAppBannerAlert.value = "Added $subject (Period $periodNumber) to $dayOfWeek schedule"
        }
    }

    fun deleteTimetableSlot(slotId: Int) {
        viewModelScope.launch {
            repository.deleteTimetableSlot(slotId)
        }
    }

    fun addAssignment(
        title: String,
        subject: String,
        description: String,
        dueDateLabel: String,
        maxPoints: Int,
        teacherName: String,
        attachment: com.example.util.UploadedFileInfo? = null
    ) {
        viewModelScope.launch {
            repository.addAssignment(
                AssignmentEntity(
                    title = title,
                    subject = subject,
                    description = description,
                    dueDateLabel = dueDateLabel,
                    maxPoints = maxPoints,
                    status = "Pending",
                    teacherName = teacherName,
                    attachedUri = attachment?.uriString,
                    attachedFileName = attachment?.fileName,
                    attachedFileType = attachment?.fileType,
                    attachedFileSize = attachment?.fileSizeFormatted
                )
            )
            triggerPushAndBanner(
                title = "New Assignment: $subject",
                body = "$title ($dueDateLabel • $maxPoints pts)",
                category = "ASSIGNMENTS"
            )
        }
    }

    fun submitAssignmentWork(
        assignment: AssignmentEntity,
        submissionNote: String,
        attachment: com.example.util.UploadedFileInfo? = null
    ) {
        viewModelScope.launch {
            repository.submitAssignment(
                assignment = assignment,
                submissionNote = submissionNote,
                attachedUri = attachment?.uriString,
                attachedFileName = attachment?.fileName,
                attachedFileType = attachment?.fileType,
                attachedFileSize = attachment?.fileSizeFormatted
            )
            val attachText = if (attachment != null) " with ${attachment.fileType} (${attachment.fileName})" else ""
            triggerPushAndBanner(
                title = "Assignment Submitted • ${assignment.subject}",
                body = "Your work$attachText for '${assignment.title}' has been sent to ${assignment.teacherName}.",
                category = "SUBMISSION"
            )
        }
    }

    fun gradeAssignmentWork(assignment: AssignmentEntity, awardedPoints: Int, feedback: String) {
        viewModelScope.launch {
            repository.gradeAssignment(assignment, awardedPoints, feedback)
            triggerPushAndBanner(
                title = "Grade Posted: ${assignment.title}",
                body = "${assignment.teacherName} graded your ${assignment.subject} task: $awardedPoints/${assignment.maxPoints} pts — \"$feedback\"",
                category = "GRADES"
            )
        }
    }

    fun openChatThread(threadId: Int?) {
        _selectedThreadId.value = threadId
        if (threadId != null) {
            viewModelScope.launch {
                repository.markThreadRead(threadId)
            }
        }
    }

    fun sendChatMessage(
        thread: ChatThreadEntity,
        text: String,
        attachment: com.example.util.UploadedFileInfo? = null,
        autoTeacherReply: Boolean = true
    ) {
        val sender = activeAccount.value?.fullName ?: "Student"
        viewModelScope.launch {
            repository.sendMessage(
                threadId = thread.id,
                senderName = sender,
                isFromCurrentUser = true,
                messageText = text.trim(),
                attachmentUri = attachment?.uriString,
                attachmentName = attachment?.fileName,
                attachmentType = attachment?.fileType,
                attachmentSize = attachment?.fileSizeFormatted
            )
            if (autoTeacherReply) {
                delay(1400L)
                val replyText = buildContextualTeacherReply(
                    teacherName = thread.participantName,
                    department = thread.subjectDepartment,
                    studentMessage = text,
                    hasAttachment = attachment != null
                )
                repository.sendMessage(
                    threadId = thread.id,
                    senderName = thread.participantName,
                    isFromCurrentUser = false,
                    messageText = replyText
                )
                if (_selectedThreadId.value == thread.id) {
                    repository.markThreadRead(thread.id)
                }
                triggerPushAndBanner(
                    title = "New Message from ${thread.participantName}",
                    body = replyText,
                    category = "MESSAGING"
                )
            }
        }
    }

    fun triggerSimulatedIncomingMessage(thread: ChatThreadEntity) {
        viewModelScope.launch {
            val message = "Hi! Quick reminder from ${thread.subjectDepartment}: office hours are open today (${thread.officeHours}) if you'd like feedback on your upcoming assessment."
            repository.sendMessage(
                threadId = thread.id,
                senderName = thread.participantName,
                isFromCurrentUser = false,
                messageText = message
            )
            triggerPushAndBanner(
                title = "${thread.participantName} (${thread.subjectDepartment})",
                body = message,
                category = "MESSAGING"
            )
        }
    }

    fun startNewConversation(
        participantName: String,
        participantRole: String,
        subjectDepartment: String,
        officeHours: String,
        initialMessage: String
    ) {
        val sender = activeAccount.value?.fullName ?: "Student"
        viewModelScope.launch {
            val threadId = repository.createNewThread(
                participantName = participantName,
                participantRole = participantRole,
                subjectDepartment = subjectDepartment,
                officeHours = officeHours,
                initialMessage = initialMessage,
                senderName = sender
            )
            _selectedThreadId.value = threadId
            delay(1200L)
            val reply = "Hello $sender! Thank you for reaching out to $subjectDepartment. I have noted your message and will assist you shortly."
            repository.sendMessage(
                threadId = threadId,
                senderName = participantName,
                isFromCurrentUser = false,
                messageText = reply
            )
            repository.markThreadRead(threadId)
            triggerPushAndBanner(
                title = "Reply from $participantName",
                body = reply,
                category = "MESSAGING"
            )
        }
    }

    fun triggerPushAndBanner(title: String, body: String, category: String) {
        SchoolNotificationHelper.sendPushNotification(
            context = getApplication(),
            title = title,
            message = body,
            categoryTag = category
        )
        _inAppBannerAlert.value = "$title — $body"
    }

    private fun buildContextualTeacherReply(
        teacherName: String,
        department: String,
        studentMessage: String,
        hasAttachment: Boolean = false
    ): String {
        val lower = studentMessage.lowercase()
        return when {
            hasAttachment ->
                "Thank you for uploading the attached document! I have received your file and will review your solutions carefully."
            lower.contains("homework") || lower.contains("diary") || lower.contains("exercise") ->
                "Great question! Check the step-by-step worked example in today's Class Diary entry, and feel free to show your draft in class tomorrow."
            lower.contains("due") || lower.contains("deadline") || lower.contains("submit") || lower.contains("extension") ->
                "Please submit your current progress via the Assignments tab before the deadline. I'm happy to review your work and provide feedback!"
            lower.contains("lab") || lower.contains("exam") || lower.contains("test") || lower.contains("quiz") ->
                "Focus on the core concepts highlighted in this week's seminar notes. Stop by during my office hours if you want to walk through practice problems."
            else ->
                "Thanks for the update! I've noted this for our $department session. Keep up the great effort, and let me know if you need any extra resources."
        }
    }

    // Admin Controls: Open and Close 1-Time for Students and All Things
    fun grantStudentOneTimeAccess(studentCode: String, seconds: Int = 120) {
        viewModelScope.launch {
            repository.grantStudentOneTimeAccess(studentCode, seconds)
            val acct = activeAccount.value
            if (acct != null && acct.studentIdNumber.equals(studentCode, ignoreCase = true)) {
                _limitedTimeSecondsLeft.value = seconds
                repository.updateAccount(acct.copy(isAccountClosed = false))
            }
            triggerPushAndBanner(
                title = "Admin: 1-Time Access Granted",
                body = "1-Time access of $seconds seconds opened for student $studentCode.",
                category = "ADMIN"
            )
        }
    }

    fun closeStudentAccess(studentCode: String) {
        viewModelScope.launch {
            repository.closeStudentAccess(studentCode)
            val acct = activeAccount.value
            if (acct != null && acct.studentIdNumber.equals(studentCode, ignoreCase = true)) {
                _limitedTimeSecondsLeft.value = 0
                repository.updateAccount(acct.copy(isAccountClosed = true))
            }
            triggerPushAndBanner(
                title = "Admin: Student Access Closed",
                body = "Access closed and locked for student $studentCode.",
                category = "ADMIN"
            )
        }
    }

    fun openStudentFullAccess(studentCode: String) {
        viewModelScope.launch {
            repository.openStudentFullAccess(studentCode)
            val acct = activeAccount.value
            if (acct != null && acct.studentIdNumber.equals(studentCode, ignoreCase = true)) {
                _limitedTimeSecondsLeft.value = 999999
                repository.updateAccount(acct.copy(isAccountClosed = false, feesPaid = true))
            }
            triggerPushAndBanner(
                title = "Admin: Full Access Granted",
                body = "Student $studentCode unlocked with full open access.",
                category = "ADMIN"
            )
        }
    }

    fun toggleStudentFees(studentCode: String, isPaid: Boolean, amount: Int = 40000) {
        viewModelScope.launch {
            repository.setStudentFeesPaid(studentCode, isPaid, amount)
            val acct = activeAccount.value
            if (acct != null && acct.studentIdNumber.equals(studentCode, ignoreCase = true)) {
                repository.updateAccount(
                    acct.copy(
                        feesPaid = isPaid,
                        isAccountClosed = !isPaid && acct.isAccountClosed,
                        feeAmount = amount
                    )
                )
            }
            triggerPushAndBanner(
                title = "Admin: Student Fees Updated",
                body = "Student $studentCode fees marked as ${if (isPaid) "PAID (₹$amount)" else "PENDING"}.",
                category = "ADMIN"
            )
        }
    }

    fun payFeesFromApp(
        studentCode: String,
        amount: Int = 40000,
        paymentMethod: String = "UPI / App Payment"
    ) {
        viewModelScope.launch {
            val receipt = repository.payStudentFeesFromApp(studentCode, amount, paymentMethod)
            _latestFeeReceipt.value = receipt
            _limitedTimeSecondsLeft.value = 999999
            val acct = activeAccount.value
            if (acct != null && acct.studentIdNumber.equals(studentCode, ignoreCase = true)) {
                repository.updateAccount(
                    acct.copy(
                        feesPaid = true,
                        isAccountClosed = false,
                        feeAmount = amount,
                        feePaidOnline = true,
                        feeTransactionId = receipt.transactionId,
                        feePaidTimestamp = System.currentTimeMillis(),
                        feePaymentMethod = paymentMethod
                    )
                )
            }
            triggerPushAndBanner(
                title = "Fees Paid: ₹40,000",
                body = "School tuition fee of ₹40,000 paid successfully via $paymentMethod! Account unlocked.",
                category = "FEES"
            )
        }
    }

    fun closeAllStudents() {
        viewModelScope.launch {
            repository.closeAllStudents()
            val acct = activeAccount.value
            if (acct != null && acct.role == "Student") {
                _limitedTimeSecondsLeft.value = 0
                repository.updateAccount(acct.copy(isAccountClosed = true))
            }
            triggerPushAndBanner(
                title = "Campus Admin Alert",
                body = "Student portal access closed for all students.",
                category = "ADMIN"
            )
        }
    }

    fun openAllStudentsOneTime(seconds: Int = 120) {
        viewModelScope.launch {
            repository.openAllStudentsOneTime(seconds)
            val acct = activeAccount.value
            if (acct != null && acct.role == "Student") {
                _limitedTimeSecondsLeft.value = seconds
                repository.updateAccount(acct.copy(isAccountClosed = false))
            }
            triggerPushAndBanner(
                title = "Campus Admin Alert",
                body = "1-Time access granted to all students ($seconds seconds session).",
                category = "ADMIN"
            )
        }
    }

    fun openAllStudentsFull() {
        viewModelScope.launch {
            repository.openAllStudentsFull()
            val acct = activeAccount.value
            if (acct != null && acct.role == "Student") {
                _limitedTimeSecondsLeft.value = 999999
                repository.updateAccount(acct.copy(isAccountClosed = false, feesPaid = true))
            }
            triggerPushAndBanner(
                title = "Campus Admin Alert",
                body = "All student accounts unlocked with full open access.",
                category = "ADMIN"
            )
        }
    }

    fun deleteDiaryEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteDiaryEntry(id)
            triggerPushAndBanner(
                title = "Admin: Diary Updated",
                body = "Diary entry #$id removed.",
                category = "DIARY"
            )
        }
    }

    fun deleteAnnouncement(id: Int) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
            triggerPushAndBanner(
                title = "Admin: Notice Removed",
                body = "Announcement #$id removed.",
                category = "NOTICES"
            )
        }
    }

    fun deleteAssignment(id: Int) {
        viewModelScope.launch {
            repository.deleteAssignment(id)
            triggerPushAndBanner(
                title = "Admin: Task Removed",
                body = "Assignment #$id removed.",
                category = "TASKS"
            )
        }
    }
}
