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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohna.ops.data.model.Rider
import com.mohna.ops.data.repository.MohnaRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminRidersTab(
    riders: List<Rider>,
    searchQuery: String = ""
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance
    val horizontalScrollState = rememberScrollState()

    var showAddRiderDialog by remember { mutableStateOf(false) }

    // Add Rider Form State
    var newRiderName by remember { mutableStateOf("") }
    var newRiderEmail by remember { mutableStateOf("") }
    var newRiderPhone by remember { mutableStateOf("") }
    var newRiderVehicle by remember { mutableStateOf("Hero Electric / Motorcycle") }
    var newRiderPreVerified by remember { mutableStateOf(true) }

    val filteredRiders = remember(riders, searchQuery) {
        if (searchQuery.isEmpty()) riders
        else riders.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true) ||
            it.id.contains(searchQuery, ignoreCase = true)
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
            // Header Bar with "Add Rider" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🛵 Rider Partner Verification & Fleet Control",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "1st-time admin verification is mandatory. Verify, activate, block, or onboard new delivery fleet partners.",
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showAddRiderDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("➕ Add Rider Partner", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { repository.refreshAll() }) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Riders Fleet Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(modifier = Modifier.width(960.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rider ID", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(100.dp))
                        Text("Registered Date", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(140.dp))
                        Text("Full Name", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(150.dp))
                        Text("Email Address", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(180.dp))
                        Text("Mobile Phone", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(120.dp))
                        Text("Live Telemetry", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(120.dp))
                        Text("Current Status", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(110.dp))
                        Text("Verification & Actions", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(180.dp))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (filteredRiders.isEmpty()) {
                        Text(
                            text = "No rider partners registered.",
                            color = Color(0xFF64748B),
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                            items(filteredRiders, key = { it.id }) { rider ->
                                val isPending = rider.status.contains("Pending", ignoreCase = true)
                                val isBlocked = rider.status.contains("Block", ignoreCase = true)
                                val isActive = rider.status.equals("Active", ignoreCase = true)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(rider.id, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF0F172A), modifier = Modifier.width(100.dp))
                                    Text(rider.date.ifEmpty { "N/A" }, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.width(140.dp))
                                    Text(rider.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A), modifier = Modifier.width(150.dp))
                                    Text(rider.email, fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.width(180.dp))
                                    Text(rider.phone, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.width(120.dp))

                                    // Live Telemetry
                                    Box(modifier = Modifier.width(120.dp)) {
                                        if (rider.liveLat != null && rider.liveLng != null) {
                                            Text(
                                                text = "📍 ${String.format("%.4f", rider.liveLat)}, ${String.format("%.4f", rider.liveLng)}",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF0284C7),
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Text("Offline", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                                        }
                                    }

                                    // Status Badge
                                    Surface(
                                        color = when {
                                            isPending -> Color(0xFFFEF08A)
                                            isBlocked -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFDCFCE7)
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.width(110.dp)
                                    ) {
                                        Text(
                                            text = rider.status,
                                            color = when {
                                                isPending -> Color(0xFF854D0E)
                                                isBlocked -> Color(0xFFB91C1C)
                                                else -> Color(0xFF166534)
                                            },
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Actions: Verify / Activate / Block / Unblock
                                    Row(
                                        modifier = Modifier.width(180.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isPending) {
                                            Button(
                                                onClick = {
                                                    repository.updateRiderVerification(rider.id, "Active")
                                                    Toast.makeText(context, "✔ Rider ${rider.name} verified & activated!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("✔ Verify", color = Color(0xFF166534), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Button(
                                                onClick = {
                                                    repository.updateRiderVerification(rider.id, "Blocked")
                                                    Toast.makeText(context, "Rider application rejected.", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("🚫 Reject", color = Color(0xFFB91C1C), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (isBlocked) {
                                            Button(
                                                onClick = {
                                                    repository.updateRiderVerification(rider.id, "Active")
                                                    Toast.makeText(context, "🔓 Rider ${rider.name} unblocked!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDCFCE7)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("🔓 Unblock", color = Color(0xFF166534), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    repository.updateRiderVerification(rider.id, "Blocked")
                                                    Toast.makeText(context, "🚫 Rider ${rider.name} blocked.", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("🚫 Block Rider", color = Color(0xFFB91C1C), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
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

    // ======================================================================
    // MODAL: ADD NEW RIDER PARTNER (USER REQUEST IMPLEMENTATION)
    // ======================================================================
    if (showAddRiderDialog) {
        AlertDialog(
            onDismissRequest = { showAddRiderDialog = false },
            title = {
                Text("➕ Add & Verify New Rider Partner", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newRiderName,
                        onValueChange = { newRiderName = it },
                        label = { Text("Full Rider Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRiderEmail,
                        onValueChange = { newRiderEmail = it },
                        label = { Text("Rider Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRiderPhone,
                        onValueChange = { newRiderPhone = it },
                        label = { Text("Mobile Number (10 digits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRiderVehicle,
                        onValueChange = { newRiderVehicle = it },
                        label = { Text("Vehicle Type / Model") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Checkbox(
                            checked = newRiderPreVerified,
                            onCheckedChange = { newRiderPreVerified = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (newRiderPreVerified) "✔ Pre-Verify & Activate Immediately"
                                   else "Set as Pending Approval",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (newRiderPreVerified) Color(0xFF166534) else Color(0xFF854D0E)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRiderName.isBlank() || newRiderEmail.isBlank() || newRiderPhone.isBlank()) {
                            Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val newRider = Rider(
                            id = "RDR-" + (100 + riders.size + 1),
                            name = newRiderName.trim(),
                            email = newRiderEmail.trim().lowercase(),
                            phone = newRiderPhone.trim(),
                            status = if (newRiderPreVerified) "Active" else "Pending Approval",
                            vehicle = newRiderVehicle.trim(),
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
                        )
                        repository.addRider(newRider)
                        showAddRiderDialog = false
                        newRiderName = ""
                        newRiderEmail = ""
                        newRiderPhone = ""
                        Toast.makeText(context, "Rider ${newRider.name} onboarded as ${newRider.status}!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save & Onboard Rider", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRiderDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}
