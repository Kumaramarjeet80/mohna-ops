package com.mohna.ops.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohna.ops.data.model.Order
import com.mohna.ops.data.repository.MohnaRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminPickerTab(
    allOrders: List<Order>,
    searchQuery: String = ""
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance
    val horizontalScrollState = rememberScrollState()

    // Filter orders in packing queue (pending and not yet delivered or in transit)
    val packingQueue = remember(allOrders, searchQuery) {
        allOrders.filter {
            !it.status.contains("Delivered", ignoreCase = true) &&
            !it.status.contains("OUT", ignoreCase = true) &&
            (searchQuery.isEmpty() || it.orderId.contains(searchQuery, ignoreCase = true) || it.item.contains(searchQuery, ignoreCase = true))
        }
    }

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
                        text = "📦 Dark Store Picker & Packing Hub",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Packers verify items and mark orders as Ready for Dispatch. Riders can ONLY accept orders after they are packed.",
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp
                    )
                }

                Button(
                    onClick = { repository.refreshAll() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("🔄 Refresh Queue", color = Color(0xFF334155), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Packing Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(modifier = Modifier.width(950.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order ID", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                        Text("Time (IST)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(140.dp))
                        Text("Customer & Address", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(220.dp))
                        Text("SKU Items to Pack", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(230.dp))
                        Text("Total Qty", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(80.dp))
                        Text("Current Stage", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(120.dp))
                        Text("Packing Action", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(150.dp))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (packingQueue.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎉 All orders packed and ready for dispatch!",
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                            items(packingQueue, key = { it.orderId }) { order ->
                                val isPacked = order.status.contains("PACKED", ignoreCase = true)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(order.orderId, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF0F172A), modifier = Modifier.width(110.dp))
                                    Text(SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).format(Date(order.orderTimestampMs)), fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.width(140.dp))

                                    Column(modifier = Modifier.width(220.dp)) {
                                        Text(order.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                        Text(order.address, fontSize = 10.5.sp, color = Color(0xFF64748B), maxLines = 1)
                                    }

                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                        modifier = Modifier.width(230.dp)
                                    ) {
                                        Text(
                                            text = "📋 ${order.item}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }

                                    Text("${order.qty} Units", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF0F172A), modifier = Modifier.width(80.dp))

                                    Surface(
                                        color = if (isPacked) Color(0xFFDCFCE7) else Color(0xFFFEF08A),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        Text(
                                            text = if (isPacked) "● Packed & Ready" else "● Pending Packing",
                                            color = if (isPacked) Color(0xFF166534) else Color(0xFF854D0E),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Box(modifier = Modifier.width(150.dp)) {
                                        if (isPacked) {
                                            Text("Ready for Driver", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        } else {
                                            Button(
                                                onClick = {
                                                    repository.markOrderPacked(order.orderId)
                                                    Toast.makeText(context, "Order ${order.orderId} marked Packed & Ready!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("📦 Mark Packed & Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
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
