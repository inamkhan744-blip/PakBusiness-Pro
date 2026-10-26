package com.example

import com.example.util.EscPosPaperSize
import com.example.util.EscPosUtil
import com.example.util.InvoiceExportData
import com.example.util.InvoiceExportItem
import com.example.util.KotPrintItem
import org.junit.Assert.assertTrue
import org.junit.Test

class BluetoothThermalPrinterTest {

    @Test
    fun testGeneratePosReceiptBytes58mm() {
        val invoice = InvoiceExportData(
            invoiceNumber = "INV-1001",
            businessName = "Al-Madina Super Store",
            customerName = "Muhammad Ali",
            items = listOf(
                InvoiceExportItem("Rice Super Basmati 5kg", 1.0, 1400.0, 1400.0),
                InvoiceExportItem("Cooking Oil 1L", 2.0, 480.0, 960.0)
            ),
            subtotal = 2360.0,
            discount = 60.0,
            grandTotal = 2300.0,
            paymentMethod = "Cash PKR"
        )

        val bytes58mm = EscPosUtil.generatePosReceiptBytes(invoice, EscPosPaperSize.WIDTH_58MM)
        assertTrue("ESC/POS byte output should not be empty", bytes58mm.isNotEmpty())

        val text58mm = EscPosUtil.formatPosReceiptText(invoice, EscPosPaperSize.WIDTH_58MM)
        assertTrue("Text output should contain store name", text58mm.contains("AL-MADINA SUPER STORE"))
        assertTrue("Text output should contain invoice number", text58mm.contains("INV-1001"))
        assertTrue("Text output should contain grand total", text58mm.contains("2,300"))
    }

    @Test
    fun testGeneratePosReceiptBytes80mm() {
        val invoice = InvoiceExportData(
            invoiceNumber = "INV-8080",
            businessName = "Karachi Dera Restaurant",
            customerName = "Tariq Mahmood",
            items = listOf(
                InvoiceExportItem("Mutton Karahi Full", 1.0, 3200.0, 3200.0),
                InvoiceExportItem("Roghni Naan", 4.0, 60.0, 240.0)
            ),
            subtotal = 3440.0,
            grandTotal = 3440.0,
            paymentMethod = "Card"
        )

        val bytes80mm = EscPosUtil.generatePosReceiptBytes(invoice, EscPosPaperSize.WIDTH_80MM)
        assertTrue("80mm byte output should not be empty", bytes80mm.isNotEmpty())

        val text80mm = EscPosUtil.formatPosReceiptText(invoice, EscPosPaperSize.WIDTH_80MM)
        assertTrue("80mm receipt should have wide width columns (48 columns)", EscPosPaperSize.WIDTH_80MM.columns == 48)
        assertTrue("Text output should contain Mutton Karahi", text80mm.contains("Mutton Karahi Full"))
    }

    @Test
    fun testGenerateKotTicketBytes() {
        val kotItems = listOf(
            KotPrintItem(name = "Chicken Biryani Double", quantity = 2, instructions = "Extra spicy, raita separate"),
            KotPrintItem(name = "Seekh Kabab Plate", quantity = 1, instructions = "Well done")
        )

        val kotBytes = EscPosUtil.generateKotTicketBytes(
            tableName = "Table T-4",
            orderId = 1042L,
            items = kotItems,
            guestCount = 3,
            serverName = "Waiter Bilal",
            orderNotes = "VIP Guests",
            paperSize = EscPosPaperSize.WIDTH_58MM
        )

        assertTrue("KOT byte output should not be empty", kotBytes.isNotEmpty())

        val kotText = EscPosUtil.formatKotReceiptText(
            tableName = "Table T-4",
            orderId = 1042L,
            items = kotItems,
            guestCount = 3,
            serverName = "Waiter Bilal",
            orderNotes = "VIP Guests",
            paperSize = EscPosPaperSize.WIDTH_58MM
        )

        assertTrue("KOT header should exist", kotText.contains("KITCHEN TICKET (KOT)"))
        assertTrue("Table name should exist", kotText.contains("TABLE: TABLE T-4"))
        assertTrue("Order number should exist", kotText.contains("1042"))
        assertTrue("Item instructions should exist", kotText.contains("Extra spicy"))
    }

    @Test
    fun testBuildTestReceiptBytes() {
        val testBytes = EscPosUtil.generateTestPrintBytes("PakBusiness POS", EscPosPaperSize.WIDTH_58MM)
        assertTrue("Test receipt bytes should be generated", testBytes.isNotEmpty())
    }
}
