package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary
import com.example.util.BackupMetadata
import com.example.util.DatabaseBackupUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-featured Room Database Backup & Restore Utility component.
 * Allows business owners to:
 * 1. Create instant local backups (.pakdb compressed archive with WAL checkpointing)
 * 2. Export & Save database backups to user-selected device storage / Google Drive / SD Card via SAF
 * 3. Import & Restore from any .pakdb, .zip or raw SQLite file with pre-validation
 * 4. Share backups via WhatsApp, Gmail, Bluetooth, or Cloud Drive
 * 5. Manage local backup history with single-click restore and deletion
 */
@Composable
fun RoomDatabaseBackupRestoreView(
    activeBusiness: BusinessEntity?,
    onDataRestored: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var operationStatusMsg by remember { mutableStateOf<String?>(null) }
    var operationErrorMsg by remember { mutableStateOf<String?>(null) }

    // List of local backups
    val localBackups = remember { mutableStateListOf<BackupMetadata>() }

    // Dialogs
    var backupToDelete by remember { mutableStateOf<BackupMetadata?>(null) }
    var backupToRestore by remember { mutableStateOf<BackupMetadata?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    fun refreshBackupList() {
        localBackups.clear()
        localBackups.addAll(DatabaseBackupUtil.listLocalBackups(context))
    }

    LaunchedEffect(Unit) {
        refreshBackupList()
    }

    // SAF (Storage Access Framework) file creator for exporting database file
    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { destinationUri: Uri? ->
        if (destinationUri != null) {
            scope.launch {
                isProcessing = true
                operationStatusMsg = null
                operationErrorMsg = null
                val result = DatabaseBackupUtil.createBackup(context, destinationUri)
                isProcessing = false
                result.onSuccess { meta ->
                    refreshBackupList()
                    operationStatusMsg = "✓ Database exported successfully: ${meta.fileName} (${formatFileSize(meta.sizeBytes)})"
                    Toast.makeText(context, "Database exported & saved to storage!", Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    operationErrorMsg = "Export failed: ${err.localizedMessage ?: "Unknown error"}"
                }
            }
        }
    }

    // SAF file picker for importing database file
    val importDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { sourceUri: Uri? ->
        if (sourceUri != null) {
            pendingImportUri = sourceUri
        }
    }

    fun executeRestore(uri: Uri) {
        scope.launch {
            isProcessing = true
            operationStatusMsg = null
            operationErrorMsg = null
            val result = DatabaseBackupUtil.restoreBackup(context, uri)
            isProcessing = false
            result.onSuccess { report ->
                refreshBackupList()
                operationStatusMsg = "✓ Database restored successfully! All business records updated."
                Toast.makeText(context, "Restore complete! Reloading app data...", Toast.LENGTH_LONG).show()
                onDataRestored()
            }.onFailure { err ->
                operationErrorMsg = "Restore failed: ${err.localizedMessage ?: "Invalid or corrupted backup file"}"
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("room_database_backup_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card: Offline Security & Integrity
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldDark),
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PakGoldSecondary.copy(alpha = 0.25f),
                            contentColor = Color(0xFFFFDFBA)
                        ) {
                            Text(
                                text = "ROOM DATABASE ENGINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { refreshBackupList() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                    }

                    Text(
                        text = "Complete Business Database Backup & Restore",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "All your business profiles, POS invoices, inventory catalog, customer Khata, school fees, clinic Rx, and restaurant KOT tables are archived in a single secure SQLite file (.pakdb).",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Enterprise: ${activeBusiness?.name ?: "All Businesses"}",
                            color = Color(0xFFFFD54F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${localBackups.size} Saved Archives",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Status or Error Banners
        if (isProcessing) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PakEmeraldPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Column {
                            Text("Processing Database Operation...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Flushing SQLite WAL checkpoint and validating database integrity", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (operationStatusMsg != null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                        Text(
                            text = operationStatusMsg!!,
                            color = Color(0xFF15803D),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        if (operationErrorMsg != null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFDC2626))
                        Text(
                            text = operationErrorMsg!!,
                            color = Color(0xFFB91C1C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Primary Actions: Create Backup, Save to File, Import File
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Backup & Export Operations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    // 1. Create Instant Local Backup
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                operationStatusMsg = null
                                operationErrorMsg = null
                                val result = DatabaseBackupUtil.createBackup(context)
                                isProcessing = false
                                result.onSuccess { meta ->
                                    refreshBackupList()
                                    operationStatusMsg = "✓ Local backup created: ${meta.fileName} (${formatFileSize(meta.sizeBytes)})"
                                    Toast.makeText(context, "Backup archive saved successfully!", Toast.LENGTH_SHORT).show()
                                }.onFailure { err ->
                                    operationErrorMsg = "Failed to create backup: ${err.localizedMessage ?: "Unknown error"}"
                                }
                            }
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_create_local_backup")
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Create Instant Local Backup (.pakdb)", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 2. Export / Save Document to Storage
                        OutlinedButton(
                            onClick = {
                                val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                                exportDocumentLauncher.launch("PakBusiness_Backup_$timeStr.pakdb")
                            },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_document_saf")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save File (SAF)", fontSize = 12.sp)
                        }

                        // 3. Import / Restore Document from Storage
                        OutlinedButton(
                            onClick = {
                                importDocumentLauncher.launch(
                                    arrayOf("application/octet-stream", "application/zip", "*/*")
                                )
                            },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_import_document_saf")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open & Restore", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Local Backup Archives
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Local Backup Archives (${localBackups.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { refreshBackupList() }) {
                    Text("Refresh", fontSize = 12.sp, color = PakEmeraldPrimary)
                }
            }
        }

        if (localBackups.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No Local Backups Found Yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap 'Create Instant Local Backup' above to generate your first complete database snapshot.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(localBackups, key = { it.filePath }) { backup ->
                BackupArchiveCard(
                    metadata = backup,
                    onRestore = { backupToRestore = backup },
                    onShare = {
                        val file = File(backup.filePath)
                        if (file.exists()) {
                            DatabaseBackupUtil.shareBackup(context, file)
                        } else {
                            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDelete = { backupToDelete = backup }
                )
            }
        }
    }

    // Confirmation Dialog: Restore from Local Backup
    if (backupToRestore != null) {
        val target = backupToRestore!!
        AlertDialog(
            onDismissRequest = { backupToRestore = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = PakGoldSecondary) },
            title = { Text("Restore Business Database?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Are you sure you want to restore from '${target.fileName}' created on ${target.formattedDate}?",
                        fontSize = 13.sp
                    )
                    Text(
                        "⚠️ This will overwrite the current active database with the contents of this archive.",
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = File(target.filePath)
                        backupToRestore = null
                        executeRestore(Uri.fromFile(file))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Confirm Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupToRestore = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog: Import from Selected External File Uri
    if (pendingImportUri != null) {
        val uri = pendingImportUri!!
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            icon = { Icon(Icons.Default.CloudDownload, contentDescription = null, tint = PakEmeraldPrimary) },
            title = { Text("Import & Restore External Backup?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "You selected a database archive file. Do you want to verify and restore all business records from it?",
                        fontSize = 13.sp
                    )
                    Text(
                        "File source: $uri",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingImportUri = null
                        executeRestore(uri)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary)
                ) {
                    Text("Verify & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog: Delete Backup
    if (backupToDelete != null) {
        val target = backupToDelete!!
        AlertDialog(
            onDismissRequest = { backupToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Delete Backup Archive?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently delete '${target.fileName}'? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        DatabaseBackupUtil.deleteLocalBackup(context, target.fileName)
                        backupToDelete = null
                        refreshBackupList()
                        Toast.makeText(context, "Backup archive deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BackupArchiveCard(
    metadata: BackupMetadata,
    onRestore: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PakEmeraldContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = PakEmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = metadata.fileName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${metadata.formattedDate} • ${formatFileSize(metadata.sizeBytes)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE2E8F0)
                ) {
                    Text(
                        text = "ROOM DB",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onShare,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onRestore,
                        colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
