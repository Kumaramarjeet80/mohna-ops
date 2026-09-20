package com.mohna.ops.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.mohna.ops.data.model.User
import com.mohna.ops.data.repository.MohnaRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminUsersTab(
    users: List<User>,
    searchQuery: String = ""
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance
    val horizontalScrollState = rememberScrollState()

    var showAddUserDialog by remember { mutableStateOf(false) }

    // Add User Form State
    var newUserName by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserPhone by remember { mutableStateOf("") }
    var newUserWallet by remember { mutableStateOf("0") }
    var newUserStatus by remember { mutableStateOf("Active") }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isEmpty()) users
        else users.filter {
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
            // Header Bar with "Add New Customer" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👥 Registered Customers & Wallet Ledger",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Real-time user balances, registered delivery coordinates, and access control.",
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showAddUserDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("➕ Add Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { repository.refreshAll() }) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Users Table
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
                        Text("User ID", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(100.dp))
                        Text("Customer Name", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(160.dp))
                        Text("Email Address", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(180.dp))
                        Text("Phone Number", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(120.dp))
                        Text("Wallet", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(90.dp))
                        Text("GPS Coordinates", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(130.dp))
                        Text("Status", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(80.dp))
                        Text("Access Action", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(100.dp))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (filteredUsers.isEmpty()) {
                        Text(
                            text = "No customers found.",
                            color = Color(0xFF64748B),
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 500.dp)) {
                            items(filteredUsers, key = { it.id }) { user ->
                                val isBlocked = user.status.contains("Block", ignoreCase = true)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(user.id, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF0F172A), modifier = Modifier.width(100.dp))

                                    Row(modifier = Modifier.width(160.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFF0284C7),
                                            shape = CircleShape,
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(user.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                    }

                                    Text(user.email, fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.width(180.dp))
                                    Text(user.phone, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.width(120.dp))

                                    Text("₹${user.walletBalance.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF16A34A), modifier = Modifier.width(90.dp))

                                    Text("📍 ${user.regLat ?: 25.60}, ${user.regLng ?: 85.13}", fontSize = 10.5.sp, color = Color(0xFF64748B), modifier = Modifier.width(130.dp))

                                    Surface(
                                        color = if (isBlocked) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.width(80.dp)
                                    ) {
                                        Text(
                                            text = if (isBlocked) "Blocked" else "Active",
                                            color = if (isBlocked) Color(0xFFB91C1C) else Color(0xFF166534),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val newStatus = if (isBlocked) "Active" else "Blocked"
                                            repository.toggleUserStatus(user.id, newStatus)
                                            Toast.makeText(context, "User ${user.name} marked $newStatus", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isBlocked) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.width(100.dp)
                                    ) {
                                        Text(
                                            text = if (isBlocked) "🔓 Unblock" else "🚫 Block",
                                            color = if (isBlocked) Color(0xFF166534) else Color(0xFFB91C1C),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
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
    // MODAL: ADD NEW CUSTOMER (USER REQUEST IMPLEMENTATION)
    // ======================================================================
    if (showAddUserDialog) {
        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = {
                Text("➕ Add New Customer Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it },
                        label = { Text("Full Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUserEmail,
                        onValueChange = { newUserEmail = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUserPhone,
                        onValueChange = { newUserPhone = it },
                        label = { Text("Mobile Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUserWallet,
                        onValueChange = { newUserWallet = it },
                        label = { Text("Initial Wallet Balance (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUserName.isBlank() || newUserEmail.isBlank() || newUserPhone.isBlank()) {
                            Toast.makeText(context, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val newUser = User(
                            id = "USR-" + (9000 + users.size + 1),
                            name = newUserName.trim(),
                            email = newUserEmail.trim().lowercase(),
                            phone = newUserPhone.trim(),
                            walletBalance = newUserWallet.toDoubleOrNull() ?: 0.0,
                            regLat = 25.6000,
                            regLng = 85.1300,
                            loginLat = 25.6000,
                            loginLng = 85.1300,
                            status = newUserStatus,
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
                        )
                        repository.addUser(newUser)
                        showAddUserDialog = false
                        newUserName = ""
                        newUserEmail = ""
                        newUserPhone = ""
                        newUserWallet = "0"
                        Toast.makeText(context, "Customer ${newUser.name} added successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Save Customer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}
