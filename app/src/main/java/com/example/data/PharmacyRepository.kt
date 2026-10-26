package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PharmacyRepository(private val pharmacyDao: PharmacyDao) {

    fun getMedicines(businessId: Long): Flow<List<MedicineEntity>> {
        return pharmacyDao.getMedicines(businessId)
    }

    fun searchMedicines(businessId: Long, query: String): Flow<List<MedicineEntity>> {
        return pharmacyDao.searchMedicines(businessId, query)
    }

    suspend fun getMedicineById(id: Long): MedicineEntity? {
        return withContext(Dispatchers.IO) {
            pharmacyDao.getMedicineById(id)
        }
    }

    suspend fun saveMedicine(medicine: MedicineEntity): Long {
        return withContext(Dispatchers.IO) {
            if (medicine.id == 0L) {
                pharmacyDao.insertMedicine(medicine)
            } else {
                pharmacyDao.updateMedicine(medicine)
                medicine.id
            }
        }
    }

    suspend fun deleteMedicine(id: Long) {
        withContext(Dispatchers.IO) {
            pharmacyDao.deleteMedicineById(id)
        }
    }

    suspend fun updateStock(id: Long, newQuantity: Int) {
        withContext(Dispatchers.IO) {
            pharmacyDao.updateStock(id, newQuantity)
        }
    }

    fun getSales(businessId: Long): Flow<List<PharmacySaleEntity>> {
        return pharmacyDao.getSales(businessId)
    }

    suspend fun deleteSale(id: Long) {
        withContext(Dispatchers.IO) {
            pharmacyDao.deleteSaleById(id)
        }
    }

    suspend fun processQuickSale(
        businessId: Long,
        cartItems: List<PharmacyCartItem>,
        customerName: String,
        customerPhone: String,
        doctorPrescriber: String,
        discount: Double,
        paymentMethod: String,
        notes: String
    ): PharmacySaleEntity {
        return withContext(Dispatchers.IO) {
            val totalAmount = cartItems.sumOf { it.subtotal }
            val netAmount = (totalAmount - discount).coerceAtLeast(0.0)

            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val count = pharmacyDao.getSalesCount(businessId) + 1
            val invoiceNumber = "RX-$dateStr-${String.format(Locale.US, "%04d", count)}"

            val soldItems = cartItems.map {
                PharmacySoldItem(
                    medicineId = it.medicine.id,
                    name = it.medicine.name,
                    batchNumber = it.medicine.batchNumber,
                    rackNumber = it.medicine.rackNumber,
                    quantity = it.quantity,
                    unitPrice = it.medicine.salePrice,
                    subtotal = it.subtotal
                )
            }

            val sale = PharmacySaleEntity(
                businessId = businessId,
                invoiceNumber = invoiceNumber,
                customerName = if (customerName.isBlank()) "Walk-in Customer" else customerName.trim(),
                customerPhone = customerPhone.trim(),
                doctorPrescriber = doctorPrescriber.trim(),
                totalAmount = totalAmount,
                discount = discount,
                netAmount = netAmount,
                paymentMethod = paymentMethod,
                saleDate = System.currentTimeMillis(),
                itemsJson = PharmacySaleEntity.encodeItems(soldItems),
                notes = notes.trim()
            )

            val saleId = pharmacyDao.insertSale(sale)

            // Deduct inventory stock for each sold item
            for (item in cartItems) {
                pharmacyDao.deductStock(item.medicine.id, item.quantity)
            }

            sale.copy(id = saleId)
        }
    }

    suspend fun seedSampleMedicinesIfEmpty(businessId: Long) {
        return // Disabled: do not seed fake medicines
        withContext(Dispatchers.IO) {
            val count = pharmacyDao.getMedicineCount(businessId)
            if (count > 0) return@withContext

            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000L

            val samples = listOf(
                MedicineEntity(
                    businessId = businessId,
                    name = "Panadol 500mg",
                    genericName = "Paracetamol",
                    category = "Tablet",
                    batchNumber = "BT-2401",
                    expiryDate = now + (365 * dayMillis), // 1 year
                    rackNumber = "Rack A-01",
                    purchasePrice = 28.0,
                    salePrice = 35.0,
                    quantity = 120,
                    minStockAlert = 25,
                    supplierName = "GSK Pakistan Ltd.",
                    supplierPhone = "0300-4567890",
                    supplierInvoiceRef = "GSK-9982",
                    dosageInstructions = "1-2 tablets every 6 hours after food"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Augmentin 625mg",
                    genericName = "Amoxicillin + Clavulanic Acid",
                    category = "Tablet",
                    batchNumber = "AG-9842",
                    expiryDate = now + (240 * dayMillis), // 8 months
                    rackNumber = "Rack B-03",
                    purchasePrice = 260.0,
                    salePrice = 310.0,
                    quantity = 42,
                    minStockAlert = 15,
                    supplierName = "GSK Pakistan Ltd.",
                    supplierPhone = "0300-4567890",
                    supplierInvoiceRef = "GSK-9982",
                    dosageInstructions = "1 tablet every 12 hours for 5 days"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Brufen 400mg",
                    genericName = "Ibuprofen",
                    category = "Tablet",
                    batchNumber = "BF-5512",
                    expiryDate = now + (500 * dayMillis),
                    rackNumber = "Rack A-02",
                    purchasePrice = 45.0,
                    salePrice = 60.0,
                    quantity = 6, // LOW STOCK!
                    minStockAlert = 20,
                    supplierName = "Abbott Laboratories",
                    supplierPhone = "0321-9876543",
                    supplierInvoiceRef = "AB-7741",
                    dosageInstructions = "1 tablet after meal for pain"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Risek 20mg",
                    genericName = "Omeprazole",
                    category = "Capsule",
                    batchNumber = "RS-3310",
                    expiryDate = now + (420 * dayMillis),
                    rackNumber = "Rack C-01",
                    purchasePrice = 220.0,
                    salePrice = 280.0,
                    quantity = 55,
                    minStockAlert = 12,
                    supplierName = "Getz Pharma",
                    supplierPhone = "0333-1122334",
                    supplierInvoiceRef = "GZ-5021",
                    dosageInstructions = "1 capsule 30 mins before breakfast"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Flagyl 400mg",
                    genericName = "Metronidazole",
                    category = "Tablet",
                    batchNumber = "FL-7711",
                    expiryDate = now + (18 * dayMillis), // EXPIRING SOON! (in 18 days)
                    rackNumber = "Rack A-04",
                    purchasePrice = 40.0,
                    salePrice = 52.0,
                    quantity = 35,
                    minStockAlert = 15,
                    supplierName = "Sanofi Aventis",
                    supplierPhone = "0301-2233445",
                    supplierInvoiceRef = "SN-6632",
                    dosageInstructions = "1 tablet 3 times a day"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Sancos Cough Syrup 120ml",
                    genericName = "Dextromethorphan + Pseudoephedrine",
                    category = "Syrup",
                    batchNumber = "SC-1029",
                    expiryDate = now - (7 * dayMillis), // EXPIRED 7 days ago!
                    rackNumber = "Rack D-02",
                    purchasePrice = 110.0,
                    salePrice = 145.0,
                    quantity = 8,
                    minStockAlert = 10,
                    supplierName = "Novartis Pakistan",
                    supplierPhone = "0312-3344556",
                    supplierInvoiceRef = "NV-1109",
                    dosageInstructions = "2 teaspoons 3 times daily"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Polyfax Skin Ointment 20g",
                    genericName = "Polymyxin B + Bacitracin",
                    category = "Ointment",
                    batchNumber = "PF-4402",
                    expiryDate = now + (180 * dayMillis),
                    rackNumber = "Rack E-01",
                    purchasePrice = 75.0,
                    salePrice = 95.0,
                    quantity = 28,
                    minStockAlert = 10,
                    supplierName = "GSK Pakistan Ltd.",
                    supplierPhone = "0300-4567890",
                    supplierInvoiceRef = "GSK-9982",
                    dosageInstructions = "Apply thin layer to affected skin"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Cac-1000 Plus Orange (10 Tab)",
                    genericName = "Calcium Lactate + Vitamin C & D3",
                    category = "Sachet",
                    batchNumber = "CC-8921",
                    expiryDate = now + (270 * dayMillis),
                    rackNumber = "Rack B-01",
                    purchasePrice = 280.0,
                    salePrice = 350.0,
                    quantity = 65,
                    minStockAlert = 15,
                    supplierName = "Novartis Pakistan",
                    supplierPhone = "0312-3344556",
                    supplierInvoiceRef = "NV-1109",
                    dosageInstructions = "Dissolve 1 tablet in water once daily"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Arinac Forte",
                    genericName = "Ibuprofen + Pseudoephedrine",
                    category = "Tablet",
                    batchNumber = "AR-2299",
                    expiryDate = now + (320 * dayMillis),
                    rackNumber = "Rack A-03",
                    purchasePrice = 85.0,
                    salePrice = 110.0,
                    quantity = 5, // LOW STOCK!
                    minStockAlert = 20,
                    supplierName = "Abbott Laboratories",
                    supplierPhone = "0321-9876543",
                    supplierInvoiceRef = "AB-7741",
                    dosageInstructions = "1 tablet twice a day"
                ),
                MedicineEntity(
                    businessId = businessId,
                    name = "Humulin 70/30 (100 IU/ml)",
                    genericName = "Human Insulin Isophane",
                    category = "Injection",
                    batchNumber = "HM-9014",
                    expiryDate = now + (120 * dayMillis),
                    rackNumber = "Cold Storage / Fridge",
                    purchasePrice = 1250.0,
                    salePrice = 1450.0,
                    quantity = 14,
                    minStockAlert = 5,
                    supplierName = "Eli Lilly Distribution",
                    supplierPhone = "0304-7766554",
                    supplierInvoiceRef = "EL-4433",
                    dosageInstructions = "Subcutaneous injection as prescribed by endocrinologist"
                )
            )

            for (sample in samples) {
                pharmacyDao.insertMedicine(sample)
            }
        }
    }
}
