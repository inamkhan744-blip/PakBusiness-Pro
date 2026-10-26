package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Metadata associated with a database backup archive.
 */
data class BackupMetadata(
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val timestamp: Long,
    val formattedDate: String,
    val version: Int = 20,
    val isZipArchive: Boolean = true
)

/**
 * Result report following a backup restoration attempt.
 */
data class RestoreResult(
    val isSuccess: Boolean,
    val message: String,
    val restoredSizeBytes: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Production-ready utility for file-based Room database backup, export,
 * and restoration with WAL checkpointing, SAF (Storage Access Framework) support,
 * and share sheet export for cloud storage services (Google Drive, Dropbox, etc.).
 */
object DatabaseBackupUtil {

    private const val DB_NAME = "pakbusiness_pro.db"
    private const val BACKUP_DIR_NAME = "database_backups"

    /**
     * Retrieves the dedicated local directory for storing backup files safely.
     */
    fun getBackupDirectory(context: Context): File {
        val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, BACKUP_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Flushes the SQLite Write-Ahead Log (WAL) to ensure all pending data
     * is written directly into the primary .db file before copying.
     */
    suspend fun checkpointWal(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val writableDb = db.openHelper.writableDatabase
            writableDb.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                if (cursor.moveToFirst()) {
                    // Checkpoint complete
                }
            }
        } catch (e: Exception) {
            // Checkpoint error fallback
        }
    }

    /**
     * Exports a complete, consistent backup of the Room database to a local file
     * or a user-selected SAF Uri (Storage Access Framework / Google Drive / External storage).
     *
     * @param context Application context
     * @param destinationUri Optional user-selected Uri from SAF (CreateDocument)
     * @return Result containing BackupMetadata on success
     */
    suspend fun createBackup(
        context: Context,
        destinationUri: Uri? = null
    ): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        try {
            // 1. Flush any pending transactions from WAL into the primary database file
            checkpointWal(context)

            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(IllegalStateException("Database file not found: $DB_NAME"))
            }

            val timestamp = System.currentTimeMillis()
            val timeString = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestamp))
            val backupFileName = "PakBusiness_Backup_$timeString.pakdb"

            val backupDir = getBackupDirectory(context)
            val localBackupFile = File(backupDir, backupFileName)

            // 2. Package database files (main db, wal, shm) into a compressed archive
            val walFile = File(dbFile.parentFile, "$DB_NAME-wal")
            val shmFile = File(dbFile.parentFile, "$DB_NAME-shm")

            FileOutputStream(localBackupFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // Main DB
                    addFileToZip(zos, dbFile, DB_NAME)
                    // Optional WAL/SHM if present
                    if (walFile.exists() && walFile.length() > 0) {
                        addFileToZip(zos, walFile, "$DB_NAME-wal")
                    }
                    if (shmFile.exists() && shmFile.length() > 0) {
                        addFileToZip(zos, shmFile, "$DB_NAME-shm")
                    }
                }
            }

            // 3. If a target SAF Uri was provided, stream the archive directly to it
            if (destinationUri != null) {
                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    FileInputStream(localBackupFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val metadata = BackupMetadata(
                fileName = backupFileName,
                filePath = localBackupFile.absolutePath,
                sizeBytes = localBackupFile.length(),
                timestamp = timestamp,
                formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
            )

            Result.success(metadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restores the Room database from a backup file or a SAF content Uri.
     * Validates the archive and safely replaces the active SQLite file.
     *
     * @param context Application context
     * @param sourceUri Uri of the backup archive to restore from
     * @return Result containing RestoreResult
     */
    suspend fun restoreBackup(
        context: Context,
        sourceUri: Uri
    ): Result<RestoreResult> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val walFile = File(dbFile.parentFile, "$DB_NAME-wal")
            val shmFile = File(dbFile.parentFile, "$DB_NAME-shm")

            // 1. Read input stream from Uri into a temporary staging file
            val tempRestoreDir = File(context.cacheDir, "db_restore_temp")
            tempRestoreDir.deleteRecursively()
            tempRestoreDir.mkdirs()

            var hasValidMainDb = false
            var totalExtractedBytes = 0L

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
                ?: if (sourceUri.path != null) FileInputStream(File(sourceUri.path!!)) else null

            if (inputStream == null) {
                return@withContext Result.failure(IllegalArgumentException("Cannot open stream for Uri: $sourceUri"))
            }

            inputStream.use { rawIn ->
                // Check if it's a Zip archive or a raw SQLite file
                val isZip = sourceUri.toString().endsWith(".pakdb") ||
                    sourceUri.toString().endsWith(".zip")

                if (isZip) {
                    ZipInputStream(rawIn).use { zis ->
                        var entry: ZipEntry? = zis.nextEntry
                        while (entry != null) {
                            val entryFile = File(tempRestoreDir, entry.name)
                            if (entry.name == DB_NAME) {
                                hasValidMainDb = true
                            }
                            FileOutputStream(entryFile).use { fos ->
                                totalExtractedBytes += zis.copyTo(fos)
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                } else {
                    // Raw SQLite file direct copy
                    val directDbFile = File(tempRestoreDir, DB_NAME)
                    FileOutputStream(directDbFile).use { fos ->
                        totalExtractedBytes += rawIn.copyTo(fos)
                    }
                    hasValidMainDb = true
                }
            }

            if (!hasValidMainDb) {
                tempRestoreDir.deleteRecursively()
                return@withContext Result.failure(
                    IllegalArgumentException("Invalid backup archive: missing $DB_NAME")
                )
            }

            val stagedMainDb = File(tempRestoreDir, DB_NAME)
            if (!isValidSqliteHeader(stagedMainDb)) {
                tempRestoreDir.deleteRecursively()
                return@withContext Result.failure(
                    IllegalArgumentException("Corrupted backup file: SQLite header verification failed")
                )
            }

            // 2. Safely close active Room instance and reset singleton before swapping files
            try {
                AppDatabase.resetDatabaseInstance()
            } catch (_: Exception) {
            }

            // 3. Remove existing WAL and SHM files to prevent schema mismatch
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // 4. Overwrite main database file with the staged backup
            dbFile.parentFile?.mkdirs()
            stagedMainDb.copyTo(dbFile, overwrite = true)

            // Copy restored WAL/SHM if present in archive
            val stagedWal = File(tempRestoreDir, "$DB_NAME-wal")
            if (stagedWal.exists()) {
                stagedWal.copyTo(walFile, overwrite = true)
            }

            // Cleanup staging directory
            tempRestoreDir.deleteRecursively()

            // 5. Re-open database to verify connection integrity and reload singleton
            AppDatabase.getDatabase(context).openHelper.readableDatabase

            Result.success(
                RestoreResult(
                    isSuccess = true,
                    message = "Database restored successfully. Restored $totalExtractedBytes bytes.",
                    restoredSizeBytes = totalExtractedBytes
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lists all locally stored backup files sorted chronologically (newest first).
     */
    fun listLocalBackups(context: Context): List<BackupMetadata> {
        val dir = getBackupDirectory(context)
        val files = dir.listFiles { file ->
            file.isFile && (file.name.endsWith(".pakdb") || file.name.endsWith(".zip"))
        } ?: return emptyList()

        return files.sortedByDescending { it.lastModified() }.map { file ->
            BackupMetadata(
                fileName = file.name,
                filePath = file.absolutePath,
                sizeBytes = file.length(),
                timestamp = file.lastModified(),
                formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(file.lastModified()))
            )
        }
    }

    /**
     * Deletes a local backup file by name.
     */
    fun deleteLocalBackup(context: Context, fileName: String): Boolean {
        val file = File(getBackupDirectory(context), fileName)
        return if (file.exists()) file.delete() else false
    }

    /**
     * Shares a backup file via Android's system share sheet, enabling users
     * to save the backup directly to Google Drive, Dropbox, Gmail, or OneDrive.
     */
    fun shareBackup(context: Context, backupFile: File) {
        val authority = "${context.packageName}.provider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, backupFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "PakBusiness Database Backup: ${backupFile.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Save Backup to Cloud or Share")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Checks if the file starts with the standard SQLite 3 header ("SQLite format 3\000").
     */
    private fun isValidSqliteHeader(file: File): Boolean {
        if (!file.exists() || file.length() < 16) return false
        val buffer = ByteArray(16)
        FileInputStream(file).use { it.read(buffer) }
        val headerString = String(buffer)
        return headerString.startsWith("SQLite format 3")
    }

    /**
     * Helper to write a file into a ZipOutputStream.
     */
    private fun addFileToZip(zos: ZipOutputStream, file: File, entryName: String) {
        FileInputStream(file).use { fis ->
            val zipEntry = ZipEntry(entryName)
            zos.putNextEntry(zipEntry)
            fis.copyTo(zos)
            zos.closeEntry()
        }
    }
}
