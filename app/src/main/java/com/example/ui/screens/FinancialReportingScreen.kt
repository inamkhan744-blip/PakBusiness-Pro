package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DailyFinancialMetric
import com.example.ui.FinancialReportingViewModel
import com.example.ui.FinancialSummary
import com.example.ui.ReportPeriod
import java.util.Locale

private val ColorSales = Color(0xFF2E7D32)      // Forest Green
private val ColorInventory = Color(0xFFE65100)  // Deep Orange
private val ColorExpenses = Color(0xFFC2185B)   // Pink/Crimson
private val ColorNetProfit = Color(0xFF1565C0)  // Royal Blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportingScreen(
    viewModel: FinancialReportingViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val dailyMetrics by viewModel.dailyMetrics.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedIndex by viewModel.selectedMetricIndex.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("financial_reporting_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Period Selector Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Financial Performance & Profitability",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time aggregation of sales revenue, inventory COGS, and net margin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    ReportPeriod.values().forEach { period ->
                        FilterChip(
                            selected = period == selectedPeriod,
                            onClick = { viewModel.selectPeriod(period) },
                            label = { Text(period.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // 2. High-Level Summary KPI Cards
        item {
            SummaryMetricsRow(summary = summary)
        }

        // 3. Multi-Metric Bar Chart (Sales vs Inventory vs Net Profit)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_revenue_cost_bar"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Revenue vs. Cost Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Comparing gross sales, product cost (COGS), and operational expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ChartLegendRow()

                    Spacer(modifier = Modifier.height(16.dp))

                    DailyMultiBarChart(
                        metrics = dailyMetrics,
                        selectedIndex = selectedIndex,
                        onSelectIndex = { viewModel.selectDataPoint(it) }
                    )

                    // Selected Data point details callout
                    AnimatedVisibility(visible = selectedIndex != null && selectedIndex!! in dailyMetrics.indices) {
                        selectedIndex?.let { idx ->
                            val metric = dailyMetrics[idx]
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(metric.fullDate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Sales: PKR ${String.format(Locale.US, "%,.0f", metric.sales)}", color = ColorSales, fontSize = 12.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Cost: PKR ${String.format(Locale.US, "%,.0f", metric.inventoryCost)}", color = ColorInventory, fontSize = 12.sp)
                                        Text("Net Profit: PKR ${String.format(Locale.US, "%,.0f", metric.netProfit)}", color = ColorNetProfit, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Net Profit Trend Line Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chart_net_profit_trend"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Net Profit Trajectory",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Daily net earnings after deducting inventory & expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (summary.netProfit >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", summary.profitMarginPercent)}% Margin",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (summary.netProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    NetProfitTrendChart(metrics = dailyMetrics)
                }
            }
        }

        // 5. Daily Ledger Breakdown Table
        item {
            Text(
                text = "Daily Ledger Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(dailyMetrics.reversed()) { item ->
            DailyMetricRowItem(metric = item)
        }
    }
}

@Composable
private fun SummaryMetricsRow(summary: FinancialSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Total Sales",
                amount = summary.totalSales,
                icon = Icons.Default.TrendingUp,
                iconColor = ColorSales,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Inventory COGS",
                amount = summary.totalInventoryCost,
                icon = Icons.Default.Inventory2,
                iconColor = ColorInventory,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Expenses",
                amount = summary.totalExpenses,
                icon = Icons.Default.Receipt,
                iconColor = ColorExpenses,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Net Profit",
                amount = summary.netProfit,
                icon = Icons.Default.AttachMoney,
                iconColor = ColorNetProfit,
                highlight = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(iconColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "PKR " + String.format(Locale.US, "%,.0f", amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ChartLegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendIndicator("Sales", ColorSales)
        LegendIndicator("Inventory Cost", ColorInventory)
        LegendIndicator("Net Profit", ColorNetProfit)
    }
}

@Composable
private fun LegendIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DailyMultiBarChart(
    metrics: List<DailyFinancialMetric>,
    selectedIndex: Int?,
    onSelectIndex: (Int?) -> Unit
) {
    val maxVal = metrics.maxOfOrNull { maxOf(it.sales, it.inventoryCost, it.netProfit, 100.0) } ?: 1000.0

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(metrics) {
                detectTapGestures { offset ->
                    if (metrics.isEmpty()) return@detectTapGestures
                    val groupWidth = size.width / metrics.size
                    val clickedIndex = (offset.x / groupWidth).toInt().coerceIn(0, metrics.size - 1)
                    onSelectIndex(clickedIndex)
                }
            }
    ) {
        if (metrics.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height - 25f // leave room for labels
        val groupWidth = w / metrics.size
        val barWidth = (groupWidth * 0.22f).coerceAtMost(16f)

        // Draw horizontal grid line at zero
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, h),
            end = Offset(w, h),
            strokeWidth = 1.dp.toPx()
        )

        metrics.forEachIndexed { index, m ->
            val groupStartX = index * groupWidth + (groupWidth - (barWidth * 3 + 8f)) / 2f

            // 1. Sales bar
            val salesH = ((m.sales / maxVal) * h).toFloat().coerceAtLeast(2f)
            drawRoundRect(
                color = ColorSales,
                topLeft = Offset(groupStartX, h - salesH),
                size = Size(barWidth, salesH),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // 2. Inventory cost bar
            val costH = ((m.inventoryCost / maxVal) * h).toFloat().coerceAtLeast(2f)
            drawRoundRect(
                color = ColorInventory,
                topLeft = Offset(groupStartX + barWidth + 3f, h - costH),
                size = Size(barWidth, costH),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // 3. Net profit bar (can be positive or clamped at zero)
            val profitH = ((m.netProfit.coerceAtLeast(0.0) / maxVal) * h).toFloat().coerceAtLeast(2f)
            drawRoundRect(
                color = if (m.netProfit >= 0) ColorNetProfit else ColorExpenses,
                topLeft = Offset(groupStartX + (barWidth * 2) + 6f, h - profitH),
                size = Size(barWidth, profitH),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Highlight selected column indicator
            if (selectedIndex == index) {
                drawRoundRect(
                    color = Color.Gray.copy(alpha = 0.2f),
                    topLeft = Offset(index * groupWidth, 0f),
                    size = Size(groupWidth, h),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
    }
}

@Composable
private fun NetProfitTrendChart(metrics: List<DailyFinancialMetric>) {
    val maxProfit = metrics.maxOfOrNull { it.netProfit }?.coerceAtLeast(100.0) ?: 1000.0
    val minProfit = metrics.minOfOrNull { it.netProfit }?.coerceAtMost(0.0) ?: 0.0
    val range = (maxProfit - minProfit).coerceAtLeast(100.0)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        if (metrics.size < 2) return@Canvas

        val w = size.width
        val h = size.height - 20f
        val stepX = w / (metrics.size - 1)

        val path = Path()
        val fillPath = Path()

        val points = metrics.mapIndexed { index, m ->
            val x = index * stepX
            val normalizedY = ((m.netProfit - minProfit) / range).toFloat()
            val y = h - (normalizedY * h)
            Offset(x, y)
        }

        path.moveTo(points.first().x, points.first().y)
        fillPath.moveTo(points.first().x, h)
        fillPath.lineTo(points.first().x, points.first().y)

        for (i in 1 until points.size) {
            val pPrev = points[i - 1]
            val pCurr = points[i]
            val cx = (pPrev.x + pCurr.x) / 2f
            path.cubicTo(cx, pPrev.y, cx, pCurr.y, pCurr.x, pCurr.y)
            fillPath.cubicTo(cx, pPrev.y, cx, pCurr.y, pCurr.x, pCurr.y)
        }

        fillPath.lineTo(points.last().x, h)
        fillPath.close()

        // Draw gradient area below curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(ColorNetProfit.copy(alpha = 0.35f), ColorNetProfit.copy(alpha = 0.02f)),
                startY = 0f,
                endY = h
            )
        )

        // Draw smooth line
        drawPath(
            path = path,
            color = ColorNetProfit,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw data points
        points.forEach { pt ->
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
            drawCircle(color = ColorNetProfit, radius = 2.5f.dp.toPx(), center = pt)
        }
    }
}

@Composable
private fun DailyMetricRowItem(metric: DailyFinancialMetric) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = metric.dateLabel,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Sales: PKR ${String.format(Locale.US, "%,.0f", metric.sales)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorSales
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "COGS: PKR ${String.format(Locale.US, "%,.0f", metric.inventoryCost)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorInventory
                )
                Text(
                    text = "Exp: PKR ${String.format(Locale.US, "%,.0f", metric.expenses)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorExpenses
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "PKR ${String.format(Locale.US, "%,.0f", metric.netProfit)}",
                    fontWeight = FontWeight.Bold,
                    color = if (metric.netProfit >= 0) ColorNetProfit else Color(0xFFC62828),
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (metric.marginPercent >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", metric.marginPercent)}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (metric.marginPercent >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}
