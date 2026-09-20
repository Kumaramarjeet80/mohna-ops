package com.mohna.ops.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohna.ops.data.model.Rider
import com.mohna.ops.data.repository.MohnaRepository

@Composable
fun AuthScreen(
    onLoginSuccess: (role: String) -> Unit
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance

    var activeTab by remember { mutableStateOf("login") } // "login", "signup"
    var signupStep by remember { mutableStateOf(1) } // 1: details, 2: otp

    // Login Form State
    var loginEmail by remember { mutableStateOf("rahul.rider@mohna.com") }
    var loginPassword by remember { mutableStateOf("123456") }

    // Signup Form State
    var signupName by remember { mutableStateOf("") }
    var signupPhone by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupOtp by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Brand Banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Surface(
                        color = Color(0xFF0284C7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "⚡",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "Mohna Ops Gateway",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { activeTab = "login" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == "login") Color(0xFF0284C7) else Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Partner Log In",
                            color = if (activeTab == "login") Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { activeTab = "signup" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeTab == "signup") Color(0xFF0284C7) else Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Sign Up (OTP)",
                            color = if (activeTab == "signup") Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // ==========================================================
                // 1. LOGIN VIEW
                // ==========================================================
                if (activeTab == "login") {
                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        label = { Text("Email Address", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password", color = Color(0xFF94A3B8)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                Toast.makeText(context, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Role determination based on account or credentials
                            val cleanEmail = loginEmail.trim().lowercase()
                            when {
                                cleanEmail.contains("admin") -> {
                                    repository.setRole("admin")
                                    onLoginSuccess("admin")
                                }
                                cleanEmail.contains("super") -> {
                                    repository.setRole("superadmin")
                                    onLoginSuccess("superadmin")
                                }
                                else -> {
                                    val matchedRider = repository.riders.value.find { it.email.equals(cleanEmail, ignoreCase = true) }
                                        ?: Rider(
                                            id = "RDR-" + cleanEmail.hashCode().toString().takeLast(4),
                                            name = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                                            email = cleanEmail,
                                            phone = "+91 9876543210",
                                            status = "Active"
                                        )
                                    repository.setCurrentRider(matchedRider)
                                    repository.setRole("rider")
                                    onLoginSuccess("rider")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🔑 Log In & Go Online", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // ==========================================================
                // 2. SIGNUP VIEW (2-STEP WITH OTP)
                // ==========================================================
                if (activeTab == "signup") {
                    if (signupStep == 1) {
                        OutlinedTextField(
                            value = signupName,
                            onValueChange = { signupName = it },
                            label = { Text("Full Name", color = Color(0xFF94A3B8)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = signupPhone,
                            onValueChange = { signupPhone = it },
                            label = { Text("Mobile Number", color = Color(0xFF94A3B8)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = signupEmail,
                            onValueChange = { signupEmail = it },
                            label = { Text("Email Address", color = Color(0xFF94A3B8)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = signupPassword,
                            onValueChange = { signupPassword = it },
                            label = { Text("Create New Password", color = Color(0xFF94A3B8)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (signupName.isBlank() || signupPhone.isBlank() || signupEmail.isBlank() || signupPassword.length < 6) {
                                    Toast.makeText(context, "Please fill all fields (password min 6 chars).", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                signupStep = 2
                                Toast.makeText(context, "6-digit OTP dispatched to $signupEmail (Use 123456)", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Send Verification OTP", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Step 2: OTP Verification
                        Text(
                            text = "Enter 6-digit OTP sent to\n${signupEmail}",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedTextField(
                            value = signupOtp,
                            onValueChange = { if (it.length <= 6) signupOtp = it },
                            placeholder = { Text("••••••", color = Color(0xFF64748B), textAlign = TextAlign.Center) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp,
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
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (signupOtp.length < 6) {
                                    Toast.makeText(context, "Please enter valid 6-digit OTP.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val newRider = Rider(
                                    id = "RDR-" + (100 + repository.riders.value.size + 1),
                                    name = signupName.trim(),
                                    email = signupEmail.trim().lowercase(),
                                    phone = signupPhone.trim(),
                                    status = "Pending Approval",
                                    date = "Just now"
                                )
                                repository.addRider(newRider)
                                repository.setCurrentRider(newRider)
                                repository.setRole("rider")
                                Toast.makeText(context, "Profile submitted! Awaiting Admin Verification.", Toast.LENGTH_LONG).show()
                                onLoginSuccess("rider")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Verify OTP & Submit Profile", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { signupStep = 1 }) {
                            Text("← Back / Change Email", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                    }
                }

                // ==========================================================
                // DEMO QUICK-SWITCH SHORTCUTS
                // ==========================================================
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quick Demo Shortcuts",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                repository.setRole("rider")
                                repository.setCurrentRider(repository.riders.value.firstOrNull())
                                onLoginSuccess("rider")
                            }
                    ) {
                        Text(
                            "🛵 Rider",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                repository.setRole("admin")
                                onLoginSuccess("admin")
                            }
                    ) {
                        Text(
                            "🛠️ Admin",
                            color = Color(0xFF22C55E),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable {
                                repository.setRole("superadmin")
                                onLoginSuccess("superadmin")
                            }
                    ) {
                        Text(
                            "⚡ Super Admin",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
