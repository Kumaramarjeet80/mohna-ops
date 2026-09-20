package com.mohna.ops.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohna.ops.data.model.Order
import com.mohna.ops.data.repository.MohnaRepository
import com.mohna.ops.service.RiderLocationService
import com.mohna.ops.util.SoundManager
import kotlinx.coroutines.delay

@Composable
fun RiderHomeScreen(
    onSwitchToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance

    val currentRider by repository.currentRider.collectAsState()
    val orders by repository.orders.collectAsState()
    val currentRole by repository.currentRole.collectAsState()

    var activeTab by remember { mutableStateOf("active") } // "active", "out", "done"

    // Scanner state
    var activeScannerMode by remember { mutableStateOf<ScannerMode?>(null) }
    var targetOrderIdForScan by remember { mutableStateOf<String?>(null) }

    // Inspect resting modal state
    var inspectOrderId by remember { mutableStateOf<String?>(null) }
    var inspectParcelToken by remember { mutableStateOf<String?>(null) }

    // Handover PIN dialog state
    var pinDialogOrderId by remember { mutableStateOf<String?>(null) }
    var enteredPin by remember { mutableStateOf("") }

    // SLA live ticking seconds counter
    var currentTimeMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        // Start background telemetry service
        RiderLocationService.start(context)
        while (true) {
            delay(1000)
            currentTimeMs = System.currentTimeMillis()
        }
    }

    val riderName = currentRider?.name?.trim()?.lowercase() ?: ""
    val isVerified = currentRider?.status?.equals("Active", ignoreCase = true) == true
    val isPending = currentRider?.status?.contains("Pending", ignoreCase = true) == true
    val isBlocked = currentRider?.status?.contains("Block", ignoreCase = true) == true

    // Filtered orders
    val readyOrders = orders.filter {
        it.status.contains("PACKED", ignoreCase = true) &&
        !it.status.contains("Delivered", ignoreCase = true) &&
        !it.status.contains("OUT", ignoreCase = true)
    }
    val myOutOrders = orders.filter {
        it.status.contains("OUT", ignoreCase = true) &&
        (it.riderName?.trim()?.lowercase() == riderName || riderName.isEmpty())
    }
    val myDeliveredOrders = orders.filter {
        it.status.contains("Delivered", ignoreCase = true) &&
        (it.riderName?.trim()?.lowercase() == riderName || riderName.isEmpty())
    }

    // On-Time Guarantee Rate Calculation
    val onTimeGuaranteeRate = remember(myDeliveredOrders) {
        if (myDeliveredOrders.isEmpty()) "100%"
        else {
            val onTimeCount = myDeliveredOrders.count {
                val exp = if (it.expiryTimestamp > 0) it.expiryTimestamp else (it.orderTimestampMs + (it.etaMinutes * 60000))
                val del = it.deliveredTimestamp ?: exp
                del <= exp
            }
            val pct = (onTimeCount.toDouble() / myDeliveredOrders.size * 100).toInt()
            "$pct%"
        }
    }

    val displayOrders = when (activeTab) {
        "active" -> readyOrders
        "out" -> myOutOrders
        else -> myDeliveredOrders
    }

    // Main Scaffold Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. Rider Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF0284C7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "⚡",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "Mohna Rider Hub",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Super Admin Toggle Switcher
                    if (currentRole == "superadmin") {
                        Surface(
                            color = Color(0xFF2563EB),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { onSwitchToAdmin() }
                        ) {
                            Text(
                                text = "🛠️ Admin Mode",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Rider Auth / Status Pill
                    Surface(
                        color = if (isVerified) Color(0xFF166534) else Color(0xFF854D0E),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { onLogout() }
                    ) {
                        Text(
                            text = if (isVerified) "🟢 ${currentRider?.name ?: "Rider"} (Active)"
                                   else "⏳ ${currentRider?.name ?: "Rider"} (${currentRider?.status ?: "Pending"})",
                            color = if (isVerified) Color(0xFFBBF7D0) else Color(0xFFFEF08A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Dynamic Account Status Alert Banners
            if (isPending) {
                Surface(
                    color = Color(0xFF78350F),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "⏳ Account Verification Required: Your profile has been submitted and is awaiting Admin Verification. You will become an active rider once approved by warehouse admin.",
                        color = Color(0xFFFEF3C7),
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (isBlocked) {
                Surface(
                    color = Color(0xFF450A0A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "🚫 Account Blocked: Your rider access has been suspended by the administrator. Contact your warehouse manager.",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 3. Live Metrics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = readyOrders.size.toString(),
                    label = "Available Dispatches"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = myDeliveredOrders.size.toString(),
                    label = "My Completed"
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = onTimeGuaranteeRate,
                    label = "My On-Time Rate"
                )
            }

            // 4. Batch Collect Bar (Visible in Ready Tab)
            if (activeTab == "active" && isVerified) {
                Surface(
                    color = Color(0xFF0284C7),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("📦 Bulk Warehouse Pickup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Scan many parcel labels continuously", color = Color(0xFFE0F2FE), fontSize = 11.sp)
                        }
                        Button(
                            onClick = { activeScannerMode = ScannerMode.BATCH_PICKUP },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("📷 Scan & Collect All", color = Color(0xFF0284C7), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 5. Common Transit Scanner Bar (Visible in Out Tab)
            if (activeTab == "out" && isVerified && myOutOrders.isNotEmpty()) {
                Surface(
                    color = Color(0xFF059669),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6EE7B7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🛵 Quick Doorstep Handover", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Scan any parcel in your bag to deliver", color = Color(0xFFECFDF5), fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                targetOrderIdForScan = null
                                activeScannerMode = ScannerMode.STEP1_PARCEL
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("📷 Common Scan Parcel", color = Color(0xFF059669), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 6. Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TabButton(
                    modifier = Modifier.weight(1f),
                    title = "⚡ Ready (${readyOrders.size})",
                    isSelected = activeTab == "active",
                    onClick = { activeTab = "active" }
                )
                TabButton(
                    modifier = Modifier.weight(1.1f),
                    title = "🛵 In-Transit (${myOutOrders.size})",
                    isSelected = activeTab == "out",
                    onClick = { activeTab = "out" }
                )
                TabButton(
                    modifier = Modifier.weight(1f),
                    title = "✅ Delivered (${myDeliveredOrders.size})",
                    isSelected = activeTab == "done",
                    onClick = { activeTab = "done" }
                )
            }

            // 7. Orders Feed
            if (displayOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!isVerified) "⚠️ Access Restricted: Account verification required by warehouse admin."
                               else "No orders in this category.",
                        color = Color(0xFF64748B),
                        fontSize = 13.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayOrders, key = { it.orderId }) { order ->
                        RiderOrderCard(
                            order = order,
                            currentTimeMs = currentTimeMs,
                            isVerified = isVerified,
                            onAcceptClick = {
                                if (currentRider != null) {
                                    repository.assignRiderToOrder(order.orderId, currentRider!!)
                                    SoundManager.playSuccess()
                                    Toast.makeText(context, "Order ${order.orderId} accepted!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onScanBagClick = {
                                activeScannerMode = ScannerMode.BATCH_PICKUP
                            },
                            onScanParcelClick = {
                                targetOrderIdForScan = order.orderId
                                activeScannerMode = ScannerMode.STEP1_PARCEL
                            },
                            onVerifyPinClick = {
                                pinDialogOrderId = order.orderId
                                enteredPin = ""
                            }
                        )
                    }
                }
            }
        }

        // ======================================================================
        // OVERLAY: CAMERA SCANNER
        // ======================================================================
        activeScannerMode?.let { mode ->
            CameraScannerOverlay(
                mode = mode,
                targetOrderId = targetOrderIdForScan,
                currentRider = currentRider,
                onClose = { activeScannerMode = null },
                onParcelVerifiedForInspection = { orderId, token ->
                    activeScannerMode = null
                    inspectOrderId = orderId
                    inspectParcelToken = token
                },
                onDeliveryCompleted = { orderId ->
                    activeScannerMode = null
                    Toast.makeText(context, "🎉 Order $orderId successfully verified and delivered!", Toast.LENGTH_LONG).show()
                }
            )
        }

        // ======================================================================
        // MODAL: STEP 1 -> STEP 2 RESTING INSPECTION CARD
        // ======================================================================
        inspectOrderId?.let { ordId ->
            val inspectOrder = orders.find { it.orderId == ordId }
            if (inspectOrder != null) {
                AlertDialog(
                    onDismissRequest = { inspectOrderId = null },
                    containerColor = Color(0xFF1E293B),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✅ Parcel Verified (On Rest)", color = Color(0xFF22C55E), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { inspectOrderId = null }) {
                                Text("✕", color = Color(0xFF94A3B8), fontSize = 18.sp)
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            // Assigned Rider Info
                            Text("🛵 ASSIGNED RIDER PARTNER INFO", color = Color(0xFF38BDF8), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text("Rider: ${currentRider?.name ?: "Assigned Rider"}", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text("Phone: ${currentRider?.phone ?: "N/A"}", color = Color(0xFF94A3B8), fontSize = 12.sp)

                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                            // Itemized Breakdown
                            Text("📦 ITEMIZED PARCEL DETAILS", color = Color(0xFF38BDF8), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text("Order ID: ${inspectOrder.orderId}", color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            Spacer(modifier = Modifier.height(4.dp))
                            if (inspectOrder.items.isNotEmpty()) {
                                inspectOrder.items.forEach { itm ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${itm.name} (x${itm.qty})", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                        Text("₹${(itm.price * itm.qty).toInt()}", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Text(inspectOrder.item, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Quantity: ${inspectOrder.qty} Units", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Text("Amount: ${inspectOrder.amount}", color = Color(0xFF22C55E), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }

                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                            // Customer Details
                            Text("👤 CUSTOMER DETAILS", color = Color(0xFFF59E0B), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text("Name: ${inspectOrder.name}", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Phone: 📞 ${inspectOrder.phone}",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${inspectOrder.phone}"))
                                    context.startActivity(intent)
                                }
                            )
                            Text("Addr: ${inspectOrder.address}", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                inspectOrderId = null
                                targetOrderIdForScan = ordId
                                activeScannerMode = ScannerMode.STEP2_CUSTOMER
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⚡ Verify & Deliver Product (Scan Customer QR)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }
        }

        // ======================================================================
        // MODAL: 4-DIGIT CUSTOMER PIN FALLBACK
        // ======================================================================
        pinDialogOrderId?.let { ordId ->
            val targetOrder = orders.find { it.orderId == ordId }
            AlertDialog(
                onDismissRequest = { pinDialogOrderId = null },
                containerColor = Color(0xFF1E293B),
                title = {
                    Text("📦 Customer Delivery PIN", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                text = {
                    Column {
                        Text("Order: $ordId", color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(
                            text = "PIN sent to: ${targetOrder?.email ?: "Customer Email"}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { if (it.length <= 4) enteredPin = it },
                            placeholder = { Text("••••", color = Color(0xFF64748B), textAlign = TextAlign.Center) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 26.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 8.sp,
                                color = Color.White
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF22C55E),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (enteredPin.length < 4) {
                                Toast.makeText(context, "Please enter 4-digit PIN.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val (success, msg) = repository.verifyDeliveryPin(ordId, enteredPin)
                            if (success) {
                                SoundManager.playSuccess()
                                pinDialogOrderId = null
                                Toast.makeText(context, "✅ $msg", Toast.LENGTH_LONG).show()
                            } else {
                                SoundManager.playError()
                                Toast.makeText(context, "❌ $msg", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("✅ Verify & Handover", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pinDialogOrderId = null }) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}

@Composable
private fun MetricCard(modifier: Modifier = Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color(0xFF38BDF8),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TabButton(modifier: Modifier = Modifier, title: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155))
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RiderOrderCard(
    order: Order,
    currentTimeMs: Long,
    isVerified: Boolean,
    onAcceptClick: () -> Unit,
    onScanBagClick: () -> Unit,
    onScanParcelClick: () -> Unit,
    onVerifyPinClick: () -> Unit
) {
    val context = LocalContext.current
    val isDelivered = order.status.contains("Delivered", ignoreCase = true)
    val isOut = order.status.contains("OUT", ignoreCase = true)

    val expMs = if (order.expiryTimestamp > 0) order.expiryTimestamp else (order.orderTimestampMs + (order.etaMinutes * 60000))
    val diffSec = (expMs - currentTimeMs) / 1000

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Header: Order ID + Timer + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderId,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )

                // Timer / Delivery Badge
                if (isDelivered) {
                    val delMs = order.deliveredTimestamp ?: expMs
                    val totalSec = ((delMs - order.orderTimestampMs) / 1000).coerceAtLeast(0)
                    val formatted = formatSeconds(totalSec)
                    if (delMs <= expMs) {
                        Surface(color = Color(0xFF064E3B), shape = RoundedCornerShape(6.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E))) {
                            Text("✔ $formatted (On Time)", color = Color(0xFF86EFAC), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    } else {
                        val lateSec = ((delMs - expMs) / 1000).coerceAtLeast(0)
                        Surface(color = Color(0xFF450A0A), shape = RoundedCornerShape(6.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))) {
                            Text("⚠️ $formatted (${formatSeconds(lateSec)} Late)", color = Color(0xFFFCA5A5), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                } else {
                    if (diffSec <= 0) {
                        Surface(color = Color(0xFF450A0A), shape = RoundedCornerShape(6.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))) {
                            Text("⏱️ 0m 00s (EXPIRED)", color = Color(0xFFFCA5A5), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    } else {
                        Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(6.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))) {
                            Text("⏱️ ${formatSeconds(diffSec)}", color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                // Status Badge
                val (statusBg, statusText) = when {
                    isDelivered -> Pair(Color(0xFFBBF7D0), Color(0xFF166534))
                    isOut -> Pair(Color(0xFFFED7AA), Color(0xFF9A3412))
                    else -> Pair(Color(0xFFFEF08A), Color(0xFF854D0E))
                }
                Surface(color = statusBg, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        text = if (isDelivered) "✔ Done" else if (isOut) "● In Transit" else "● Ready",
                        color = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Item and Customer Info
            Text(text = "${order.item} (Total Qty: ${order.qty})", color = Color(0xFFF8FAFC), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            Text(text = "👤 ${order.name} | Total: ${order.amount}", color = Color(0xFF94A3B8), fontSize = 12.5.sp)

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📍 Delivery Address: ${order.address}",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Navigation & Dialer Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse(if (order.mapsUrl.isNotBlank()) order.mapsUrl else "geo:0,0?q=${Uri.encode(order.address)}"))
                        context.startActivity(mapsIntent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("🗺️ Direct GPS Route", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.phone}"))
                        context.startActivity(callIntent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("📞 Call Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Dual Action Grid depending on status
            if (isVerified) {
                Spacer(modifier = Modifier.height(8.dp))
                if (!isOut && !isDelivered) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAcceptClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚡ Accept (Click)", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Button(
                            onClick = onScanBagClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📷 Scan Bag QR", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                } else if (isOut) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onVerifyPinClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🔐 Verify PIN", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Button(
                            onClick = onScanParcelClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📷 Scan Parcel QR", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatSeconds(totalSecs: Long): String {
    val s = (totalSecs % 60).coerceAtLeast(0)
    val m = ((totalSecs / 60) % 60).coerceAtLeast(0)
    val h = (totalSecs / 3600).coerceAtLeast(0)
    return if (h > 0) String.format("%dh %02dm %02ds", h, m, s)
           else String.format("%dm %02ds", m, s)
}
