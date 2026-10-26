package com.example

import com.example.data.ModulePermission
import com.example.data.StaffEntity
import com.example.data.StaffRole
import com.example.data.hasModulePermission
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RbacLogicTest {

    @Test
    fun testOwnerHasAllPermissions() {
        val owner = StaffEntity(
            businessId = 1L,
            name = "Inam Owner",
            role = "Owner",
            phone = "03001234567",
            monthlySalary = 0.0,
            accessLevel = "OWNER"
        )

        ModulePermission.entries.forEach { perm ->
            assertTrue("Owner should have ${perm.name}", owner.hasModulePermission(perm))
        }
    }

    @Test
    fun testManagerPermissions() {
        val manager = StaffEntity(
            businessId = 1L,
            name = "Ali Manager",
            role = "Branch Manager",
            phone = "03011234567",
            monthlySalary = 60000.0,
            accessLevel = "MANAGER"
        )

        assertTrue(manager.hasModulePermission(ModulePermission.POS))
        assertTrue(manager.hasModulePermission(ModulePermission.INVENTORY))
        assertTrue(manager.hasModulePermission(ModulePermission.KHATA))
        assertTrue(manager.hasModulePermission(ModulePermission.EXPENSES))
        assertTrue(manager.hasModulePermission(ModulePermission.REPORTS))
        assertTrue(manager.hasModulePermission(ModulePermission.STAFF))

        // Manager should NOT have master Settings or Delete permissions
        assertFalse(manager.hasModulePermission(ModulePermission.SETTINGS))
        assertFalse(manager.hasModulePermission(ModulePermission.DELETE))
    }

    @Test
    fun testCashierPermissions() {
        val cashier = StaffEntity(
            businessId = 1L,
            name = "Bilal Cashier",
            role = "Counter Cashier",
            phone = "03021234567",
            monthlySalary = 35000.0,
            accessLevel = "CASHIER"
        )

        // Cashier can do POS, Khata, Inventory checking
        assertTrue(cashier.hasModulePermission(ModulePermission.POS))
        assertTrue(cashier.hasModulePermission(ModulePermission.KHATA))
        assertTrue(cashier.hasModulePermission(ModulePermission.INVENTORY))

        // Cashier CANNOT view reports, edit expenses, manage staff, or settings
        assertFalse(cashier.hasModulePermission(ModulePermission.REPORTS))
        assertFalse(cashier.hasModulePermission(ModulePermission.EXPENSES))
        assertFalse(cashier.hasModulePermission(ModulePermission.STAFF))
        assertFalse(cashier.hasModulePermission(ModulePermission.SETTINGS))
        assertFalse(cashier.hasModulePermission(ModulePermission.DELETE))
    }

    @Test
    fun testViewOnlyPermissions() {
        val viewOnly = StaffEntity(
            businessId = 1L,
            name = "Auditor Viewer",
            role = "Accountant Viewer",
            phone = "03031234567",
            monthlySalary = 20000.0,
            accessLevel = "VIEW_ONLY"
        )

        // View only can view inventory, khata, reports
        assertTrue(viewOnly.hasModulePermission(ModulePermission.INVENTORY))
        assertTrue(viewOnly.hasModulePermission(ModulePermission.KHATA))
        assertTrue(viewOnly.hasModulePermission(ModulePermission.REPORTS))

        // View only CANNOT make sales, record expenses, edit staff or settings
        assertFalse(viewOnly.hasModulePermission(ModulePermission.POS))
        assertFalse(viewOnly.hasModulePermission(ModulePermission.EXPENSES))
        assertFalse(viewOnly.hasModulePermission(ModulePermission.STAFF))
        assertFalse(viewOnly.hasModulePermission(ModulePermission.SETTINGS))
    }

    @Test
    fun testCustomRolePermissions() {
        val customStaff = StaffEntity(
            businessId = 1L,
            name = "Hamza Inventory Specialist",
            role = "Stock Incharge",
            phone = "03041234567",
            monthlySalary = 40000.0,
            accessLevel = "CUSTOM",
            permissions = "INVENTORY,EXPENSES"
        )

        assertTrue(customStaff.hasModulePermission(ModulePermission.INVENTORY))
        assertTrue(customStaff.hasModulePermission(ModulePermission.EXPENSES))

        // Denied modules
        assertFalse(customStaff.hasModulePermission(ModulePermission.POS))
        assertFalse(customStaff.hasModulePermission(ModulePermission.KHATA))
        assertFalse(customStaff.hasModulePermission(ModulePermission.REPORTS))
        assertFalse(customStaff.hasModulePermission(ModulePermission.STAFF))
        assertFalse(customStaff.hasModulePermission(ModulePermission.SETTINGS))
    }
}
