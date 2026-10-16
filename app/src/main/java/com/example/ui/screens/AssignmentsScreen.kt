package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.AssignmentEntity
import com.example.data.local.UserAccountEntity
import com.example.ui.components.AttachmentUploaderSection
import com.example.ui.components.DocumentViewerDialog
import com.example.util.UploadedFileInfo

@Composable
fun AssignmentsScreen(
    account: UserAccountEntity,
    assignments: List<AssignmentEntity>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    onSubmitAssignment: (AssignmentEntity, String, UploadedFileInfo?) -> Unit,
    onGradeAssignment: (AssignmentEntity, Int, String) -> Unit,
    onAddAssignment: (String, String, String, String, Int, String, UploadedFileInfo?) -> Unit
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var assignmentToSubmit by remember { mutableStateOf<AssignmentEntity?>(null) }
    var assignmentToGrade by remember { mutableStateOf<AssignmentEntity?>(null) }
    var activeViewerDoc by remember { mutableStateOf<Triple<String, String, String?>?>(null) }

    val filters = listOf("All", "Pending", "Submitted", "Graded")
    val filtered = if (selectedFilter == "All") {
        assignments
    } else {
        assignments.filter { it.status.equals(selectedFilter, ignoreCase = true) }
    }

    val completedCount = assignments.count { it.status != "Pending" }
    val completionFraction = if (assignments.isEmpty()) 0f else completedCount.toFloat() / assignments.size

    Scaffold(
        floatingActionButton = {
            if (account.role.equals("Teacher", ignoreCase = true)) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_assignment_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create Assignment")
                        Text("New Assignment", style = MaterialTheme.typography.labelLarge)
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
                .testTag("assignments_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Academic Progress Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
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
                            Column {
                                Text(
                                    text = "Coursework Completion Tracker",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$completedCount of ${assignments.size} assignments submitted or graded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "${(completionFraction * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { completionFraction },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                }
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { status ->
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { onSelectFilter(status) },
                            label = { Text(status) },
                            modifier = Modifier.testTag("assignment_filter_$status")
                        )
                    }
                }
            }

            items(filtered, key = { it.id }) { assignment ->
                AssignmentItemCard(
                    assignment = assignment,
                    currentRole = account.role,
                    onSubmitClick = { assignmentToSubmit = assignment },
                    onGradeClick = { assignmentToGrade = assignment },
                    onViewAttachment = { name, uri, size ->
                        activeViewerDoc = Triple(name, uri, size)
                    }
                )
            }
        }
    }

    if (activeViewerDoc != null) {
        val (name, uri, size) = activeViewerDoc!!
        val isPdf = name.endsWith(".pdf", ignoreCase = true)
        DocumentViewerDialog(
            fileName = name,
            uriString = uri,
            fileType = if (isPdf) "PDF" else "IMAGE",
            fileSize = size,
            onDismiss = { activeViewerDoc = null }
        )
    }

    if (assignmentToSubmit != null) {
        SubmitAssignmentDialog(
            assignment = assignmentToSubmit!!,
            onDismiss = { assignmentToSubmit = null },
            onConfirm = { note, attachment ->
                onSubmitAssignment(assignmentToSubmit!!, note, attachment)
                assignmentToSubmit = null
            }
        )
    }

    if (assignmentToGrade != null) {
        GradeAssignmentDialog(
            assignment = assignmentToGrade!!,
            onDismiss = { assignmentToGrade = null },
            onConfirm = { score, feedback ->
                onGradeAssignment(assignmentToGrade!!, score, feedback)
                assignmentToGrade = null
            }
        )
    }

    if (showCreateDialog) {
        CreateAssignmentDialog(
            defaultTeacher = if (account.role == "Teacher") account.fullName else "DEV ANIL PRAJAPATI",
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, subject, desc, due, maxPts, teacher, attachment ->
                onAddAssignment(title, subject, desc, due, maxPts, teacher, attachment)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun AssignmentItemCard(
    assignment: AssignmentEntity,
    currentRole: String,
    onSubmitClick: () -> Unit,
    onGradeClick: () -> Unit,
    onViewAttachment: (String, String, String?) -> Unit
) {
    val statusColor = when (assignment.status) {
        "Graded" -> MaterialTheme.colorScheme.tertiary
        "Submitted" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("assignment_card_${assignment.id}")
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
                            text = assignment.subject,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = assignment.status.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = if (assignment.awardedPoints != null) {
                        "${assignment.awardedPoints}/${assignment.maxPoints} pts"
                    } else {
                        "${assignment.maxPoints} pts"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = assignment.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${assignment.dueDateLabel} • Instructor: ${assignment.teacherName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (assignment.submissionNote.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Student Submission Note",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = assignment.submissionNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Attached Photo or PDF Card
            if (assignment.attachedFileName != null) {
                val isPdf = assignment.attachedFileType.equals("PDF", ignoreCase = true) ||
                    assignment.attachedFileName.endsWith(".pdf", ignoreCase = true)
                Surface(
                    color = if (isPdf) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isPdf) Color(0xFFEF9A9A) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onViewAttachment(
                                assignment.attachedFileName,
                                assignment.attachedUri ?: "",
                                assignment.attachedFileSize
                            )
                        }
                        .testTag("assignment_attachment_badge_${assignment.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isPdf) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD32F2F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF File",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Image Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = assignment.attachedFileName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Attached ${if (isPdf) "PDF Document" else "Photo"} • ${assignment.attachedFileSize ?: "Uploaded"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isPdf) "View PDF" else "View Photo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (assignment.teacherFeedback.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Faculty Evaluation & Feedback (${assignment.awardedPoints}/${assignment.maxPoints} pts)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = assignment.teacherFeedback,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (assignment.status == "Pending" || currentRole == "Student") {
                    Button(
                        onClick = onSubmitClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_assignment_btn_${assignment.id}")
                    ) {
                        Icon(
                            imageVector = if (assignment.status == "Pending") Icons.Default.UploadFile else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (assignment.status == "Pending") "Submit Work" else "Update Submission"
                        )
                    }
                }

                if (currentRole.equals("Teacher", ignoreCase = true)) {
                    OutlinedButton(
                        onClick = onGradeClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("grade_assignment_btn_${assignment.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Grade,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grade & Notify")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmitAssignmentDialog(
    assignment: AssignmentEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, UploadedFileInfo?) -> Unit
) {
    var note by rememberSaveable {
        mutableStateOf(
            assignment.submissionNote.ifBlank {
                "Attached completed solution worksheet and step-by-step verification."
            }
        )
    }
    var attachedFile by remember {
        mutableStateOf(
            if (assignment.attachedFileName != null) {
                UploadedFileInfo(
                    uriString = assignment.attachedUri ?: "",
                    fileName = assignment.attachedFileName,
                    fileType = assignment.attachedFileType ?: "PDF",
                    fileSizeFormatted = assignment.attachedFileSize ?: "Uploaded"
                )
            } else null
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Assignment") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${assignment.subject} • ${assignment.title}",
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Submission Summary / Notes") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submission_note_input")
                )

                AttachmentUploaderSection(
                    currentAttachment = attachedFile,
                    onAttachmentSelected = { attachedFile = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (note.isNotBlank() || attachedFile != null) {
                        onConfirm(note.trim().ifBlank { "Attached homework file." }, attachedFile)
                    }
                },
                modifier = Modifier.testTag("confirm_submit_assignment_button")
            ) {
                Text("Submit Now")
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
private fun GradeAssignmentDialog(
    assignment: AssignmentEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var pointsText by rememberSaveable { mutableStateOf((assignment.awardedPoints ?: 95).toString()) }
    var feedback by rememberSaveable {
        mutableStateOf(
            assignment.teacherFeedback.ifBlank {
                "Well-structured work and thorough analysis! Keep up the great effort."
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grade Submission & Send Alert") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${assignment.title} (Max ${assignment.maxPoints} pts)",
                    style = MaterialTheme.typography.titleSmall
                )
                OutlinedTextField(
                    value = pointsText,
                    onValueChange = { pointsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Awarded Score (out of ${assignment.maxPoints})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Instructor Feedback") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pts = (pointsText.toIntOrNull() ?: assignment.maxPoints).coerceIn(0, assignment.maxPoints)
                    onConfirm(pts, feedback.ifBlank { "Graded." })
                }
            ) {
                Text("Publish Grade & Notify")
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
private fun CreateAssignmentDialog(
    defaultTeacher: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Int, String, UploadedFileInfo?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("Mathematics") }
    var description by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf("Due Oct 14, 11:59 PM") }
    var maxPoints by rememberSaveable { mutableStateOf("100") }
    var attachedFile by remember { mutableStateOf<UploadedFileInfo?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Assignment") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title") },
                    placeholder = { Text("e.g. Calculus Derivatives Problem Set") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assignment_title_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions & Rubric") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlinedTextField(
                        value = maxPoints,
                        onValueChange = { maxPoints = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Points") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                AttachmentUploaderSection(
                    currentAttachment = attachedFile,
                    onAttachmentSelected = { attachedFile = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            subject.ifBlank { "General" },
                            description.ifBlank { "Complete all assigned problems and show full working." },
                            dueDate.ifBlank { "Due Next Week" },
                            maxPoints.toIntOrNull() ?: 100,
                            defaultTeacher,
                            attachedFile
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_add_assignment_button")
            ) {
                Text("Assign & Notify")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
