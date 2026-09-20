package com.mohna.ops.ui.screens.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohna.ops.data.model.Order
import com.mohna.ops.data.repository.MohnaRepository
import com.mohna.ops.util.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminOrdersTab(
    filteredOrders: List<Order>,
    currentTimeMs: Long
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance
    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🧾 Orders & Synchronized Zone ETA Tracking",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Filtered by selected date range. Live countdown tickers synchronized with dispatch SLA.",
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { PdfReportGenerator.printParcelLabels(context, filteredOrders) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🏷️ Print Parcel Labels (8 Per A4)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { PdfReportGenerator.printDispatchDocket(context, filteredOrders) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("🖨️ A4 Manifest Print", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { repository.refreshAll() }) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Orders Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(modifier = Modifier.width(1100.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order ID", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(100.dp))
                        Text("Time (IST)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(140.dp))
                        Text("Customer & Address", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(220.dp))
                        Text("Phone", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                        Text("Assigned Rider", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(130.dp))
                        Text("Paid Total", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(90.dp))
                        Text("Zone ETA", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(80.dp))
                        Text("Timer / SLA", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(140.dp))
                        Text("Status", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(100.dp))
                        Text("Route", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(80.dp))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (filteredOrders.isEmpty()) {
                        Text(
                            text = "No orders found matching the filter criteria.",
                            color = Color(0xFF64748B),
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                            items(filteredOrders, key = { it.orderId }) { order ->
                                val isDelivered = order.status.contains("Delivered", ignoreCase = true)
                                val expMs = if (order.expiryTimestamp > 0) order.expiryTimestamp else (order.orderTimestampMs + (order.etaMinutes * 60000))
                                val diffSec = (expMs - currentTimeMs) / 1000

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(order.orderId, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF0F172A), modifier = Modifier.width(100.dp))
                                    Text(formatDateTime(order.orderTimestampMs), fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.width(140.dp))

                                    Column(modifier = Modifier.width(220.dp)) {
                                        Text(order.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                        Text(order.address, fontSize = 10.5.sp, color = Color(0xFF64748B), maxLines = 1)
                                    }

                                    Text(order.phone, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.width(110.dp))

                                    Column(modifier = Modifier.width(130.dp)) {
                                        Text(order.riderName ?: "Unassigned", fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, color = if (order.riderName != null) Color(0xFF0284C7) else Color(0xFF94A3B8))
                                        if (order.riderPhone != null) {
                                            Text("📞 ${order.riderPhone}", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }
                                    }

                                    Text(order.amount, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF16A34A), modifier = Modifier.width(90.dp))

                                    Surface(color = Color(0xFFDBEAFE), shape = RoundedCornerShape(4.dp), modifier = Modifier.width(80.dp)) {
                                        Text("⚡ ${order.etaMinutes}m", color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(2.dp))
                                    }

                                    // Timer Badge
                                    Box(modifier = Modifier.width(140.dp)) {
                                        if (isDelivered) {
                                            val delMs = order.deliveredTimestamp ?: expMs
                                            val totalSec = ((delMs - order.orderTimestampMs) / 1000).coerceAtLeast(0)
                                            if (delMs <= expMs) {
                                                Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp)) {
                                                    Text("✔ ${formatSeconds(totalSec)} (On Time)", color = Color(0xFF166534), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            } else {
                                                val lateSec = ((delMs - expMs) / 1000).coerceAtLeast(0)
                                                Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(4.dp)) {
                                                    Text("⚠️ ${formatSeconds(totalSec)} (${formatSeconds(lateSec)} Late)", color = Color(0xFFB91C1C), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }
                                        } else {
                                            if (diffSec <= 0) {
                                                Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(4.dp)) {
                                                    Text("⏱️ EXPIRED", color = Color(0xFFB91C1C), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            } else {
                                                Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(4.dp)) {
                                                    Text("⏱️ ${formatSeconds(diffSec)}", color = Color(0xFF38BDF8), fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                    }

                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(4.dp), modifier = Modifier.width(100.dp)) {
                                        Text(order.status.take(12), color = Color(0xFF166534), fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.padding(2.dp))
                                    }

                                    TextButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(if (order.mapsUrl.isNotBlank()) order.mapsUrl else "geo:0,0?q=${Uri.encode(order.address)}"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.width(80.dp)
                                    ) {
                                        Text("🗺️ Route", fontSize = 11.5.sp, color = Color(0xFF2563EB))
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDateTime(ms: Long): String {
    if (ms <= 0) return "N/A"
    return SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(ms))
}

private fun formatSeconds(totalSecs: Long): String {
    val s = (totalSecs % 60).coerceAtLeast(0)
    val m = ((totalSecs / 60) % 60).coerceAtLeast(0)
    val h = (totalSecs / 3600).coerceAtLeast(0)
    return if (h > 0) String.format("%dh %02dm %02ds", h, m, s)
           else String.format("%dm %02ds", m, s)
}
