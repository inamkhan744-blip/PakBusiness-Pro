package com.example.data

import kotlinx.coroutines.flow.Flow

class WholesaleRepository(private val dao: WholesaleDao) {

    // --- Parties ---
    fun getParties(businessId: Long): Flow<List<WholesalePartyEntity>> =
        dao.getParties(businessId)

    fun getPartiesByType(businessId: Long, partyType: String): Flow<List<WholesalePartyEntity>> =
        dao.getPartiesByType(businessId, partyType)

    suspend fun getPartyById(id: Long): WholesalePartyEntity? =
        dao.getPartyById(id)

    suspend fun insertParty(party: WholesalePartyEntity): Long =
        dao.insertParty(party)

    suspend fun updateParty(party: WholesalePartyEntity) =
        dao.updateParty(party)

    suspend fun deleteParty(party: WholesalePartyEntity) =
        dao.deleteParty(party)

    suspend fun adjustPartyBalance(partyId: Long, delta: Double) =
        dao.adjustPartyBalance(partyId, delta)

    // --- Bulk Orders ---
    fun getBulkOrders(businessId: Long): Flow<List<WholesaleBulkOrderEntity>> =
        dao.getBulkOrders(businessId)

    fun getOrdersByParty(partyId: Long): Flow<List<WholesaleBulkOrderEntity>> =
        dao.getOrdersByParty(partyId)

    suspend fun insertBulkOrder(order: WholesaleBulkOrderEntity): Long {
        val orderId = dao.insertBulkOrder(order)
        // If it's a SALE, creditBalanceAdded increases customer receivable (+).
        // If it's a PURCHASE, creditBalanceAdded increases supplier payable (-).
        val balanceDelta = if (order.orderType == "SALE") {
            order.creditBalanceAdded
        } else {
            -order.creditBalanceAdded
        }
        if (balanceDelta != 0.0) {
            dao.adjustPartyBalance(order.partyId, balanceDelta)
        }
        return orderId
    }

    suspend fun updateBulkOrder(order: WholesaleBulkOrderEntity) =
        dao.updateBulkOrder(order)

    suspend fun deleteBulkOrder(order: WholesaleBulkOrderEntity) =
        dao.deleteBulkOrder(order)

    suspend fun updateOrderStatus(orderId: Long, status: String) =
        dao.updateOrderStatus(orderId, status)

    // --- Payments ---
    fun getPayments(businessId: Long): Flow<List<WholesalePaymentEntity>> =
        dao.getPayments(businessId)

    fun getPaymentsByParty(partyId: Long): Flow<List<WholesalePaymentEntity>> =
        dao.getPaymentsByParty(partyId)

    suspend fun recordPayment(payment: WholesalePaymentEntity): Long {
        val paymentId = dao.insertPayment(payment)
        // If RECEIPT from customer: decreases customer receivable (-)
        // If PAYMENT to supplier: settles supplier payable (+)
        val balanceDelta = if (payment.paymentType == "RECEIPT") {
            -payment.amount
        } else {
            payment.amount
        }
        dao.adjustPartyBalance(payment.partyId, balanceDelta)
        return paymentId
    }

    suspend fun deletePayment(payment: WholesalePaymentEntity) =
        dao.deletePayment(payment)

    // --- Seed sample data if empty ---
    suspend fun seedSampleWholesaleDataIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake wholesale entries
        val sampleParties = listOf(
            WholesalePartyEntity(
                businessId = businessId,
                name = "Madina Grain & Oil Traders",
                contactPerson = "Haji Abdul Ghaffar",
                phone = "+92 300 8456123",
                address = "Shop #14, Akbari Mandi, Circular Road",
                city = "Lahore",
                partyType = "CUSTOMER",
                creditLimit = 1500000.0,
                currentBalance = 345000.0, // Customer owes us 345,000 PKR
                taxNtnNumber = "1458920-3",
                paymentTermsDays = 15,
                notes = "Large bulk buyer for pulses, basmati rice & cooking oil. Prompt pay on Fridays."
            ),
            WholesalePartyEntity(
                businessId = businessId,
                name = "Al-Karam General Whole-Sale",
                contactPerson = "Sheikh Muhammad Irfan",
                phone = "+92 321 7894561",
                address = "Plot 82, Jodia Bazar, Near Denso Hall",
                city = "Karachi",
                partyType = "CUSTOMER",
                creditLimit = 2500000.0,
                currentBalance = 780000.0, // Customer owes us 780,000 PKR
                taxNtnNumber = "3892147-1",
                paymentTermsDays = 30,
                notes = "Major distribution partner for Sindh region. Goods dispatched via Faisal Cargo."
            ),
            WholesalePartyEntity(
                businessId = businessId,
                name = "National Flour & Agro Mills Ltd",
                contactPerson = "Malik Naveed Awan",
                phone = "+92 333 4567890",
                address = "G.T. Road, Gujranwala Industrial Area",
                city = "Gujranwala",
                partyType = "SUPPLIER",
                creditLimit = 5000000.0,
                currentBalance = -950000.0, // We owe them 950,000 PKR
                taxNtnNumber = "0987654-9",
                paymentTermsDays = 21,
                notes = "Primary supplier for Grade-A Super Basmati & Maida. Payment through Meezan Bank direct."
            ),
            WholesalePartyEntity(
                businessId = businessId,
                name = "Pak Packaging Industries",
                contactPerson = "Chaudhry Rizwan",
                phone = "+92 301 6543219",
                address = "Sector 15, Korangi Industrial Zone",
                city = "Karachi",
                partyType = "SUPPLIER",
                creditLimit = 1000000.0,
                currentBalance = -120000.0, // We owe them 120,000 PKR
                taxNtnNumber = "7654321-0",
                paymentTermsDays = 15,
                notes = "Printed corrugated master cartons and woven PP bags manufacturer."
            ),
            WholesalePartyEntity(
                businessId = businessId,
                name = "Khyber Mega Marts Distributor",
                contactPerson = "Khanzada Asif Afridi",
                phone = "+92 313 9871234",
                address = "Karkhano Market, Jamrud Road",
                city = "Peshawar",
                partyType = "CUSTOMER",
                creditLimit = 1200000.0,
                currentBalance = 195000.0,
                taxNtnNumber = "2468135-7",
                paymentTermsDays = 10,
                notes = "Wholesale distributor for KP and frontier retail outlets."
            )
        )
        dao.insertParties(sampleParties)

        val sampleOrders = listOf(
            WholesaleBulkOrderEntity(
                businessId = businessId,
                orderNumber = "WO-2026-101",
                partyId = 1L,
                partyName = "Madina Grain & Oil Traders",
                partyPhone = "+92 300 8456123",
                orderType = "SALE",
                status = "CONFIRMED",
                orderDate = "2026-09-18",
                deliveryDate = "2026-09-20",
                vehicleNumberOrBilty = "Bilty #LK-9021 (New Subhan Goods)",
                itemsSummary = "Super Kernel Basmati 50kg (100 Bags), Dal Mash Washed 25kg (40 Sacks)",
                totalUnitsCount = 140,
                subtotal = 680000.0,
                discount = 20000.0,
                taxGst = 0.0,
                grandTotal = 660000.0,
                paidAmount = 350000.0,
                creditBalanceAdded = 310000.0,
                remarks = "Dispatched from warehouse #2. Cash token received, balance on delivery confirmation."
            ),
            WholesaleBulkOrderEntity(
                businessId = businessId,
                orderNumber = "WO-2026-102",
                partyId = 2L,
                partyName = "Al-Karam General Whole-Sale",
                partyPhone = "+92 321 7894561",
                orderType = "SALE",
                status = "DISPATCHED",
                orderDate = "2026-09-19",
                deliveryDate = "2026-09-22",
                vehicleNumberOrBilty = "Container #KHI-5542 (Faisal Movers Cargo)",
                itemsSummary = "Banaspati Ghee 16kg Tins (250 Units), Sugar 50kg Sacks (150 Bags)",
                totalUnitsCount = 400,
                subtotal = 1450000.0,
                discount = 50000.0,
                taxGst = 0.0,
                grandTotal = 1400000.0,
                paidAmount = 700000.0,
                creditBalanceAdded = 700000.0,
                remarks = "Advance 50% paid via Meezan Bank IBFT. Balance 30 days credit terms."
            ),
            WholesaleBulkOrderEntity(
                businessId = businessId,
                orderNumber = "PO-2026-051",
                partyId = 3L,
                partyName = "National Flour & Agro Mills Ltd",
                partyPhone = "+92 333 4567890",
                orderType = "PURCHASE",
                status = "DELIVERED",
                orderDate = "2026-09-15",
                deliveryDate = "2026-09-16",
                vehicleNumberOrBilty = "Truck #GT-9921 (Direct Mills Supply)",
                itemsSummary = "Fine Wheat Flour (Atta) 20kg (500 Bags), Chakki Atta (300 Bags)",
                totalUnitsCount = 800,
                subtotal = 1800000.0,
                discount = 50000.0,
                taxGst = 0.0,
                grandTotal = 1750000.0,
                paidAmount = 800000.0,
                creditBalanceAdded = 950000.0,
                remarks = "Received at main depot. Checked sample quality: moisture < 12%."
            )
        )
        dao.insertBulkOrders(sampleOrders)

        val samplePayments = listOf(
            WholesalePaymentEntity(
                businessId = businessId,
                voucherNumber = "RV-2026-401",
                partyId = 1L,
                partyName = "Madina Grain & Oil Traders",
                paymentType = "RECEIPT",
                amount = 250000.0,
                paymentMethod = "Online Bank Transfer (Meezan/HBL)",
                bankAccountOrChequeNo = "HBL IBFT Ref #TXN-889012",
                paymentDate = "2026-09-19",
                notes = "Part payment received against Khata ledger for order WO-2026-101",
                balanceAfterPayment = 345000.0
            ),
            WholesalePaymentEntity(
                businessId = businessId,
                voucherNumber = "PV-2026-210",
                partyId = 3L,
                partyName = "National Flour & Agro Mills Ltd",
                paymentType = "PAYMENT",
                amount = 500000.0,
                paymentMethod = "Cheque",
                bankAccountOrChequeNo = "Meezan Bank Chq #0098412",
                paymentDate = "2026-09-17",
                notes = "Cheque cleared for supply batch #51",
                balanceAfterPayment = -950000.0
            ),
            WholesalePaymentEntity(
                businessId = businessId,
                voucherNumber = "RV-2026-402",
                partyId = 2L,
                partyName = "Al-Karam General Whole-Sale",
                paymentType = "RECEIPT",
                amount = 300000.0,
                paymentMethod = "Cash",
                bankAccountOrChequeNo = "Direct Cash Receipt Akbari Branch",
                paymentDate = "2026-09-20",
                notes = "Cash deposited by Karachi agent",
                balanceAfterPayment = 780000.0
            )
        )
        dao.insertPayments(samplePayments)
    }
}
