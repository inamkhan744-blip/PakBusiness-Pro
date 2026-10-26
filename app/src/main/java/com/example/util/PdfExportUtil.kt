package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data structures for Prescription (Rx) document generation.
 */
data class PrescribedMedicineItem(
    val name: String,
    val dosage: String = "",       // e.g. "1+0+1" or "500mg"
    val duration: String = "",     // e.g. "5 Days"
    val instruction: String = ""   // e.g. "After food"
)

data class PrescriptionExportData(
    val rxNumber: String = "RX-${System.currentTimeMillis() % 100000}",
    val date: String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
    val clinicName: String = "Healthcare Clinic",
    val clinicAddress: String = "",
    val clinicPhone: String = "",
    val doctorName: String = "Dr. Medical Practitioner",
    val doctorSpecialty: String = "General Physician",
    val doctorDegree: String = "MBBS",
    val patientName: String,
    val patientAge: String = "",
    val patientGender: String = "",
    val patientPhone: String = "",
    val bloodPressure: String = "",
    val pulse: String = "",
    val temperature: String = "",
    val weight: String = "",
    val diagnosis: String = "",
    val medicines: List<PrescribedMedicineItem> = emptyList(),
    val advice: String = "",
    val followUpDate: String = ""
)

/**
 * Data structures for Shop / POS / Clinic billing invoice generation.
 */
data class InvoiceExportItem(
    val name: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double = quantity * unitPrice
)

data class InvoiceExportData(
    val invoiceNumber: String = "INV-${System.currentTimeMillis() % 100000}",
    val date: String = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()),
    val businessName: String = "PakBusiness Store",
    val businessAddress: String = "",
    val businessPhone: String = "",
    val businessTaxId: String = "",
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val items: List<InvoiceExportItem> = emptyList(),
    val subtotal: Double = items.sumOf { it.total },
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double = subtotal - discount + tax,
    val paymentMethod: String = "Cash",
    val amountPaid: Double = grandTotal,
    val changeDue: Double = 0.0,
    val footerNote: String = "Thank you for your business! Please visit again."
)

/**
 * Utility class to generate and export Rx / Invoice data to PDF documents,
 * enabling the clinic and shop modules to print receipts and share digital invoices.
 */
object PdfExportUtil {

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842
    private const val MARGIN = 40f

    /**
     * Generates a formal medical Prescription (Rx) PDF file.
     * @return Generated PDF File stored in context.cacheDir
     */
    fun generatePrescriptionPdf(context: Context, data: PrescriptionExportData): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val primaryPaint = Paint().apply {
            color = Color.rgb(18, 89, 74) // Deep teal medical branding
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 10f
            isAntiAlias = true
        }
        val titlePaint = Paint().apply {
            color = Color.rgb(18, 89, 74)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subtlePaint = Paint().apply {
            color = Color.rgb(117, 117, 117)
            textSize = 9f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(220, 224, 230)
            strokeWidth = 1f
        }

        var y = MARGIN + 10f

        // Top Header Banner
        canvas.drawText(data.clinicName, MARGIN, y + 10f, titlePaint)
        y += 26f

        if (data.clinicAddress.isNotBlank()) {
            canvas.drawText(data.clinicAddress, MARGIN, y, subtlePaint)
            y += 13f
        }
        if (data.clinicPhone.isNotBlank()) {
            canvas.drawText("Tel: ${data.clinicPhone}", MARGIN, y, subtlePaint)
            y += 13f
        }

        // Doctor Info (Right aligned header)
        val docPaint = Paint(boldPaint).apply {
            color = Color.rgb(18, 89, 74)
            textSize = 13f
        }
        val rightX = A4_WIDTH - MARGIN
        canvas.drawText(data.doctorName, rightX - docPaint.measureText(data.doctorName), MARGIN + 10f, docPaint)
        val docSub = "${data.doctorDegree} - ${data.doctorSpecialty}"
        canvas.drawText(docSub, rightX - subtlePaint.measureText(docSub), MARGIN + 25f, subtlePaint)

        y += 10f
        // Divider line
        primaryPaint.strokeWidth = 2.5f
        canvas.drawLine(MARGIN, y, rightX, y, primaryPaint)
        y += 16f

        // Patient Details Card
        boldPaint.textSize = 10.5f
        canvas.drawText("Patient: ${data.patientName}", MARGIN, y, boldPaint)
        val ageGender = listOfNotNull(
            data.patientAge.takeIf { it.isNotBlank() }?.let { "$it Yrs" },
            data.patientGender.takeIf { it.isNotBlank() }
        ).joinToString(", ")
        if (ageGender.isNotBlank()) {
            canvas.drawText("Age/Gender: $ageGender", MARGIN + 220f, y, textPaint)
        }
        val dateText = "Date: ${data.date}"
        canvas.drawText(dateText, rightX - textPaint.measureText(dateText), y, textPaint)
        y += 16f

        if (data.patientPhone.isNotBlank()) {
            canvas.drawText("Phone: ${data.patientPhone}", MARGIN, y, subtlePaint)
        }
        val rxNumText = "Rx #: ${data.rxNumber}"
        canvas.drawText(rxNumText, rightX - subtlePaint.measureText(rxNumText), y, subtlePaint)
        y += 12f

        canvas.drawLine(MARGIN, y, rightX, y, linePaint)
        y += 18f

        // Vitals Section (if available)
        val vitalsList = mutableListOf<String>()
        if (data.bloodPressure.isNotBlank()) vitalsList.add("BP: ${data.bloodPressure}")
        if (data.pulse.isNotBlank()) vitalsList.add("Pulse: ${data.pulse} bpm")
        if (data.temperature.isNotBlank()) vitalsList.add("Temp: ${data.temperature}")
        if (data.weight.isNotBlank()) vitalsList.add("Weight: ${data.weight} kg")

        if (vitalsList.isNotEmpty()) {
            canvas.drawText("Vitals: " + vitalsList.joinToString("  |  "), MARGIN, y, boldPaint)
            y += 16f
        }

        if (data.diagnosis.isNotBlank()) {
            canvas.drawText("Diagnosis: ${data.diagnosis}", MARGIN, y, boldPaint)
            y += 20f
        }

        // Rx Symbol
        val rxSymbolPaint = Paint().apply {
            color = Color.rgb(18, 89, 74)
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("℞", MARGIN, y + 6f, rxSymbolPaint)
        y += 24f

        // Medicines Table Header
        val colMedX = MARGIN + 25f
        val colDosageX = MARGIN + 260f
        val colDurationX = MARGIN + 360f
        val colInstX = MARGIN + 440f

        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(240, 246, 244)
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, y - 12f, rightX, y + 6f, tableHeaderPaint)

        boldPaint.textSize = 9.5f
        canvas.drawText("Medicine Name", colMedX, y, boldPaint)
        canvas.drawText("Dosage", colDosageX, y, boldPaint)
        canvas.drawText("Duration", colDurationX, y, boldPaint)
        canvas.drawText("Instructions", colInstX, y, boldPaint)
        y += 16f

        // Medicines List
        textPaint.textSize = 10f
        data.medicines.forEachIndexed { index, med ->
            canvas.drawText("${index + 1}. ${med.name}", colMedX, y, boldPaint)
            canvas.drawText(med.dosage, colDosageX, y, textPaint)
            canvas.drawText(med.duration, colDurationX, y, textPaint)
            canvas.drawText(med.instruction, colInstX, y, textPaint)
            y += 18f
        }

        if (data.medicines.isEmpty()) {
            canvas.drawText("No specific medications prescribed.", colMedX, y, subtlePaint)
            y += 24f
        } else {
            y += 14f
        }

        // Advice / Instructions
        if (data.advice.isNotBlank()) {
            canvas.drawLine(MARGIN, y, rightX, y, linePaint)
            y += 16f
            canvas.drawText("General Advice / Instructions:", MARGIN, y, boldPaint)
            y += 14f
            canvas.drawText(data.advice, MARGIN + 10f, y, textPaint)
            y += 20f
        }

        // Follow up
        if (data.followUpDate.isNotBlank()) {
            canvas.drawText("Follow up: ${data.followUpDate}", MARGIN, y, boldPaint)
        }

        // Signature area at the bottom
        val sigY = A4_HEIGHT - MARGIN - 40f
        canvas.drawLine(rightX - 160f, sigY, rightX, sigY, linePaint)
        val sigText = "Doctor's Signature / Stamp"
        canvas.drawText(sigText, rightX - 150f, sigY + 14f, subtlePaint)

        // Footer notice
        val footerNotice = "Generated via PakBusiness Pro Medical Suite - Not valid without signature"
        canvas.drawText(footerNotice, (A4_WIDTH - subtlePaint.measureText(footerNotice)) / 2f, A4_HEIGHT - MARGIN, subtlePaint)

        document.finishPage(page)

        val outputFile = File(context.cacheDir, "Prescription_${data.rxNumber}.pdf")
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()
        return outputFile
    }

    /**
     * Generates a commercial Shop / POS / Clinic billing Invoice PDF file.
     * @return Generated PDF File stored in context.cacheDir
     */
    fun generateInvoicePdf(context: Context, data: InvoiceExportData): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val primaryColor = Color.rgb(27, 78, 155) // Corporate blue branding
        val titlePaint = Paint().apply {
            color = primaryColor
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 10f
            isAntiAlias = true
        }
        val subtlePaint = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 9f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(225, 228, 232)
            strokeWidth = 1f
        }

        val rightX = A4_WIDTH - MARGIN
        var y = MARGIN + 10f

        // Company Header (Left)
        canvas.drawText(data.businessName, MARGIN, y + 10f, titlePaint)
        y += 26f
        if (data.businessAddress.isNotBlank()) {
            canvas.drawText(data.businessAddress, MARGIN, y, subtlePaint)
            y += 13f
        }
        if (data.businessPhone.isNotBlank()) {
            canvas.drawText("Phone: ${data.businessPhone}", MARGIN, y, subtlePaint)
            y += 13f
        }
        if (data.businessTaxId.isNotBlank()) {
            canvas.drawText("NTN/Tax ID: ${data.businessTaxId}", MARGIN, y, subtlePaint)
            y += 13f
        }

        // Invoice Badge (Right)
        val invoiceBadgePaint = Paint().apply {
            color = primaryColor
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val invTitle = "SALES INVOICE"
        canvas.drawText(invTitle, rightX - invoiceBadgePaint.measureText(invTitle), MARGIN + 10f, invoiceBadgePaint)
        val invNoText = "Invoice #: ${data.invoiceNumber}"
        canvas.drawText(invNoText, rightX - headerPaint.measureText(invNoText), MARGIN + 26f, headerPaint)
        val dateText = "Date: ${data.date}"
        canvas.drawText(dateText, rightX - subtlePaint.measureText(dateText), MARGIN + 40f, subtlePaint)

        y = maxOf(y, MARGIN + 55f)
        val brandLine = Paint().apply {
            color = primaryColor
            strokeWidth = 2.5f
        }
        canvas.drawLine(MARGIN, y, rightX, y, brandLine)
        y += 16f

        // Bill To section
        canvas.drawText("Billed To:", MARGIN, y, headerPaint)
        y += 14f
        canvas.drawText(data.customerName, MARGIN, y, textPaint)
        if (data.customerPhone.isNotBlank()) {
            canvas.drawText("Contact: ${data.customerPhone}", MARGIN + 200f, y, textPaint)
        }
        val paymentMethodText = "Payment: ${data.paymentMethod}"
        canvas.drawText(paymentMethodText, rightX - textPaint.measureText(paymentMethodText), y, textPaint)
        y += 18f

        // Items Table Header
        val colSnoX = MARGIN + 8f
        val colItemX = MARGIN + 40f
        val colQtyX = MARGIN + 310f
        val colPriceX = MARGIN + 380f
        val colTotalX = rightX - 10f

        val tableBg = Paint().apply {
            color = Color.rgb(243, 246, 250)
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, y - 12f, rightX, y + 8f, tableBg)

        canvas.drawText("#", colSnoX, y, headerPaint)
        canvas.drawText("Item / Description", colItemX, y, headerPaint)
        canvas.drawText("Qty", colQtyX, y, headerPaint)
        canvas.drawText("Unit Price", colPriceX, y, headerPaint)
        val totalHead = "Total (PKR)"
        canvas.drawText(totalHead, colTotalX - headerPaint.measureText(totalHead), y, headerPaint)
        y += 18f

        // Table Rows
        data.items.forEachIndexed { index, item ->
            canvas.drawText("${index + 1}", colSnoX, y, subtlePaint)
            canvas.drawText(item.name, colItemX, y, textPaint)
            val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format(Locale.US, "%.2f", item.quantity)
            canvas.drawText(qtyStr, colQtyX, y, textPaint)
            val priceStr = String.format(Locale.US, "%.2f", item.unitPrice)
            canvas.drawText(priceStr, colPriceX, y, textPaint)
            val lineTotalStr = String.format(Locale.US, "%.2f", item.total)
            canvas.drawText(lineTotalStr, colTotalX - textPaint.measureText(lineTotalStr), y, textPaint)

            y += 16f
            canvas.drawLine(MARGIN, y - 6f, rightX, y - 6f, linePaint)
        }

        y += 10f

        // Financial Totals block (Right aligned)
        val summaryX = A4_WIDTH - MARGIN - 180f
        val summaryValX = rightX - 10f

        fun drawSummaryRow(label: String, value: Double, isBold: Boolean = false, isHighlight: Boolean = false) {
            val p = if (isBold) headerPaint else textPaint
            if (isHighlight) {
                val highlightBg = Paint().apply {
                    color = Color.rgb(235, 243, 255)
                    style = Paint.Style.FILL
                }
                canvas.drawRect(summaryX - 10f, y - 12f, rightX, y + 6f, highlightBg)
            }
            canvas.drawText(label, summaryX, y, p)
            val formatted = "PKR " + String.format(Locale.US, "%,.2f", value)
            canvas.drawText(formatted, summaryValX - p.measureText(formatted), y, p)
            y += 16f
        }

        drawSummaryRow("Subtotal:", data.subtotal)
        if (data.discount > 0.0) {
            drawSummaryRow("Discount:", data.discount)
        }
        if (data.tax > 0.0) {
            drawSummaryRow("Tax / GST:", data.tax)
        }
        drawSummaryRow("Grand Total:", data.grandTotal, isBold = true, isHighlight = true)

        y += 6f
        drawSummaryRow("Amount Paid:", data.amountPaid)
        if (data.changeDue > 0.0) {
            drawSummaryRow("Change Returned:", data.changeDue)
        }

        // Footer Note
        val footerY = A4_HEIGHT - MARGIN - 30f
        canvas.drawLine(MARGIN, footerY, rightX, footerY, linePaint)
        val noteText = data.footerNote
        canvas.drawText(noteText, (A4_WIDTH - textPaint.measureText(noteText)) / 2f, footerY + 16f, textPaint)

        val legal = "PakBusiness Pro POS • Computer Generated Official Receipt"
        canvas.drawText(legal, (A4_WIDTH - subtlePaint.measureText(legal)) / 2f, A4_HEIGHT - MARGIN, subtlePaint)

        document.finishPage(page)

        val outputFile = File(context.cacheDir, "Invoice_${data.invoiceNumber}.pdf")
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()
        return outputFile
    }

    /**
     * Sends the PDF file directly to the system PrintManager dialog or thermal/connected printer.
     */
    fun printPdf(context: Context, pdfFile: File, jobName: String = "Print Document") {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                var input: FileInputStream? = null
                var output: FileOutputStream? = null
                try {
                    input = FileInputStream(pdfFile)
                    output = FileOutputStream(destination?.fileDescriptor)
                    val buf = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buf).also { bytesRead = it } > 0) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onWriteCancelled()
                            return
                        }
                        output.write(buf, 0, bytesRead)
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    try { input?.close() } catch (_: IOException) {}
                    try { output?.close() } catch (_: IOException) {}
                }
            }
        }

        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .build()

        printManager.print(jobName, printAdapter, attributes)
    }

    /**
     * Shares the generated PDF file through Android's system share sheet (WhatsApp, Email, etc.).
     */
    fun sharePdf(context: Context, pdfFile: File, subject: String = "Receipt / Document") {
        val authority = "${context.packageName}.provider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Receipt / Rx")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Opens the generated PDF in the system default PDF viewer app.
     */
    fun openPdf(context: Context, pdfFile: File) {
        val authority = "${context.packageName}.provider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(viewIntent, "Open PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Generates and immediately opens the system print dialog for a Prescription (Rx).
     */
    fun printPrescription(context: Context, data: PrescriptionExportData) {
        val pdfFile = generatePrescriptionPdf(context, data)
        printPdf(context, pdfFile, "Prescription_${data.rxNumber}")
    }

    /**
     * Generates and immediately opens the system print dialog for a Shop/POS Invoice.
     */
    fun printInvoice(context: Context, data: InvoiceExportData) {
        val pdfFile = generateInvoicePdf(context, data)
        printPdf(context, pdfFile, "Invoice_${data.invoiceNumber}")
    }

    /**
     * Generates and shares a Prescription (Rx) PDF file via system share sheet.
     */
    fun sharePrescription(context: Context, data: PrescriptionExportData) {
        val pdfFile = generatePrescriptionPdf(context, data)
        sharePdf(context, pdfFile, "Prescription - ${data.patientName} (${data.rxNumber})")
    }

    /**
     * Generates and shares an Invoice PDF file via system share sheet.
     */
    fun shareInvoice(context: Context, data: InvoiceExportData) {
        val pdfFile = generateInvoicePdf(context, data)
        sharePdf(context, pdfFile, "Invoice #${data.invoiceNumber} - ${data.businessName}")
    }
}

typealias RxPdfUtil = PdfExportUtil
typealias InvoicePdfUtil = PdfExportUtil
typealias ReceiptPdfExporter = PdfExportUtil
typealias PdfDocumentUtil = PdfExportUtil
