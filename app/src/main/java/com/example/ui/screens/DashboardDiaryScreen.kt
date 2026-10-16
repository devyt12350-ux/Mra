package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AnnouncementEntity
import com.example.data.local.DiaryEntryEntity
import com.example.data.local.UserAccountEntity

@Composable
fun ClassDiaryScreen(
    account: UserAccountEntity,
    diaryEntries: List<DiaryEntryEntity>,
    selectedSubject: String,
    onSelectSubject: (String) -> Unit,
    onToggleAcknowledge: (DiaryEntryEntity) -> Unit,
    onAddDiaryEntry: (String, String, String, String, String, String, Boolean) -> Unit
) {
    val isTeacher = account.role.equals("Teacher", ignoreCase = true)
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val subjects = listOf("All", "Mathematics", "Physics", "Literature", "Computer Science", "Chemistry", "World History")

    val filteredEntries = if (selectedSubject == "All") {
        diaryEntries
    } else {
        diaryEntries.filter { it.subject.equals(selectedSubject, ignoreCase = true) }
    }

    val acknowledgedCount = diaryEntries.count { it.isAcknowledged }

    Scaffold(
        floatingActionButton = {
            // Only Teachers can create Class Diary entries; Students are read-only + acknowledge
            if (isTeacher) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_diary_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Post Class Diary")
                        Text(
                            text = "Post Class Diary",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("diary_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Campus Welcome & Daily Diary Summary Banner
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_campus_hero_1791378274201),
                            contentDescription = "Campus Diary Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF102238).copy(alpha = 0.92f),
                                            Color(0xFF1A2E4C).copy(alpha = 0.65f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "${account.studentIdNumber} • ${account.gradeSection}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "$acknowledgedCount/${diaryEntries.size} Signed Off",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Daily Class Diary & Homework Log",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isTeacher) {
                                        "Faculty Mode: Post daily lesson summaries and homework for students."
                                    } else {
                                        "Student View Only: Review teacher diary notes and sign off completed homework."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Subject Filter Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subject ->
                        FilterChip(
                            selected = selectedSubject == subject,
                            onClick = { onSelectSubject(subject) },
                            label = { Text(subject) },
                            modifier = Modifier.testTag("diary_filter_$subject")
                        )
                    }
                }
            }

            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "No Diary Entries",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "No diary entries for $selectedSubject yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Switch subjects above to view teacher diary entries.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredEntries, key = { it.id }) { entry ->
                    DiaryEntryCard(
                        entry = entry,
                        onToggleAcknowledge = { onToggleAcknowledge(entry) }
                    )
                }
            }
        }
    }

    if (showAddDialog && isTeacher) {
        AddDiaryEntryDialog(
            defaultTeacherName = account.fullName,
            onDismiss = { showAddDialog = false },
            onConfirm = { subject, dateLabel, topic, notes, homework, teacher, notify ->
                onAddDiaryEntry(subject, dateLabel, topic, notes, homework, teacher, notify)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DiaryEntryCard(
    entry: DiaryEntryEntity,
    onToggleAcknowledge: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("diary_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = entry.subject,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = entry.dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (entry.isAcknowledged) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onToggleAcknowledge() }
                        .testTag("diary_ack_${entry.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isAcknowledged) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                            contentDescription = "Acknowledge Diary",
                            tint = if (entry.isAcknowledged) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (entry.isAcknowledged) "Signed" else "Sign Diary",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.isAcknowledged) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = entry.topicCovered,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = entry.summaryNotes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Homework Box
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = "Homework",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Homework / Action Item",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = entry.homeworkTask,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Text(
                text = "Logged by ${entry.teacherName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddDiaryEntryDialog(
    defaultTeacherName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Boolean) -> Unit
) {
    var subject by rememberSaveable { mutableStateOf("Mathematics") }
    var topic by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var homework by rememberSaveable { mutableStateOf("") }
    var notifyPush by rememberSaveable { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Class Diary Entry (Faculty)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic Covered Today") },
                    placeholder = { Text("e.g. Trigonometric Identities") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diary_topic_input")
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Class Summary Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = homework,
                    onValueChange = { homework = it },
                    label = { Text("Homework / Follow-Up Task") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { notifyPush = !notifyPush }
                ) {
                    Checkbox(checked = notifyPush, onCheckedChange = { notifyPush = it })
                    Text(
                        text = "Send Push Notification to Class",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (topic.isNotBlank()) {
                        onConfirm(
                            subject.ifBlank { "General" },
                            "Today • Oct 7",
                            topic.trim(),
                            notes.ifBlank { "Reviewed key concepts and worked through class examples." },
                            homework.ifBlank { "Review today's lecture notes." },
                            defaultTeacherName,
                            notifyPush
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_add_diary_button")
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AnnouncementsScreen(
    account: UserAccountEntity,
    announcements: List<AnnouncementEntity>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onTogglePin: (AnnouncementEntity) -> Unit,
    onAddAnnouncement: (String, String, String, String, String, Boolean, Boolean, com.example.util.UploadedFileInfo?) -> Unit
) {
    val isTeacher = account.role.equals("Teacher", ignoreCase = true)
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var activeViewerNotice by remember { mutableStateOf<Triple<String, String, String?>?>(null) }
    val categories = listOf("All", "Urgent", "Academic", "Events", "Sports & Clubs")

    val filtered = if (selectedCategory == "All") {
        announcements
    } else {
        announcements.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            // Only Teachers/Admin can broadcast Announcements; Students are read-only
            if (isTeacher) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.testTag("add_announcement_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Broadcast Notice")
                        Text("Broadcast Notice", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("announcements_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onSelectCategory(category) },
                            label = { Text(category) },
                            modifier = Modifier.testTag("notice_filter_$category")
                        )
                    }
                }
            }

            items(filtered, key = { it.id }) { notice ->
                AnnouncementCard(
                    notice = notice,
                    isTeacher = isTeacher,
                    onTogglePin = { onTogglePin(notice) },
                    onViewAttachment = { name, uri, size ->
                        activeViewerNotice = Triple(name, uri, size)
                    }
                )
            }
        }
    }

    if (showAddDialog && isTeacher) {
        AddAnnouncementDialog(
            authorName = account.fullName,
            authorRole = "${account.role} • ${account.gradeSection}",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, body, category, pinned, sendPush, attachment ->
                onAddAnnouncement(title, body, category, account.fullName, account.role, pinned, sendPush, attachment)
                showAddDialog = false
            }
        )
    }

    val viewerDoc = activeViewerNotice
    if (viewerDoc != null) {
        val (name, uri, size) = viewerDoc
        val isPdf = name.endsWith(".pdf", ignoreCase = true)
        com.example.ui.components.DocumentViewerDialog(
            fileName = name,
            uriString = uri,
            fileType = if (isPdf) "PDF" else "IMAGE",
            fileSize = size,
            onDismiss = { activeViewerNotice = null }
        )
    }
}

@Composable
private fun AnnouncementCard(
    notice: AnnouncementEntity,
    isTeacher: Boolean,
    onTogglePin: () -> Unit,
    onViewAttachment: (String, String, String?) -> Unit
) {
    val isUrgent = notice.category.equals("Urgent", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUrgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.28f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (notice.isPinned) 1.5.dp else 0.dp,
                color = if (notice.isPinned) MaterialTheme.colorScheme.secondary else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("announcement_card_${notice.id}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = when (notice.category) {
                            "Urgent" -> MaterialTheme.colorScheme.error
                            "Academic" -> MaterialTheme.colorScheme.primary
                            "Events" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.tertiary
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = notice.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (notice.isPinned) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "PINNED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (isTeacher) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (notice.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Announcement",
                            tint = if (notice.isPinned) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = notice.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (notice.attachmentName != null) {
                        val isPdf = notice.attachmentType.equals("PDF", ignoreCase = true) ||
                            notice.attachmentName.endsWith(".pdf", ignoreCase = true)
                        Surface(
                            color = if (isPdf) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPdf) Color(0xFFEF9A9A) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onViewAttachment(
                                        notice.attachmentName,
                                        notice.attachmentUri ?: "",
                                        notice.attachmentSize
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                    contentDescription = null,
                                    tint = if (isPdf) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notice.attachmentName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${if (isPdf) "PDF Document" else "Photo"} • ${notice.attachmentSize ?: "Attached"} (Tap to view)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Posted by ${notice.authorName} • ${notice.authorRole}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddAnnouncementDialog(
    authorName: String,
    authorRole: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Boolean, Boolean, com.example.util.UploadedFileInfo?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Urgent") }
    var isPinned by rememberSaveable { mutableStateOf(true) }
    var sendPush by rememberSaveable { mutableStateOf(true) }
    var attachedFile by remember { mutableStateOf<com.example.util.UploadedFileInfo?>(null) }

    val categories = listOf("Urgent", "Academic", "Events", "Sports & Clubs")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish School Announcement") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Posting as $authorName ($authorRole)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Headline") },
                    placeholder = { Text("e.g. Tomorrow's Chemistry Lab Moved") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_title_input")
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Announcement Details") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_body_input")
                )

                com.example.ui.components.AttachmentUploaderSection(
                    currentAttachment = attachedFile,
                    onAttachmentSelected = { attachedFile = it }
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPinned = !isPinned }
                ) {
                    Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                    Text("Pin to top of school board", style = MaterialTheme.typography.bodySmall)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { sendPush = !sendPush }
                ) {
                    Checkbox(checked = sendPush, onCheckedChange = { sendPush = it })
                    Text("Dispatch instant Push Notification", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            body.ifBlank { "Please check the school portal for complete details." },
                            category,
                            isPinned,
                            sendPush,
                            attachedFile
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_add_notice_button")
            ) {
                Text("Broadcast")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
