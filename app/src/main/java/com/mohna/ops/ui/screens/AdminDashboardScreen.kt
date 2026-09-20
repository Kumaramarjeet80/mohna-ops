package com.mohna.ops.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohna.ops.data.repository.MohnaRepository
import com.mohna.ops.ui.screens.admin.*
import kotlinx.coroutines.delay

@Composable
fun AdminDashboardScreen(
    onSwitchToRider: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance

    val orders by repository.orders.collectAsState()
    val users by repository.users.collectAsState()
    val riders by repository.riders.collectAsState()
    val products by repository.products.collectAsState()
    val categories by repository.categories.collectAsState()
    val coupons by repository.coupons.collectAsState()
    val deliveryZones by repository.deliveryZones.collectAsState()
    val reviews by repository.reviews.collectAsState()
    val currentRole by repository.currentRole.collectAsState()

    var activeTab by remember { mutableStateOf("tab_orders") }
    var searchQuery by remember { mutableStateOf("") }
    var datePreset by remember { mutableStateOf("all") } // "all", "24h", "7d", "30d"

    // SLA live ticking seconds counter
    var currentTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMs = System.currentTimeMillis()
        }
    }

    // Filter orders by Master Date Preset
    val dateFilteredOrders = remember(orders, datePreset, searchQuery) {
        val now = System.currentTimeMillis()
        val minTimeMs = when (datePreset) {
            "24h" -> now - (24 * 60 * 60 * 1000L)
            "7d" -> now - (7 * 24 * 60 * 60 * 1000L)
            "30d" -> now - (30 * 24 * 60 * 60 * 1000L)
            else -> 0L
        }

        orders.filter {
            (it.orderTimestampMs >= minTimeMs) &&
            (searchQuery.isEmpty() ||
             it.orderId.contains(searchQuery, ignoreCase = true) ||
             it.name.contains(searchQuery, ignoreCase = true) ||
             it.phone.contains(searchQuery) ||
             (it.riderName?.contains(searchQuery, ignoreCase = true) == true) ||
             it.item.contains(searchQuery, ignoreCase = true))
        }
    }

    val tabScrollState = rememberScrollState()
    val pageScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .padding(16.dp)
            .verticalScroll(pageScrollState)
    ) {
        // 1. Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚡ Mohna Express Control Hub",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Live Synchronized Orders, Packing Hub, Delivery Timers & Fleet Control",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (currentRole == "superadmin") {
                    Button(
                        onClick = onSwitchToRider,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🛵 Rider Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { repository.refreshAll() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("🔄 Master Refresh", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                TextButton(onClick = onLogout) {
                    Text("Logout", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // 2. Tab Navigation Bar (10 Workspaces)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(6.dp)
                .horizontalScroll(tabScrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AdminTabItem(title = "🧾 Orders & Timers", isSelected = activeTab == "tab_orders") { activeTab = "tab_orders" }
            AdminTabItem(title = "📦 Dark Store Picker", isSelected = activeTab == "tab_picker") { activeTab = "tab_picker" }
            AdminTabItem(title = "👥 Users Database", isSelected = activeTab == "tab_users") { activeTab = "tab_users" }
            AdminTabItem(title = "🛵 Rider Fleet & Verify", isSelected = activeTab == "tab_riders") { activeTab = "tab_riders" }
            AdminTabItem(title = "⭐ Reviews & Feedback", isSelected = activeTab == "tab_reviews") { activeTab = "tab_reviews" }
            AdminTabItem(title = "📊 Reports (CSV & PDF)", isSelected = activeTab == "tab_reports") { activeTab = "tab_reports" }
            AdminTabItem(title = "🎟️ Coupons", isSelected = activeTab == "tab_coupons") { activeTab = "tab_coupons" }
            AdminTabItem(title = "📦 Products & Stock", isSelected = activeTab == "tab_products") { activeTab = "tab_products" }
            AdminTabItem(title = "🏷️ Categories", isSelected = activeTab == "tab_categories") { activeTab = "tab_categories" }
            AdminTabItem(title = "🛠️ Delivery Zones", isSelected = activeTab == "tab_zones") { activeTab = "tab_zones" }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Master Date Filter Strip & Omnisearch
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📅 Date Preset:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                    FilterPresetChip("🌐 All Time", isSelected = datePreset == "all") { datePreset = "all" }
                    FilterPresetChip("⏱️ 24 Hours", isSelected = datePreset == "24h") { datePreset = "24h" }
                    FilterPresetChip("📅 7 Days", isSelected = datePreset == "7d") { datePreset = "7d" }
                    FilterPresetChip("🗓️ 30 Days", isSelected = datePreset == "30d") { datePreset = "30d" }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("🔍 Omnisearch across all tabs (Names, Phones, Order IDs, SKUs)...", fontSize = 12.5.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Render Selected Workspace Tab
        when (activeTab) {
            "tab_orders" -> AdminOrdersTab(filteredOrders = dateFilteredOrders, currentTimeMs = currentTimeMs)
            "tab_picker" -> AdminPickerTab(allOrders = orders, searchQuery = searchQuery)
            "tab_users" -> AdminUsersTab(users = users, searchQuery = searchQuery)
            "tab_riders" -> AdminRidersTab(riders = riders, searchQuery = searchQuery)
            "tab_reviews" -> AdminReviewsTab(reviews = reviews, searchQuery = searchQuery)
            "tab_reports" -> AdminReportsTab(orders = dateFilteredOrders, users = users, products = products)
            "tab_coupons" -> AdminCouponsTab(coupons = coupons, zones = deliveryZones, searchQuery = searchQuery)
            "tab_products" -> AdminProductsTab(products = products, categories = categories, searchQuery = searchQuery)
            "tab_categories" -> AdminCategoriesTab(categories = categories, searchQuery = searchQuery)
            "tab_zones" -> AdminZonesTab(zones = deliveryZones, searchQuery = searchQuery)
        }
    }
}

@Composable
private fun AdminTabItem(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFF475569),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun FilterPresetChip(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFF475569),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
