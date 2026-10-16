package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.data.local.FeeReceiptInfo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.UserAccountEntity
import com.example.ui.ScholarViewModel
import com.example.ui.screens.AdminConsoleScreen
import com.example.ui.screens.AnnouncementsScreen
import com.example.ui.screens.AssignmentsScreen
import com.example.ui.screens.ClassDiaryScreen
import com.example.ui.screens.MessagingPortalScreen
import com.example.ui.screens.OnboardingAuthScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.ScholarHubTheme
import com.example.util.SchoolNotificationHelper
import kotlinx.coroutines.delay
import java.util.Locale

enum class PortalTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    ADMIN("admin", "Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings),
    DIARY("diary", "Diary", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    ANNOUNCEMENTS("notices", "Notices", Icons.Filled.Campaign, Icons.Outlined.Campaign),
    TIMETABLE("timetable", "Timetable", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    ASSIGNMENTS("assignments", "Tasks", Icons.Filled.Assignment, Icons.Outlined.Assignment),
    MESSAGES("messages", "Messages", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScholarHubTheme {
                ScholarHubApp()
            }
        }
    }
}

@Composable
fun ScholarHubApp(viewModel: ScholarViewModel = viewModel()) {
    val context = LocalContext.current
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()
    val diaryEntries by viewModel.diaryEntries.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val chatThreads by viewModel.chatThreads.collectAsStateWithLifecycle()
    val selectedThreadId by viewModel.selectedThreadId.collectAsStateWithLifecycle()
    val currentThreadMessages by viewModel.currentThreadMessages.collectAsStateWithLifecycle()

    val selectedSubjectFilter by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()
    val selectedAnnouncementCategory by viewModel.selectedAnnouncementCategory.collectAsStateWithLifecycle()
    val selectedDayOfWeek by viewModel.selectedDayOfWeek.collectAsStateWithLifecycle()
    val selectedAssignmentFilter by viewModel.selectedAssignmentFilter.collectAsStateWithLifecycle()
    val inAppBannerAlert by viewModel.inAppBannerAlert.collectAsStateWithLifecycle()
    val allStudentLocks by viewModel.allStudentLocks.collectAsStateWithLifecycle()
    val registeredStudents = viewModel.getRegisteredStudents()

    val isAdministrator = activeAccount?.role.equals("Admin", ignoreCase = true) || activeAccount?.role.equals("Administrator", ignoreCase = true)
    val visibleTabs = if (isAdministrator) {
        listOf(PortalTab.ADMIN, PortalTab.DIARY, PortalTab.ANNOUNCEMENTS, PortalTab.ASSIGNMENTS, PortalTab.MESSAGES)
    } else {
        listOf(PortalTab.DIARY, PortalTab.ANNOUNCEMENTS, PortalTab.TIMETABLE, PortalTab.ASSIGNMENTS, PortalTab.MESSAGES)
    }

    var currentTab by rememberSaveable { mutableStateOf(PortalTab.DIARY) }
    var showProfileDialog by rememberSaveable { mutableStateOf(false) }
    var showFeePaymentDialog by rememberSaveable { mutableStateOf(false) }
    var feeReceiptToView by remember { mutableStateOf<FeeReceiptInfo?>(null) }
    val latestFeeReceipt by viewModel.latestFeeReceipt.collectAsStateWithLifecycle()
    var hasNotificationPermission by rememberSaveable {
        mutableStateOf(SchoolNotificationHelper.hasNotificationPermission(context))
    }

    LaunchedEffect(activeAccount?.role) {
        if (isAdministrator && currentTab != PortalTab.ADMIN) {
            currentTab = PortalTab.ADMIN
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted) {
            viewModel.triggerPushAndBanner(
                title = "Push Notifications Enabled",
                body = "You will now receive instant alerts for teacher messages, announcements, and grades.",
                category = "SETTINGS"
            )
        }
    }

    LaunchedEffect(inAppBannerAlert) {
        if (inAppBannerAlert != null) {
            delay(4500L)
            viewModel.dismissBannerAlert()
        }
    }

    if (activeAccount == null) {
        OnboardingAuthScreen(
            onFindStudentByCode = { code -> viewModel.findStudentByCode(code) },
            onFindTeacherByCode = { code -> viewModel.findTeacherByCode(code) },
            onFindAdminByCode = { code -> viewModel.findAdminByCode(code) },
            onValidateSchoolCode = { code -> viewModel.validateSchoolCode(code) },
            onRegisterAccount = { schoolInfo, fullName, role, grade, studentId ->
                viewModel.registerVerifiedAccount(
                    schoolInfo = schoolInfo,
                    fullName = fullName,
                    role = role,
                    gradeSection = grade,
                    studentIdNumber = studentId
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        )
    } else if (activeAccount!!.role == "Student" && activeAccount!!.isAccountClosed) {
        // Locked account screen with in-app fee payment (₹40,000) option to instantly unlock
        ClosedStudentAccountScreen(
            account = activeAccount!!,
            onPayFeesClick = {
                showFeePaymentDialog = true
            },
            onSignOut = {
                viewModel.signOut()
            }
        )
    } else {
        val defaultBackTab = if (isAdministrator) PortalTab.ADMIN else PortalTab.DIARY
        if (currentTab != defaultBackTab && selectedThreadId == null) {
            BackHandler {
                currentTab = defaultBackTab
            }
        }

        val unreadMessagesTotal = chatThreads.sumOf { it.unreadCount }
        val pendingTasksCount = assignments.count { it.status == "Pending" }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 640.dp

            Scaffold(
                contentWindowInsets = WindowInsets.systemBars,
                topBar = {
                    Column {
                        PortalTopHeader(
                            account = activeAccount!!,
                            onOpenProfile = { showProfileDialog = true }
                        )

                        // Isolated 1-Time 2-Minute Limited Access Banner ONLY for unpaid-fee student (Reyansh Verma)
                        if (activeAccount!!.role == "Student" && !activeAccount!!.feesPaid) {
                            StudentLimitedTimeBanner(
                                viewModel = viewModel,
                                onPayFeesClick = {
                                    showFeePaymentDialog = true
                                }
                            )
                        }

                        // Optional Permission Prompt Banner for Android 13+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Allow push notifications for teacher messages & urgent school alerts",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    ) {
                                        Text("Enable")
                                    }
                                }
                            }
                        }

                        // Live Push Notification Banner inside App
                        AnimatedVisibility(
                            visible = inAppBannerAlert != null,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            if (inAppBannerAlert != null) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .testTag("live_push_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "Push Alert",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = inAppBannerAlert!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.dismissBannerAlert() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss Alert",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            visibleTabs.forEach { tab ->
                                val selected = currentTab == tab
                                val badgeCount = when (tab) {
                                    PortalTab.MESSAGES -> unreadMessagesTotal
                                    PortalTab.ASSIGNMENTS -> pendingTasksCount
                                    else -> 0
                                }
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        currentTab = tab
                                        if (tab != PortalTab.MESSAGES) {
                                            viewModel.openChatThread(null)
                                        }
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (badgeCount > 0) {
                                                    Badge { Text(badgeCount.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        }
                                    },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag("nav_tab_${tab.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            visibleTabs.forEach { tab ->
                                val selected = currentTab == tab
                                val badgeCount = when (tab) {
                                    PortalTab.MESSAGES -> unreadMessagesTotal
                                    PortalTab.ASSIGNMENTS -> pendingTasksCount
                                    else -> 0
                                }
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = {
                                        currentTab = tab
                                        if (tab != PortalTab.MESSAGES) {
                                            viewModel.openChatThread(null)
                                        }
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                if (badgeCount > 0) {
                                                    Badge { Text(badgeCount.toString()) }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        }
                                    },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag("nav_rail_${tab.route}")
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            PortalTab.ADMIN -> AdminConsoleScreen(
                                adminAccount = activeAccount!!,
                                registeredStudents = registeredStudents,
                                studentLocks = allStudentLocks,
                                diaryEntries = diaryEntries,
                                announcements = announcements,
                                assignments = assignments,
                                onGrantStudentOneTimeAccess = { code, sec -> viewModel.grantStudentOneTimeAccess(code, sec) },
                                onCloseStudentAccess = { code -> viewModel.closeStudentAccess(code) },
                                onOpenStudentFullAccess = { code -> viewModel.openStudentFullAccess(code) },
                                onToggleStudentFees = { code, paid -> viewModel.toggleStudentFees(code, paid) },
                                onCloseAllStudents = { viewModel.closeAllStudents() },
                                onOpenAllStudentsOneTime = { sec -> viewModel.openAllStudentsOneTime(sec) },
                                onOpenAllStudentsFull = { viewModel.openAllStudentsFull() },
                                onAddDiaryEntry = { subject, date, topic, notes, hw, teacher ->
                                    viewModel.addDiaryEntry(subject, date, topic, notes, hw, teacher, true)
                                },
                                onDeleteDiaryEntry = { viewModel.deleteDiaryEntry(it) },
                                onAddAnnouncement = { title, body, cat, author, role, pin, push, file ->
                                    viewModel.addAnnouncement(title, body, cat, author, role, pin, push, file)
                                },
                                onTogglePinAnnouncement = { viewModel.toggleAnnouncementPin(it) },
                                onDeleteAnnouncement = { viewModel.deleteAnnouncement(it) },
                                onAddAssignment = { subj, title, desc, due ->
                                    viewModel.addAssignment(title, subj, desc, due, 100, activeAccount!!.fullName, null)
                                },
                                onDeleteAssignment = { viewModel.deleteAssignment(it) },
                                onSendBroadcastAlert = { title, msg ->
                                    viewModel.triggerPushAndBanner(title, msg, "BROADCAST")
                                }
                            )
                            PortalTab.DIARY -> ClassDiaryScreen(
                                account = activeAccount!!,
                                diaryEntries = diaryEntries,
                                selectedSubject = selectedSubjectFilter,
                                onSelectSubject = { viewModel.setSubjectFilter(it) },
                                onToggleAcknowledge = { viewModel.toggleDiaryAcknowledged(it) },
                                onAddDiaryEntry = { subject, date, topic, notes, hw, teacher, notify ->
                                    viewModel.addDiaryEntry(subject, date, topic, notes, hw, teacher, notify)
                                }
                            )
                            PortalTab.ANNOUNCEMENTS -> AnnouncementsScreen(
                                account = activeAccount!!,
                                announcements = announcements,
                                selectedCategory = selectedAnnouncementCategory,
                                onSelectCategory = { viewModel.setAnnouncementCategory(it) },
                                onTogglePin = { viewModel.toggleAnnouncementPin(it) },
                                onAddAnnouncement = { title, body, category, author, role, pinned, push, attachment ->
                                    viewModel.addAnnouncement(title, body, category, author, role, pinned, push, attachment)
                                }
                            )
                            PortalTab.TIMETABLE -> TimetableScreen(
                                isTeacher = activeAccount!!.role.equals("Teacher", ignoreCase = true),
                                slots = timetableSlots,
                                selectedDay = selectedDayOfWeek,
                                onSelectDay = { viewModel.setSelectedDayOfWeek(it) },
                                onAddSlot = { day, period, start, end, subject, teacher, room, type ->
                                    viewModel.addTimetableSlot(day, period, start, end, subject, teacher, room, type)
                                },
                                onDeleteSlot = { viewModel.deleteTimetableSlot(it) }
                            )
                            PortalTab.ASSIGNMENTS -> AssignmentsScreen(
                                account = activeAccount!!,
                                assignments = assignments,
                                selectedFilter = selectedAssignmentFilter,
                                onSelectFilter = { viewModel.setAssignmentFilter(it) },
                                onSubmitAssignment = { assignment, note, attachment ->
                                    viewModel.submitAssignmentWork(assignment, note, attachment)
                                },
                                onGradeAssignment = { assignment, pts, feedback ->
                                    viewModel.gradeAssignmentWork(assignment, pts, feedback)
                                },
                                onAddAssignment = { title, subject, desc, due, maxPts, teacher, attachment ->
                                    viewModel.addAssignment(title, subject, desc, due, maxPts, teacher, attachment)
                                }
                            )
                            PortalTab.MESSAGES -> MessagingPortalScreen(
                                account = activeAccount!!,
                                threads = chatThreads,
                                selectedThreadId = selectedThreadId,
                                messages = currentThreadMessages,
                                onSelectThread = { viewModel.openChatThread(it) },
                                onSendMessage = { thread, text, attachment -> viewModel.sendChatMessage(thread, text, attachment) },
                                onSimulateIncomingPush = { thread -> viewModel.triggerSimulatedIncomingMessage(thread) },
                                onStartNewConversation = { name, role, dept, hours, msg ->
                                    viewModel.startNewConversation(name, role, dept, hours, msg)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showProfileDialog && activeAccount != null) {
        VerifiedSchoolProfileDialog(
            account = activeAccount!!,
            onDismiss = { showProfileDialog = false },
            onTestPushAlert = {
                viewModel.triggerPushAndBanner(
                    title = "MRS Mira Road School Notification",
                    body = "Push notification active for ${activeAccount!!.fullName} (${activeAccount!!.studentIdNumber}).",
                    category = "SYSTEM TEST"
                )
            },
            onPayFeesClick = {
                showProfileDialog = false
                showFeePaymentDialog = true
            },
            onViewReceiptClick = {
                showProfileDialog = false
                feeReceiptToView = FeeReceiptInfo(
                    receiptNumber = "REC-" + (activeAccount!!.feeTransactionId ?: "ONLINE-40000"),
                    studentName = activeAccount!!.fullName,
                    studentCode = activeAccount!!.studentIdNumber,
                    gradeSection = activeAccount!!.gradeSection,
                    schoolName = activeAccount!!.schoolName,
                    amount = activeAccount!!.feeAmount,
                    paymentMethod = activeAccount!!.feePaymentMethod ?: "Online App Payment",
                    transactionId = activeAccount!!.feeTransactionId ?: "TXN-VERIFIED-40000",
                    paymentDate = "Paid & Verified",
                    status = "PAID & VERIFIED"
                )
            },
            onSwitchSchoolOrSignOut = {
                showProfileDialog = false
                viewModel.signOut()
            }
        )
    }

    if (showFeePaymentDialog && activeAccount != null) {
        FeePaymentDialog(
            account = activeAccount!!,
            amount = 40000,
            onDismiss = { showFeePaymentDialog = false },
            onConfirmPayment = { method ->
                showFeePaymentDialog = false
                viewModel.payFeesFromApp(activeAccount!!.studentIdNumber, 40000, method)
            }
        )
    }

    val receiptToView = feeReceiptToView
    if (latestFeeReceipt != null) {
        FeeReceiptDialog(
            receipt = latestFeeReceipt!!,
            onDismiss = { viewModel.dismissFeeReceipt() }
        )
    } else if (receiptToView != null) {
        FeeReceiptDialog(
            receipt = receiptToView,
            onDismiss = { feeReceiptToView = null }
        )
    }
}

@Composable
private fun StudentLimitedTimeBanner(
    viewModel: ScholarViewModel,
    onPayFeesClick: () -> Unit
) {
    val limitedTimeSecondsLeft by viewModel.limitedTimeSecondsLeft.collectAsStateWithLifecycle()
    val mins = limitedTimeSecondsLeft / 60
    val secs = limitedTimeSecondsLeft % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", mins, secs)

    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("unpaid_fees_limited_access_banner")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Limited Access Warning",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ScholarViewModel.UNPAID_FEES_MESSAGE,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "One-time access remaining: $timeFormatted",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                )
            }
            Button(
                onClick = onPayFeesClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("banner_pay_fees_button")
            ) {
                Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pay ₹40,000", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ClosedStudentAccountScreen(
    account: UserAccountEntity,
    onPayFeesClick: () -> Unit,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .testTag("closed_account_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Account Locked",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "ACCOUNT LOCKED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = ScholarViewModel.UNPAID_FEES_MESSAGE,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Student Name: ${account.fullName}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Account Code: ${account.studentIdNumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "School: ${account.schoolName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Class: ${account.gradeSection}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Outstanding Fees Info & Pay Option
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Outstanding School Fees",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹40,000",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "You can pay your tuition fee of ₹40,000 directly from this app to instantly unlock and permanently reopen your student account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = onPayFeesClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pay_fees_40000_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay Fees ₹40,000 (Unlock Account)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("closed_account_sign_out_btn")
                ) {
                    Text("Back to Login")
                }
            }
        }
    }
}

@Composable
private fun PortalTopHeader(
    account: UserAccountEntity,
    onOpenProfile: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenProfile() }
                    .testTag("school_profile_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = account.fullName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = account.schoolName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified School Code",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${account.fullName} • ${account.studentIdNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val roleContainerColor = when {
                account.role.equals("Admin", ignoreCase = true) || account.role.equals("Administrator", ignoreCase = true) -> MaterialTheme.colorScheme.tertiaryContainer
                account.role == "Teacher" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
            val roleContentColor = when {
                account.role.equals("Admin", ignoreCase = true) || account.role.equals("Administrator", ignoreCase = true) -> MaterialTheme.colorScheme.onTertiaryContainer
                account.role == "Teacher" -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onPrimaryContainer
            }
            Surface(
                color = roleContainerColor,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = when {
                        account.role.equals("Admin", ignoreCase = true) || account.role.equals("Administrator", ignoreCase = true) -> "ADMIN"
                        else -> account.role.uppercase()
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = roleContentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun VerifiedSchoolProfileDialog(
    account: UserAccountEntity,
    onDismiss: () -> Unit,
    onTestPushAlert: () -> Unit,
    onPayFeesClick: () -> Unit,
    onViewReceiptClick: () -> Unit,
    onSwitchSchoolOrSignOut: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text("Account Details")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = account.schoolName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Name: ${account.fullName} (${account.role})",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Account Code: ${account.studentIdNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Class / Dept: ${account.gradeSection}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (account.role == "Student") {
                    Surface(
                        color = if (account.feesPaid) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "School Fees: ₹40,000",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = if (account.feesPaid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = if (account.feesPaid) "PAID ✅" else "PENDING ⚠️",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (account.feesPaid) {
                                Text(
                                    text = "Full academic year fees paid${if (account.feePaidOnline) " via App" else ""}.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (account.feeTransactionId != null) {
                                    Text(
                                        text = "Txn: ${account.feeTransactionId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(
                                    onClick = onViewReceiptClick,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View Fee Receipt (₹40,000)", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Text(
                                    text = "Tuition and lab fees of ₹40,000 are pending. Pay online from app to keep your account open permanently.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Button(
                                    onClick = onPayFeesClick,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pay ₹40,000 from App", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.size(4.dp))

                OutlinedButton(
                    onClick = onTestPushAlert,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_push_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Test Push Notification")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onSwitchSchoolOrSignOut,
                modifier = Modifier.testTag("sign_out_switch_school_button")
            ) {
                Text("Sign Out")
            }
        }
    )
}

@Composable
private fun FeePaymentDialog(
    account: UserAccountEntity,
    amount: Int = 40000,
    onDismiss: () -> Unit,
    onConfirmPayment: (String) -> Unit
) {
    var selectedMethod by rememberSaveable { mutableStateOf("UPI (Google Pay / PhonePe / Paytm)") }
    var isProcessing by remember { mutableStateOf(false) }

    val methods = listOf(
        "UPI (Google Pay / PhonePe / Paytm)" to Icons.Default.AccountBalanceWallet,
        "Debit / Credit Card (Visa / MasterCard / RuPay)" to Icons.Default.CreditCard,
        "Net Banking (All Major Indian Banks)" to Icons.Default.AccountBalance
    )

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("School Fee Payment")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isProcessing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(44.dp))
                        Text(
                            text = "Processing Fee Payment...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Securing transaction of ₹40,000 for ${account.fullName}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "MRS Mira Road School",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Student: ${account.fullName} • Code: ${account.studentIdNumber}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Class: ${account.gradeSection}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Fee Breakdown Table
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Fee Breakdown (Session 2025–26)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tuition Fee", style = MaterialTheme.typography.bodySmall)
                                Text("₹32,000", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Computer Lab & Library", style = MaterialTheme.typography.bodySmall)
                                Text("₹5,000", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Sports & Campus Development", style = MaterialTheme.typography.bodySmall)
                                Text("₹3,000", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total Payable",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "₹40,000",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        text = "Select Payment Mode:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    methods.forEach { (mode, icon) ->
                        val isSelected = selectedMethod == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedMethod = mode }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = mode,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isProcessing) {
                Button(
                    onClick = {
                        isProcessing = true
                        onConfirmPayment(selectedMethod)
                    },
                    modifier = Modifier.testTag("confirm_pay_40000_btn")
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pay ₹40,000 Now")
                }
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun FeeReceiptDialog(
    receipt: FeeReceiptInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Fee Payment e-Receipt")
            }
        },
        text = {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = receipt.schoolName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Finance Dept • Student Portal",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = receipt.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Text("Receipt No: ${receipt.receiptNumber}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("Date & Time: ${receipt.paymentDate}", style = MaterialTheme.typography.bodySmall)
                    Text("Student Name: ${receipt.studentName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Account Code: ${receipt.studentCode} (${receipt.gradeSection})", style = MaterialTheme.typography.bodySmall)
                    Text("Payment Mode: ${receipt.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                    Text("Transaction ID: ${receipt.transactionId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Amount Paid:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "₹${String.format(Locale.US, "%,d", receipt.amount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Amount in words: Rupees Forty Thousand Only",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "✅ Account status: UNLOCKED with full permanent access.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

