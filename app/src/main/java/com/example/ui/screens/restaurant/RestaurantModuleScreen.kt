package com.example.ui.screens.restaurant

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessEntity
import com.example.data.RestaurantMenuItemEntity
import com.example.data.RestaurantOrderEntity
import com.example.data.RestaurantOrderItemEntity
import com.example.data.RestaurantTableEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldSecondary
import kotlinx.coroutines.launch

enum class RestaurantSubTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    TABLES("Table Grid", Icons.Default.TableBar, "tab_restaurant_tables"),
    ORDER_POS("Order POS", Icons.Default.PointOfSale, "tab_restaurant_pos"),
    KOT("Kitchen KOT", Icons.Default.SoupKitchen, "tab_restaurant_kot"),
    MENU("Menu Dishes", Icons.Default.RestaurantMenu, "tab_restaurant_menu")
}

@Composable
fun RestaurantModuleScreen(
    business: BusinessEntity?,
    tables: List<RestaurantTableEntity>,
    menuItems: List<RestaurantMenuItemEntity>,
    activeKotItems: List<RestaurantOrderItemEntity>,
    selectedTable: RestaurantTableEntity?,
    currentTableOrder: RestaurantOrderEntity?,
    currentTableOrderItems: List<RestaurantOrderItemEntity>,
    onSelectTable: (RestaurantTableEntity?) -> Unit,
    onSaveTable: (name: String, capacity: Int, section: String, id: Long) -> Unit,
    onDeleteTable: (Long) -> Unit,
    onReserveTable: (tableId: Long, customerName: String, phone: String) -> Unit,
    onClearReservation: (Long) -> Unit,
    onSaveMenuItem: (name: String, category: String, pricePkr: Double, description: String, prepTimeMinutes: Int, isAvailable: Boolean, id: Long) -> Unit,
    onDeleteMenuItem: (Long) -> Unit,
    onToggleMenuAvailability: (Long, Boolean) -> Unit,
    onSaveAndSendToKitchen: (order: RestaurantOrderEntity, items: List<RestaurantOrderItemEntity>) -> Unit,
    onSettleOrder: (orderId: Long, tableId: Long, paymentMethod: String) -> Unit,
    onCancelOrder: (orderId: Long, tableId: Long) -> Unit,
    onUpdateKotStatus: (itemId: Long, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val pendingKotCount = activeKotItems.count { it.kotStatus == "PENDING" || it.kotStatus == "PREPARING" }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Restaurant Sub Navigation Bar
            TabRow(
                selectedTabIndex = activeSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PakEmeraldPrimary,
                indicator = { tabPositions ->
                    if (activeSubTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeSubTab]),
                            color = PakEmeraldPrimary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                RestaurantSubTab.values().forEachIndexed { index, tab ->
                    val isSelected = activeSubTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { activeSubTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (tab == RestaurantSubTab.KOT && pendingKotCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE65100)
                                    ) {
                                        Text(
                                            text = "$pendingKotCount",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }

            // Sub Tab Body
            Crossfade(
                targetState = activeSubTab,
                label = "restaurantTabTransition",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        // 1. Table Grid View
                        TableGridView(
                            tables = tables,
                            onSelectTableForOrder = { table ->
                                onSelectTable(table)
                                activeSubTab = 1 // Switch to POS Order taking screen
                            },
                            onSaveTable = onSaveTable,
                            onDeleteTable = onDeleteTable,
                            onReserveTable = onReserveTable,
                            onClearReservation = onClearReservation,
                            onSettleTableBill = { table ->
                                onSelectTable(table)
                                activeSubTab = 1 // Switch to POS for settlement
                            }
                        )
                    }
                    1 -> {
                        // 2. Order taking screen (POS tied to table)
                        val effectiveTable = selectedTable ?: tables.firstOrNull()

                        OrderTakingPosView(
                            allTables = tables,
                            selectedTable = effectiveTable,
                            existingOrder = currentTableOrder,
                            existingOrderItems = currentTableOrderItems,
                            menuItems = menuItems,
                            onSelectTable = { table ->
                                onSelectTable(table)
                            },
                            onSaveAndSendToKitchen = { order, items ->
                                onSaveAndSendToKitchen(order, items)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Sent ${items.size} dishes to Kitchen (KOT) for ${order.tableName}!",
                                        actionLabel = "View KOT",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        activeSubTab = 2 // Switch to KOT screen
                                    }
                                }
                            },
                            onSettleOrder = { orderId, tableId, paymentMethod ->
                                onSettleOrder(orderId, tableId, paymentMethod)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Payment received ($paymentMethod) • Table cleared!")
                                }
                                activeSubTab = 0 // Return to Table Grid
                            },
                            onCancelOrder = { orderId, tableId ->
                                onCancelOrder(orderId, tableId)
                                activeSubTab = 0 // Return to Table Grid
                            },
                            onBackToTables = {
                                activeSubTab = 0 // Return to Table Grid
                            }
                        )
                    }
                    2 -> {
                        // 3. Kitchen Order Ticket (KOT) Screen
                        KitchenOrderTicketView(
                            kotItems = activeKotItems,
                            onUpdateKotStatus = onUpdateKotStatus
                        )
                    }
                    3 -> {
                        // 4. Menu Management View
                        MenuManagementView(
                            menuItems = menuItems,
                            onSaveMenuItem = onSaveMenuItem,
                            onDeleteMenuItem = onDeleteMenuItem,
                            onToggleAvailability = onToggleMenuAvailability
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
