package com.example.ui.screens.school

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.ExamResultEntity
import com.example.data.SchoolClassEntity
import com.example.data.StudentEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolExamsTab(
    activeBusiness: BusinessEntity?,
    students: List<StudentEntity>,
    classes: List<SchoolClassEntity>,
    examResults: List<ExamResultEntity>,
    onSaveExamResult: (ExamResultEntity) -> Unit,
    onDeleteExamResult: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterClass by remember { mutableStateOf("All Classes") }
    var showAddDialog by remember { mutableStateOf(false) }
    var resultToDelete by remember { mutableStateOf<ExamResultEntity?>(null) }

    val classNames: List<String> = remember(classes) {
        listOf("All Classes") + classes.map { it.className }.distinct()
    }

    val filteredResults = remember(examResults, searchQuery, selectedFilterClass) {
        examResults.filter { result ->
            val matchClass = selectedFilterClass == "All Classes" || result.className.equals(selectedFilterClass, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() ||
                result.studentName.contains(searchQuery, ignoreCase = true) ||
                result.rollNumber.contains(searchQuery, ignoreCase = true) ||
                result.subject.contains(searchQuery, ignoreCase = true) ||
                result.examName.contains(searchQuery, ignoreCase = true)
            matchClass && matchSearch
        }
    }

    fun shareResultCard(result: ExamResultEntity) {
        val pct = if (result.totalMarks > 0) (result.obtainedMarks / result.totalMarks) * 100 else 0.0
        val text = """
            🏫 *${activeBusiness?.name ?: "School & Academy"}*
            📋 *EXAM RESULT CARD / MARKSHEET*
            ------------------------------------
            👤 *Student:* ${result.studentName}
            🆔 *Roll No:* ${result.rollNumber}
            📚 *Class:* ${result.className}
            📝 *Exam:* ${result.examName}
            📖 *Subject:* ${result.subject}
            ------------------------------------
            🎯 *Marks Obtained:* ${"%.1f".format(result.obtainedMarks)} / ${"%.0f".format(result.totalMarks)}
            📊 *Percentage:* ${"%.1f".format(pct)}%
            🏆 *Grade:* ${result.grade}
            ------------------------------------
            Generated via PakBusiness Pro Portal
        """.trimIndent()

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Exam Result Card")
        context.startActivity(shareIntent)
    }

    Box(modifier = modifier.fillMaxSize().testTag("school_exams_tab")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header stats
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PakGoldContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Exams & Student Results",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PakEmeraldDark
                        )
                        Text(
                            text = "${examResults.size} Marks Records Logged",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PakGoldSecondary
                    ) {
                        val avgPct = if (examResults.isNotEmpty()) {
                            examResults.map { if (it.totalMarks > 0) (it.obtainedMarks / it.totalMarks) * 100 else 0.0 }.average()
                        } else 0.0
                        Text(
                            text = "School Avg: ${"%.1f".format(avgPct)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Search Bar & Filter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by student, roll #, subject...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PakEmeraldPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("school_exam_search_input")
                )
            }

            // List of Exam Results
            if (filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "No exam results recorded yet. Tap '+' to record marks!" else "No results match '$searchQuery'",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredResults, key = { it.id }) { result ->
                        val pct = if (result.totalMarks > 0) (result.obtainedMarks / result.totalMarks) * 100 else 0.0
                        val isPass = pct >= 40.0

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("school_exam_item_${result.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isPass) PakEmeraldContainer else Color(0xFFFFEBEE)
                                        ) {
                                            Text(
                                                text = result.grade,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPass) PakEmeraldPrimary else Color(0xFFC62828),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = result.studentName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Roll #${result.rollNumber} • ${result.className}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${"%.1f".format(result.obtainedMarks)} / ${"%.0f".format(result.totalMarks)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isPass) PakEmeraldPrimary else Color(0xFFC62828)
                                        )
                                        Text(
                                            text = "${"%.1f".format(pct)}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${result.examName} • ${result.subject}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { shareResultCard(result) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = "Share Marks Card",
                                                tint = Color(0xFF25D366),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { resultToDelete = result },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to Add Exam Result
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PakEmeraldPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("btn_add_exam_result")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Record Exam Marks")
        }
    }

    // Add Exam Result Dialog
    if (showAddDialog) {
        var selectedStudent by remember { mutableStateOf<StudentEntity?>(students.firstOrNull()) }
        var examName by remember { mutableStateOf("Final Term Exam 2026") }
        var subject by remember { mutableStateOf("Mathematics") }
        var totalMarksText by remember { mutableStateOf("100") }
        var obtainedMarksText by remember { mutableStateOf("75") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Record Student Exam Result",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (students.isNotEmpty()) {
                        var studentDropdownExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = studentDropdownExpanded,
                            onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedStudent?.let { "${it.name} (Roll: ${it.rollNumber ?: it.rollNo})" } ?: "Select Student",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Student *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = studentDropdownExpanded,
                                onDismissRequest = { studentDropdownExpanded = false }
                            ) {
                                students.forEach { std ->
                                    DropdownMenuItem(
                                        text = { Text("${std.name} • Class: ${std.className} (Roll #${std.rollNumber ?: std.rollNo})") },
                                        onClick = {
                                            selectedStudent = std
                                            studentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = examName,
                        onValueChange = { examName = it },
                        label = { Text("Exam Name *") },
                        placeholder = { Text("e.g. Mid-Term 2026, Annual Exam") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_exam_name")
                    )

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        placeholder = { Text("e.g. Mathematics, Science, Urdu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_exam_subject")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalMarksText,
                            onValueChange = { totalMarksText = it },
                            label = { Text("Total Marks") },
                            placeholder = { Text("100") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_exam_total_marks")
                        )

                        OutlinedTextField(
                            value = obtainedMarksText,
                            onValueChange = { obtainedMarksText = it },
                            label = { Text("Obtained Marks *") },
                            placeholder = { Text("75") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_exam_obtained_marks")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val total = totalMarksText.toDoubleOrNull() ?: 100.0
                        val obtained = obtainedMarksText.toDoubleOrNull() ?: 0.0
                        val pct = if (total > 0) (obtained / total) * 100 else 0.0
                        val grade = when {
                            pct >= 85 -> "A+"
                            pct >= 75 -> "A"
                            pct >= 65 -> "B"
                            pct >= 50 -> "C"
                            pct >= 40 -> "D"
                            else -> "F"
                        }

                        val student = selectedStudent
                        if (student != null) {
                            val entity = ExamResultEntity(
                                id = 0L,
                                businessId = activeBusiness?.id ?: 0L,
                                studentId = student.id,
                                studentName = student.name,
                                rollNumber = student.rollNumber ?: student.rollNo,
                                className = student.className,
                                examName = examName.trim(),
                                subject = subject.trim(),
                                totalMarks = total,
                                obtainedMarks = obtained,
                                grade = grade
                            )
                            onSaveExamResult(entity)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    modifier = Modifier.testTag("btn_save_exam_confirm")
                ) {
                    Text("Save Result")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (resultToDelete != null) {
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            title = { Text("Delete Exam Record") },
            text = { Text("Are you sure you want to delete the result of '${resultToDelete?.studentName}' for ${resultToDelete?.subject}?") },
            confirmButton = {
                Button(
                    onClick = {
                        resultToDelete?.id?.let { onDeleteExamResult(it) }
                        resultToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
