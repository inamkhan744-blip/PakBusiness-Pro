package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.BusinessEntity
import com.example.data.PrescriptionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrescriptionPdfGenerator {

    /**
     * Generates a medical prescription PDF and returns the saved File.
     */
    fun generatePdf(
        context: Context,
        business: BusinessEntity?,
        prescription: PrescriptionEntity
    ): File? {
        val pdfDocument = PdfDocument()
        // Standard A4 dimensions: 595 x 842 points
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(prescription.createdAt))

        // Colors
        val primaryColor = Color.rgb(15, 118, 110) // Deep Medical Emerald
        val secondaryColor = Color.rgb(51, 65, 85) // Slate 700
        val lightBgColor = Color.rgb(241, 245, 249) // Slate 100
        val borderColor = Color.rgb(203, 213, 225) // Slate 300
        val textDark = Color.rgb(15, 23, 42) // Slate 900
        val textMuted = Color.rgb(100, 116, 139) // Slate 500

        var currentY = 40f
        val marginX = 40f
        val contentWidth = pageWidth - (marginX * 2)

        // 1. Clinic Header
        val clinicName = business?.name ?: "PakBusiness Healthcare Clinic"
        paint.color = primaryColor
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(clinicName.uppercase(), marginX, currentY, paint)

        currentY += 16f
        paint.color = textMuted
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val clinicSub = "${business?.address ?: "Medical Complex"} • Contact: ${business?.phone ?: "0300-0000000"}"
        canvas.drawText(clinicSub, marginX, currentY, paint)

        currentY += 10f
        // Header Divider
        paint.color = primaryColor
        paint.strokeWidth = 2.5f
        canvas.drawLine(marginX, currentY, marginX + contentWidth, currentY, paint)

        currentY += 24f

        // 2. Doctor Info & Rx ID row
        paint.color = textDark
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(prescription.doctorName, marginX, currentY, paint)

        // Right-aligned Date
        paint.color = textMuted
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val rxIdText = "Rx ID: #${prescription.id.toString().padStart(5, '0')} | $dateStr"
        val rxIdWidth = paint.measureText(rxIdText)
        canvas.drawText(rxIdText, marginX + contentWidth - rxIdWidth, currentY, paint)

        currentY += 14f
        paint.color = primaryColor
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val docDegree = prescription.doctorSpecialization
        canvas.drawText(docDegree, marginX, currentY, paint)

        currentY += 20f

        // 3. Patient Information Card Box
        val patientBoxHeight = 52f
        paint.color = lightBgColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(
            RectF(marginX, currentY, marginX + contentWidth, currentY + patientBoxHeight),
            8f, 8f, paint
        )
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(
            RectF(marginX, currentY, marginX + contentWidth, currentY + patientBoxHeight),
            8f, 8f, paint
        )

        paint.style = Paint.Style.FILL
        var pTextY = currentY + 20f
        paint.color = textMuted
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("PATIENT NAME:", marginX + 12f, pTextY, paint)

        paint.color = textDark
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(prescription.patientName, marginX + 90f, pTextY, paint)

        // Age / Gender
        paint.color = textMuted
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("AGE / GENDER:", marginX + 310f, pTextY, paint)

        paint.color = textDark
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${prescription.patientAge} Yrs / ${prescription.patientGender}", marginX + 390f, pTextY, paint)

        pTextY += 20f
        paint.color = textMuted
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("CONTACT:", marginX + 12f, pTextY, paint)

        paint.color = textDark
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(if (prescription.patientPhone.isNotBlank()) prescription.patientPhone else "N/A", marginX + 90f, pTextY, paint)

        currentY += patientBoxHeight + 14f

        // 4. Clinical Vitals (BP, Pulse, Temp, Weight)
        if (prescription.vitalsBp.isNotBlank() || prescription.vitalsPulse.isNotBlank() ||
            prescription.vitalsTemp.isNotBlank() || prescription.vitalsWeight.isNotBlank()
        ) {
            paint.color = Color.rgb(248, 250, 252)
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(
                RectF(marginX, currentY, marginX + contentWidth, currentY + 28f),
                6f, 6f, paint
            )
            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.8f
            canvas.drawRoundRect(
                RectF(marginX, currentY, marginX + contentWidth, currentY + 28f),
                6f, 6f, paint
            )

            paint.style = Paint.Style.FILL
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val vitalsString = "Vitals:  " +
                (if (prescription.vitalsBp.isNotBlank()) "BP: ${prescription.vitalsBp}  |  " else "") +
                (if (prescription.vitalsPulse.isNotBlank()) "Pulse: ${prescription.vitalsPulse}  |  " else "") +
                (if (prescription.vitalsTemp.isNotBlank()) "Temp: ${prescription.vitalsTemp}  |  " else "") +
                (if (prescription.vitalsWeight.isNotBlank()) "Weight: ${prescription.vitalsWeight}" else "")
            paint.color = secondaryColor
            canvas.drawText(vitalsString.trimEnd(' ', '|'), marginX + 10f, currentY + 18f, paint)

            currentY += 36f
        }

        // 5. Symptoms & Diagnosis
        if (prescription.diagnosis.isNotBlank() || prescription.symptoms.isNotBlank()) {
            if (prescription.diagnosis.isNotBlank()) {
                paint.color = textDark
                paint.textSize = 10.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Diagnosis: ", marginX, currentY, paint)

                paint.color = primaryColor
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(prescription.diagnosis, marginX + 65f, currentY, paint)
                currentY += 16f
            }
            if (prescription.symptoms.isNotBlank()) {
                paint.color = textMuted
                paint.textSize = 9.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText("Symptoms: ${prescription.symptoms}", marginX, currentY, paint)
                currentY += 16f
            }
            currentY += 6f
        }

        // 6. Medical Rx Symbol
        paint.color = primaryColor
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
        canvas.drawText("℞", marginX, currentY + 10f, paint)

        currentY += 26f

        // 7. Medicines Table
        val medicines = prescription.parseMedicines()
        if (medicines.isNotEmpty()) {
            // Table Header Background
            paint.color = lightBgColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(marginX, currentY, marginX + contentWidth, currentY + 22f, paint)

            // Table Header Text
            paint.color = textDark
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("#", marginX + 8f, currentY + 15f, paint)
            canvas.drawText("MEDICINE & FORM", marginX + 30f, currentY + 15f, paint)
            canvas.drawText("DOSAGE", marginX + 220f, currentY + 15f, paint)
            canvas.drawText("DURATION", marginX + 320f, currentY + 15f, paint)
            canvas.drawText("INSTRUCTIONS", marginX + 410f, currentY + 15f, paint)

            currentY += 24f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            medicines.forEachIndexed { index, med ->
                // Row divider
                paint.color = borderColor
                paint.strokeWidth = 0.6f
                canvas.drawLine(marginX, currentY + 20f, marginX + contentWidth, currentY + 20f, paint)

                paint.color = textDark
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("${index + 1}.", marginX + 8f, currentY + 14f, paint)
                canvas.drawText(med.name, marginX + 30f, currentY + 14f, paint)

                paint.color = secondaryColor
                paint.textSize = 9.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(med.dosage, marginX + 220f, currentY + 14f, paint)
                canvas.drawText(med.duration, marginX + 320f, currentY + 14f, paint)
                canvas.drawText(med.instructions, marginX + 410f, currentY + 14f, paint)

                currentY += 26f
            }
            currentY += 10f
        }

        // 8. Investigations / Lab Tests
        if (prescription.labTests.isNotBlank()) {
            paint.color = textDark
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Required Diagnostic Investigations:", marginX, currentY, paint)
            currentY += 14f

            paint.color = secondaryColor
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val lines = prescription.labTests.split("\n", ",")
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    canvas.drawText("• $trimmed", marginX + 10f, currentY, paint)
                    currentY += 14f
                }
            }
            currentY += 8f
        }

        // 9. Advice & Precautions
        if (prescription.advice.isNotBlank()) {
            paint.color = textDark
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Special Advice & Dietary Guidelines:", marginX, currentY, paint)
            currentY += 14f

            paint.color = secondaryColor
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val lines = prescription.advice.split("\n")
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    canvas.drawText("• $trimmed", marginX + 10f, currentY, paint)
                    currentY += 14f
                }
            }
            currentY += 8f
        }

        // 10. Follow-up
        if (prescription.followUpDays > 0) {
            paint.color = primaryColor
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Next Follow-up: After ${prescription.followUpDays} Days (or SOS if worsening symptoms)", marginX, currentY, paint)
        }

        // 11. Doctor Signature Area at bottom right
        val signY = pageHeight - 80f
        paint.color = borderColor
        paint.strokeWidth = 1f
        canvas.drawLine(marginX + contentWidth - 160f, signY, marginX + contentWidth, signY, paint)

        paint.color = textDark
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val sigLabel = "Doctor's Signature & Stamp"
        canvas.drawText(sigLabel, marginX + contentWidth - 150f, signY + 14f, paint)

        // 12. Bottom Page Disclaimer
        val footerY = pageHeight - 30f
        paint.color = borderColor
        paint.strokeWidth = 0.8f
        canvas.drawLine(marginX, footerY - 8f, marginX + contentWidth, footerY - 8f, paint)

        paint.color = textMuted
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val footerText = "Generated via PakBusiness Pro Medical Management System • Valid without physical stamp if digitally verified."
        canvas.drawText(footerText, marginX, footerY + 6f, paint)

        pdfDocument.finishPage(page)

        // Save PDF file to cache directory
        return try {
            val outputDir = File(context.cacheDir, "prescriptions").apply { mkdirs() }
            val pdfFile = File(outputDir, "Prescription_${prescription.patientName.replace(" ", "_")}_${prescription.id}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Triggers Android share sheet with the generated PDF.
     */
    fun sharePdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Prescription - ${pdfFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Prescription PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Opens the generated PDF in an external viewer.
     */
    fun viewPdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            // Fallback to share
            sharePdf(context, pdfFile)
        }
    }
}
