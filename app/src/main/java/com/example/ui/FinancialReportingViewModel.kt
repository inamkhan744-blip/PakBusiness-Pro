package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ExpenseDao
import com.example.data.InventoryItemDao
import com.example.data.OrderDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Daily aggregated metric data model for charts and visual reports.
 */
data class DailyFinancialMetric(
    val dateLabel: String,       // Short label for chart X-axis e.g. "01 Oct"
    val fullDate: String,        // Formatted date string e.g. "2026-10-01"
    val timestamp: Long,
    val sales: Double,           // Daily gross sales
    val inventoryCost: Double,   // Daily cost of goods sold (COGS)
    val expenses: Double,        // Daily operational expenses
    val netProfit: Double = sales - (inventoryCost + expenses),
    val marginPercent: Double = if (sales > 0) ((sales - (inventoryCost + expenses)) / sales) * 100.0 else 0.0
)

/**
 * Aggregated summary over the selected reporting window.
 */
data class FinancialSummary(
    val totalSales: Double = 0.0,
    val totalInventoryCost: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val averageDailySales: Double = 0.0,
    val bestDayLabel: String = "-",
    val bestDaySales: Double = 0.0
)

enum class ReportPeriod(val label: String, val dayCount: Int) {
    WEEK("7 Days", 7),
    TWO_WEEKS("14 Days", 14),
    MONTH("30 Days", 30)
}

/**
 * ViewModel aggregating daily sales, inventory costs, and net profit.
 * Exposes observable StateFlow streams for native Jetpack Compose charts and dashboards.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FinancialReportingViewModel(
    application: Application,
    private val orderDao: OrderDao = AppDatabase.getDatabase(application).orderDao(),
    private val inventoryDao: InventoryItemDao = AppDatabase.getDatabase(application).inventoryItemDao(),
    private val expenseDao: ExpenseDao = AppDatabase.getDatabase(application).expenseDao()
) : BaseBusinessViewModel(application) {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.WEEK)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _selectedMetricIndex = MutableStateFlow<Int?>(null)
    val selectedMetricIndex: StateFlow<Int?> = _selectedMetricIndex.asStateFlow()

    // Observable stream of orders for the active tenant
    private val tenantOrders = activeBusiness.flatMapLatest { biz ->
        if (biz != null) orderDao.getOrders(biz.id) else flowOf(emptyList())
    }

    // Observable stream of expenses for the active tenant
    private val tenantExpenses = activeBusiness.flatMapLatest { biz ->
        if (biz != null) expenseDao.getExpenses(biz.id) else flowOf(emptyList())
    }

    // Observable stream of inventory catalog
    private val tenantInventory = activeBusiness.flatMapLatest { biz ->
        if (biz != null) inventoryDao.getItems(biz.id) else flowOf(emptyList())
    }

    // Daily metrics calculated reactively by combining orders, expenses, and selected time window
    val dailyMetrics: StateFlow<List<DailyFinancialMetric>> = combine(
        tenantOrders,
        tenantExpenses,
        tenantInventory,
        selectedPeriod
    ) { orders, expenses, items, period ->
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val shortFormat = SimpleDateFormat("dd MMM", Locale.US)

        val metrics = mutableListOf<DailyFinancialMetric>()

        // Estimate average product cost margin (ratio of cost to sale price)
        val avgCostRatio = if (items.isNotEmpty()) {
            val totalCost = items.sumOf { it.costPrice }
            val totalSale = items.sumOf { it.salePrice }
            if (totalSale > 0) (totalCost / totalSale).coerceIn(0.2, 0.85) else 0.55
        } else {
            0.55 // Standard 55% default COGS ratio for retail/distribution
        }

        for (i in (period.dayCount - 1) downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = dayCal.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000L - 1
            val dateStr = dateFormat.format(dayCal.time)
            val shortStr = shortFormat.format(dayCal.time)

            // Orders during this day
            val dayOrders = orders.filter { it.createdAt in startOfDay..endOfDay }
            val daySales = dayOrders.sumOf { it.totalAmount }

            // Inventory cost for goods sold this day
            val dayCogs = daySales * avgCostRatio

            // Operational expenses logged for this day
            val dayExpenses = expenses.filter { it.date in startOfDay..endOfDay }
                .sumOf { it.amount }

            metrics.add(
                DailyFinancialMetric(
                    dateLabel = shortStr,
                    fullDate = dateStr,
                    timestamp = startOfDay,
                    sales = daySales,
                    inventoryCost = dayCogs,
                    expenses = dayExpenses
                )
            )
        }
        metrics
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // High-level financial summary KPIs
    val summary: StateFlow<FinancialSummary> = dailyMetrics.combine(selectedPeriod) { list, period ->
        if (list.isEmpty()) {
            FinancialSummary()
        } else {
            val totalSales = list.sumOf { it.sales }
            val totalCost = list.sumOf { it.inventoryCost }
            val totalExpenses = list.sumOf { it.expenses }
            val netProfit = totalSales - (totalCost + totalExpenses)
            val margin = if (totalSales > 0) (netProfit / totalSales) * 100.0 else 0.0
            val bestDay = list.maxByOrNull { it.sales }

            FinancialSummary(
                totalSales = totalSales,
                totalInventoryCost = totalCost,
                totalExpenses = totalExpenses,
                netProfit = netProfit,
                profitMarginPercent = margin,
                averageDailySales = totalSales / period.dayCount.toDouble(),
                bestDayLabel = bestDay?.dateLabel ?: "-",
                bestDaySales = bestDay?.sales ?: 0.0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
        _selectedMetricIndex.value = null
    }

    fun selectDataPoint(index: Int?) {
        _selectedMetricIndex.value = index
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FinancialReportingViewModel(application) as T
                }
            }
    }
}
