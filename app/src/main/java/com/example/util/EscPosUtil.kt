package com.example.util

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Supported Thermal Receipt Paper Sizes.
 * 58mm (2-inch mobile Bluetooth printers): 32 characters per line.
 * 80mm (3-inch POS desktop / kitchen printers): 48 characters per line.
 */
enum class EscPosPaperSize(val columns: Int, val label: String, val widthMm: Int) {
    WIDTH_58MM(columns = 32, label = "58mm (2-Inch / 32 Cols)", widthMm = 58),
    WIDTH_80MM(columns = 48, label = "80mm (3-Inch / 48 Cols)", widthMm = 80)
}

data class KotPrintItem(
    val name: String,
    val quantity: Int,
    val instructions: String = ""
)

/**
 * Standard ESC/POS Thermal Command and Byte Stream Generator.
 * Fully compatible with ESC/POS mobile & desktop Bluetooth thermal printers
 * (Sunmi, Zjiang ZJ-58, Goojprt PT-210, Xprinter, Epson TM series, Rongta, etc.).
 */
object EscPosUtil {

    // ESC/POS Command Constants
    val CMD_INIT = byteArrayOf(0x1B, 0x40) // ESC @
    val CMD_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00) // ESC a 0
    val CMD_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01) // ESC a 1
    val CMD_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02) // ESC a 2
    val CMD_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01) // ESC E 1
    val CMD_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00) // ESC E 0
    val CMD_UNDERLINE_ON = byteArrayOf(0x1B, 0x2D, 0x01) // ESC - 1
    val CMD_UNDERLINE_OFF = byteArrayOf(0x1B, 0x2D, 0x00) // ESC - 0
    val CMD_TEXT_NORMAL = byteArrayOf(0x1D, 0x21, 0x00) // GS ! 0
    val CMD_TEXT_DOUBLE_HEIGHT = byteArrayOf(0x1D, 0x21, 0x01) // GS ! 1
    val CMD_TEXT_DOUBLE_WIDTH = byteArrayOf(0x1D, 0x21, 0x10) // GS ! 16
    val CMD_TEXT_DOUBLE_BOTH = byteArrayOf(0x1D, 0x21, 0x11) // GS ! 17 (Double width & height)
    val CMD_FEED_3_LINES = byteArrayOf(0x1B, 0x64, 0x03) // ESC d 3
    val CMD_FEED_5_LINES = byteArrayOf(0x1B, 0x64, 0x05) // ESC d 5
    val CMD_PARTIAL_CUT = byteArrayOf(0x1D, 0x56, 0x42, 0x00) // GS V 66 0
    val LF = 0x0A.toByte()

    private val CHARSET_ASCII: Charset = Charset.forName("US-ASCII")
    private val CHARSET_CP864: Charset = try {
        Charset.forName("windows-1256")
    } catch (_: Exception) {
        Charset.defaultCharset()
    }

    /**
     * Formats two columns across the specified width (e.g. "Item Name" aligned left, "Amount" aligned right).
     */
    fun formatTwoColumns(left: String, right: String, totalCols: Int): String {
        val maxLeftLen = totalCols - right.length - 1
        val safeLeft = if (left.length > maxLeftLen && maxLeftLen > 3) {
            left.take(maxLeftLen - 1) + "."
        } else {
            left
        }
        val spaces = totalCols - safeLeft.length - right.length
        return if (spaces > 0) {
            safeLeft + " ".repeat(spaces) + right
        } else {
            "$safeLeft $right"
        }
    }

    /**
     * Formats three columns (e.g. "2x" + "Chicken Karahi" + "1,200").
     */
    fun formatThreeColumns(col1: String, col2: String, col3: String, totalCols: Int): String {
        val col1Padded = "$col1 "
        val remSpace = totalCols - col1Padded.length - col3.length - 1
        val safeCol2 = if (col2.length > remSpace && remSpace > 3) {
            col2.take(remSpace - 1) + "."
        } else {
            col2
        }
        val spaces = totalCols - col1Padded.length - safeCol2.length - col3.length
        return if (spaces > 0) {
            col1Padded + safeCol2 + " ".repeat(spaces) + col3
        } else {
            "$col1Padded$safeCol2 $col3"
        }
    }

    /**
     * Centers text within the total columns.
     */
    fun centerText(text: String, totalCols: Int): String {
        if (text.length >= totalCols) return text.take(totalCols)
        val leftPad = (totalCols - text.length) / 2
        val rightPad = totalCols - text.length - leftPad
        return " ".repeat(leftPad) + text + " ".repeat(rightPad)
    }

    /**
     * Builds a clean horizontal separator line.
     */
    fun dividerLine(char: Char = '-', totalCols: Int): String {
        return char.toString().repeat(totalCols)
    }

    /**
     * Generates ESC/POS byte sequence for a Retail / POS Customer Receipt.
     */
    fun generatePosReceiptBytes(
        invoice: InvoiceExportData,
        paperSize: EscPosPaperSize,
        cashierName: String? = null,
        taxNumber: String? = null
    ): ByteArray {
        val cols = paperSize.columns
        val stream = ByteArrayOutputStream()

        fun writeBytes(bytes: ByteArray) = stream.write(bytes)
        fun writeText(text: String) {
            stream.write(text.toByteArray(CHARSET_CP864))
            stream.write(LF.toInt())
        }

        // 1. Initialize Printer
        writeBytes(CMD_INIT)

        // 2. Business Header (Center, Double Size, Bold)
        writeBytes(CMD_ALIGN_CENTER)
        writeBytes(CMD_BOLD_ON)
        writeBytes(CMD_TEXT_DOUBLE_BOTH)
        writeText(invoice.businessName)

        // Address & Phone (Normal, Center)
        writeBytes(CMD_TEXT_NORMAL)
        writeBytes(CMD_BOLD_OFF)
        if (invoice.businessAddress.isNotBlank()) {
            writeText(invoice.businessAddress)
        }
        if (invoice.businessPhone.isNotBlank()) {
            writeText("Tel: ${invoice.businessPhone}")
        }
        val ntn = taxNumber ?: invoice.businessTaxId
        if (ntn.isNotBlank()) {
            writeText("NTN / STRN: $ntn")
        }

        writeBytes(CMD_BOLD_ON)
        writeText("*** CASH SALE RECEIPT ***")
        writeBytes(CMD_BOLD_OFF)

        // 3. Metadata (Left Aligned)
        writeBytes(CMD_ALIGN_LEFT)
        writeText(dividerLine('-', cols))
        writeText(formatTwoColumns("Invoice: ${invoice.invoiceNumber}", "", cols))
        writeText(formatTwoColumns("Date: ${invoice.date}", "", cols))
        if (!cashierName.isNullOrBlank()) {
            writeText(formatTwoColumns("Cashier: $cashierName", "", cols))
        }
        if (invoice.customerName.isNotBlank() && invoice.customerName != "Walk-in Customer") {
            writeText(formatTwoColumns("Customer: ${invoice.customerName}", "", cols))
        }
        if (invoice.customerPhone.isNotBlank()) {
            writeText(formatTwoColumns("Phone: ${invoice.customerPhone}", "", cols))
        }
        writeText(dividerLine('-', cols))

        // 4. Line Items Table Header
        writeBytes(CMD_BOLD_ON)
        writeText(formatThreeColumns("QTY", "ITEM DESCRIPTION", "TOTAL", cols))
        writeBytes(CMD_BOLD_OFF)
        writeText(dividerLine('-', cols))

        // Line Items
        for (item in invoice.items) {
            val qtyStr = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()}x" else "${item.quantity}x"
            val totalStr = String.format(Locale.getDefault(), "%,.0f", item.total)
            writeText(formatThreeColumns(qtyStr, item.name, totalStr, cols))
            // Rate detail row if quantity > 1
            if (item.quantity > 1.0) {
                val rateDetail = "   @ ${String.format(Locale.getDefault(), "%,.0f", item.unitPrice)} each"
                writeText(rateDetail)
            }
        }
        writeText(dividerLine('-', cols))

        // 5. Totals Section
        writeText(formatTwoColumns("Subtotal:", String.format(Locale.getDefault(), "%,.0f", invoice.subtotal), cols))
        if (invoice.discount > 0.0) {
            writeText(formatTwoColumns("Discount (-):", String.format(Locale.getDefault(), "%,.0f", invoice.discount), cols))
        }
        if (invoice.tax > 0.0) {
            writeText(formatTwoColumns("GST / Tax (+):", String.format(Locale.getDefault(), "%,.0f", invoice.tax), cols))
        }

        // Grand Total (Bold Double Height)
        writeText(dividerLine('=', cols))
        writeBytes(CMD_BOLD_ON)
        writeBytes(CMD_TEXT_DOUBLE_HEIGHT)
        writeText(formatTwoColumns("TOTAL:", "PKR " + String.format(Locale.getDefault(), "%,.0f", invoice.grandTotal), cols))
        writeBytes(CMD_TEXT_NORMAL)
        writeBytes(CMD_BOLD_OFF)
        writeText(dividerLine('=', cols))

        // 6. Payment Info
        writeText(formatTwoColumns("Payment Method:", invoice.paymentMethod, cols))
        if (invoice.amountPaid > 0.0) {
            writeText(formatTwoColumns("Amount Tendered:", String.format(Locale.getDefault(), "%,.0f", invoice.amountPaid), cols))
        }
        if (invoice.changeDue > 0.0) {
            writeBytes(CMD_BOLD_ON)
            writeText(formatTwoColumns("Change Returned:", String.format(Locale.getDefault(), "%,.0f", invoice.changeDue), cols))
            writeBytes(CMD_BOLD_OFF)
        }

        // 7. Footer & Barcode Simulation
        writeText(dividerLine('-', cols))
        writeBytes(CMD_ALIGN_CENTER)
        writeText(invoice.footerNote)
        writeText("Software by PakBusiness Pro ERP")
        writeText("* ${invoice.invoiceNumber} *")

        // 8. Feed & Cut
        writeBytes(CMD_FEED_5_LINES)
        writeBytes(CMD_PARTIAL_CUT)

        return stream.toByteArray()
    }

    /**
     * Generates ESC/POS byte sequence for a Restaurant Kitchen Order Ticket (KOT).
     */
    fun generateKotTicketBytes(
        tableName: String,
        orderId: Long,
        items: List<KotPrintItem>,
        guestCount: Int = 1,
        serverName: String = "Captain",
        orderNotes: String = "",
        timestamp: String = SimpleDateFormat("dd-MMM hh:mm a", Locale.getDefault()).format(Date()),
        paperSize: EscPosPaperSize
    ): ByteArray {
        val cols = paperSize.columns
        val stream = ByteArrayOutputStream()

        fun writeBytes(bytes: ByteArray) = stream.write(bytes)
        fun writeText(text: String) {
            stream.write(text.toByteArray(CHARSET_CP864))
            stream.write(LF.toInt())
        }

        // 1. Initialize Printer
        writeBytes(CMD_INIT)

        // 2. KOT Header
        writeBytes(CMD_ALIGN_CENTER)
        writeBytes(CMD_BOLD_ON)
        writeBytes(CMD_TEXT_DOUBLE_BOTH)
        writeText("*** KITCHEN TICKET (KOT) ***")

        // 3. Table Banner (Massive Bold)
        writeBytes(CMD_BOLD_ON)
        writeBytes(CMD_TEXT_DOUBLE_BOTH)
        writeText("TABLE: ${tableName.uppercase()}")

        // 4. Order Metadata
        writeBytes(CMD_ALIGN_LEFT)
        writeBytes(CMD_TEXT_NORMAL)
        writeBytes(CMD_BOLD_OFF)
        writeText(dividerLine('=', cols))
        writeText(formatTwoColumns("KOT #: $orderId", "Guests: $guestCount", cols))
        writeText(formatTwoColumns("Time: $timestamp", "Staff: $serverName", cols))
        writeText(dividerLine('=', cols))

        // 5. Kitchen Items with High-Visibility Quantities
        writeBytes(CMD_ALIGN_LEFT)
        for (item in items) {
            writeBytes(CMD_BOLD_ON)
            writeBytes(CMD_TEXT_DOUBLE_HEIGHT)
            val qtyPrefix = "[ ${item.quantity}x ] "
            writeText("$qtyPrefix${item.name}")
            writeBytes(CMD_TEXT_NORMAL)
            writeBytes(CMD_BOLD_OFF)

            // Special cooking instructions
            if (item.instructions.isNotBlank()) {
                writeBytes(CMD_BOLD_ON)
                writeText("   >> NOTE: ${item.instructions}")
                writeBytes(CMD_BOLD_OFF)
            }
            writeText(dividerLine('-', cols))
        }

        // 6. Overall Special Notes
        if (orderNotes.isNotBlank()) {
            writeBytes(CMD_BOLD_ON)
            writeText("ORDER INSTRUCTION:")
            writeText(orderNotes)
            writeBytes(CMD_BOLD_OFF)
            writeText(dividerLine('=', cols))
        }

        // 7. Feed & Cut for Kitchen Chef
        writeBytes(CMD_ALIGN_CENTER)
        writeText("--- DISPATCH TO CHEF ---")
        writeBytes(CMD_FEED_5_LINES)
        writeBytes(CMD_PARTIAL_CUT)

        return stream.toByteArray()
    }

    /**
     * Generates a printer alignment and hardware diagnostic test print.
     */
    fun generateTestPrintBytes(
        printerName: String,
        paperSize: EscPosPaperSize
    ): ByteArray {
        val cols = paperSize.columns
        val stream = ByteArrayOutputStream()

        fun writeBytes(bytes: ByteArray) = stream.write(bytes)
        fun writeText(text: String) {
            stream.write(text.toByteArray(CHARSET_CP864))
            stream.write(LF.toInt())
        }

        writeBytes(CMD_INIT)
        writeBytes(CMD_ALIGN_CENTER)
        writeBytes(CMD_BOLD_ON)
        writeBytes(CMD_TEXT_DOUBLE_BOTH)
        writeText("THERMAL PRINTER TEST")

        writeBytes(CMD_TEXT_NORMAL)
        writeBytes(CMD_BOLD_OFF)
        writeText("PakBusiness Pro Thermal Engine")
        writeText(dividerLine('=', cols))

        writeBytes(CMD_ALIGN_LEFT)
        writeText(formatTwoColumns("Device Name:", printerName, cols))
        writeText(formatTwoColumns("Paper Width:", paperSize.label, cols))
        writeText(formatTwoColumns("Max Columns:", "$cols Characters", cols))
        writeText(formatTwoColumns("Baud Rate / SPP:", "RFCOMM Bluetooth", cols))
        val now = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())
        writeText(formatTwoColumns("Timestamp:", now, cols))

        writeText(dividerLine('-', cols))
        writeText("Test 1: Alignment (Left/Center/Right)")
        writeBytes(CMD_ALIGN_LEFT)
        writeText("[LEFT ALIGNED]")
        writeBytes(CMD_ALIGN_CENTER)
        writeText("[CENTER ALIGNED]")
        writeBytes(CMD_ALIGN_RIGHT)
        writeText("[RIGHT ALIGNED]")

        writeBytes(CMD_ALIGN_LEFT)
        writeText(dividerLine('-', cols))
        writeText("Test 2: Typography")
        writeBytes(CMD_BOLD_ON)
        writeText("Bold Text: OK")
        writeBytes(CMD_BOLD_OFF)
        writeBytes(CMD_TEXT_DOUBLE_HEIGHT)
        writeText("Double Height: OK")
        writeBytes(CMD_TEXT_DOUBLE_WIDTH)
        writeText("Double Width: OK")
        writeBytes(CMD_TEXT_NORMAL)

        writeText(dividerLine('=', cols))
        writeBytes(CMD_ALIGN_CENTER)
        writeBytes(CMD_BOLD_ON)
        writeText(">>> PRINTER READY FOR POS & KOT <<<")
        writeBytes(CMD_BOLD_OFF)

        writeBytes(CMD_FEED_5_LINES)
        writeBytes(CMD_PARTIAL_CUT)

        return stream.toByteArray()
    }

    /**
     * Formats plain-text preview of the POS Receipt for on-screen receipt simulation.
     */
    fun formatPosReceiptText(
        invoice: InvoiceExportData,
        paperSize: EscPosPaperSize,
        cashierName: String? = null
    ): String {
        val cols = paperSize.columns
        val sb = StringBuilder()

        sb.appendLine(centerText(invoice.businessName.uppercase(), cols))
        if (invoice.businessAddress.isNotBlank()) sb.appendLine(centerText(invoice.businessAddress, cols))
        if (invoice.businessPhone.isNotBlank()) sb.appendLine(centerText("Tel: ${invoice.businessPhone}", cols))
        if (invoice.businessTaxId.isNotBlank()) sb.appendLine(centerText("NTN: ${invoice.businessTaxId}", cols))
        sb.appendLine(centerText("*** CASH RECEIPT ***", cols))
        sb.appendLine(dividerLine('-', cols))

        sb.appendLine(formatTwoColumns("Invoice: ${invoice.invoiceNumber}", "", cols))
        sb.appendLine(formatTwoColumns("Date: ${invoice.date}", "", cols))
        if (!cashierName.isNullOrBlank()) {
            sb.appendLine(formatTwoColumns("Cashier: $cashierName", "", cols))
        }
        if (invoice.customerName.isNotBlank() && invoice.customerName != "Walk-in Customer") {
            sb.appendLine(formatTwoColumns("Customer: ${invoice.customerName}", "", cols))
        }
        sb.appendLine(dividerLine('-', cols))

        sb.appendLine(formatThreeColumns("QTY", "ITEM", "TOTAL", cols))
        sb.appendLine(dividerLine('-', cols))

        for (item in invoice.items) {
            val qStr = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()}x" else "${item.quantity}x"
            val tStr = String.format(Locale.getDefault(), "%,.0f", item.total)
            sb.appendLine(formatThreeColumns(qStr, item.name, tStr, cols))
            if (item.quantity > 1.0) {
                sb.appendLine("   @ ${String.format(Locale.getDefault(), "%,.0f", item.unitPrice)} ea")
            }
        }

        sb.appendLine(dividerLine('-', cols))
        sb.appendLine(formatTwoColumns("Subtotal:", String.format(Locale.getDefault(), "%,.0f", invoice.subtotal), cols))
        if (invoice.discount > 0.0) {
            sb.appendLine(formatTwoColumns("Discount (-):", String.format(Locale.getDefault(), "%,.0f", invoice.discount), cols))
        }
        if (invoice.tax > 0.0) {
            sb.appendLine(formatTwoColumns("Tax (+):", String.format(Locale.getDefault(), "%,.0f", invoice.tax), cols))
        }
        sb.appendLine(dividerLine('=', cols))
        sb.appendLine(formatTwoColumns("GRAND TOTAL:", "PKR " + String.format(Locale.getDefault(), "%,.0f", invoice.grandTotal), cols))
        sb.appendLine(dividerLine('=', cols))

        sb.appendLine(formatTwoColumns("Payment:", invoice.paymentMethod, cols))
        if (invoice.amountPaid > 0.0) {
            sb.appendLine(formatTwoColumns("Tendered:", String.format(Locale.getDefault(), "%,.0f", invoice.amountPaid), cols))
        }
        if (invoice.changeDue > 0.0) {
            sb.appendLine(formatTwoColumns("Change:", String.format(Locale.getDefault(), "%,.0f", invoice.changeDue), cols))
        }

        sb.appendLine(dividerLine('-', cols))
        sb.appendLine(centerText(invoice.footerNote, cols))
        sb.appendLine(centerText("PakBusiness Pro ERP", cols))
        sb.appendLine(centerText("* ${invoice.invoiceNumber} *", cols))

        return sb.toString()
    }

    /**
     * Formats plain-text preview of a KOT Ticket for on-screen receipt simulation.
     */
    fun formatKotReceiptText(
        tableName: String,
        orderId: Long,
        items: List<KotPrintItem>,
        guestCount: Int = 1,
        serverName: String = "Captain",
        orderNotes: String = "",
        timestamp: String = SimpleDateFormat("dd-MMM hh:mm a", Locale.getDefault()).format(Date()),
        paperSize: EscPosPaperSize
    ): String {
        val cols = paperSize.columns
        val sb = StringBuilder()

        sb.appendLine(centerText("*** KITCHEN TICKET (KOT) ***", cols))
        sb.appendLine(centerText("TABLE: ${tableName.uppercase()}", cols))
        sb.appendLine(dividerLine('=', cols))
        sb.appendLine(formatTwoColumns("KOT #: $orderId", "Guests: $guestCount", cols))
        sb.appendLine(formatTwoColumns("Time: $timestamp", "Staff: $serverName", cols))
        sb.appendLine(dividerLine('=', cols))

        for (item in items) {
            sb.appendLine("[ ${item.quantity}x ] ${item.name.uppercase()}")
            if (item.instructions.isNotBlank()) {
                sb.appendLine("   >> NOTE: ${item.instructions}")
            }
            sb.appendLine(dividerLine('-', cols))
        }

        if (orderNotes.isNotBlank()) {
            sb.appendLine("ORDER NOTE:")
            sb.appendLine(orderNotes)
            sb.appendLine(dividerLine('=', cols))
        }

        sb.appendLine(centerText("--- DISPATCH TO CHEF ---", cols))

        return sb.toString()
    }
}
