package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val title: String,
    val category: String, // Rent, Utilities, Salaries, Refreshment, Supplies, Maintenance, Transport, Misc
    val amount: Double,
    val paymentMethod: String = "Cash", // Cash, Online / JazzCash / EasyPaisa, Bank Transfer, Card
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val receiptNumber: String = ""
)

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE businessId = :businessId ORDER BY date DESC")
    fun getExpenses(businessId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE businessId = :businessId ORDER BY date DESC")
    fun getExpensesForBusiness(businessId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE businessId = :businessId AND date >= :startTime AND date <= :endTime ORDER BY date DESC")
    fun getExpensesByDateRange(businessId: Long, startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT SUM(amount) FROM expenses WHERE businessId = :businessId")
    fun getTotalExpenseAmount(businessId: Long): Flow<Double?>
}

class ExpenseRepository(private val dao: ExpenseDao) {
    fun getExpenses(businessId: Long): Flow<List<ExpenseEntity>> =
        dao.getExpensesForBusiness(businessId)

    fun getExpensesByRange(businessId: Long, start: Long, end: Long): Flow<List<ExpenseEntity>> =
        dao.getExpensesByDateRange(businessId, start, end)

    suspend fun saveExpense(expense: ExpenseEntity): Long =
        dao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) =
        dao.updateExpense(expense)

    suspend fun deleteExpense(id: Long) =
        dao.deleteExpenseById(id)

    fun getTotalExpenses(businessId: Long): Flow<Double?> =
        dao.getTotalExpenseAmount(businessId)
}
