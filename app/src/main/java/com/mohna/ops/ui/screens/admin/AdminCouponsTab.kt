package com.mohna.ops.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohna.ops.data.model.Coupon
import com.mohna.ops.data.model.DeliveryZone
import com.mohna.ops.data.repository.MohnaRepository

@Composable
fun AdminCouponsTab(
    coupons: List<Coupon>,
    zones: List<DeliveryZone>,
    searchQuery: String = ""
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance

    var newCode by remember { mutableStateOf("") }
    var newDiscount by remember { mutableStateOf("20") }
    var newExpiry by remember { mutableStateOf("2026-12-31") }
    var newZoneScope by remember { mutableStateOf("all") }

    val filteredCoupons = remember(coupons, searchQuery) {
        if (searchQuery.isEmpty()) coupons
        else coupons.filter { it.code.contains(searchQuery, ignoreCase = true) }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Column: Create Coupon Card
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎟️ Create Zone Coupon",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = newCode,
                    onValueChange = { newCode = it.uppercase() },
                    label = { Text("Promo Code (e.g. DIWALI25)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newDiscount,
                    onValueChange = { newDiscount = it },
                    label = { Text("Discount (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newExpiry,
                    onValueChange = { newExpiry = it },
                    label = { Text("Valid Until (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (newCode.isBlank()) {
                            Toast.makeText(context, "Please enter a valid coupon code.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val coupon = Coupon(
                            id = "CPN-" + System.currentTimeMillis().toString().takeLast(5),
                            code = newCode.trim(),
                            discountPercent = newDiscount.toIntOrNull() ?: 10,
                            validUntil = newExpiry.trim(),
                            zoneScope = newZoneScope
                        )
                        repository.addCoupon(coupon)
                        newCode = ""
                        Toast.makeText(context, "Coupon ${coupon.code} published!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ Publish Coupon", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Right Column: Active Coupons Table
        Card(
            modifier = Modifier
                .weight(1.3f)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 Active Coupons (${filteredCoupons.size})",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                    IconButton(onClick = { repository.refreshAll() }) {
                        Text("🔄", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    Text("Code", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.2f))
                    Text("Discount", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.9f))
                    Text("Expires", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.1f))
                    Text("Zone", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(1f))
                    Text("Action", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(50.dp))
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    items(filteredCoupons, key = { it.id }) { coupon ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(coupon.code, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(1.2f))
                            Text("${coupon.discountPercent}% OFF", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF16A34A), modifier = Modifier.weight(0.9f))
                            Text(coupon.validUntil, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1.1f))
                            Text(coupon.zoneScope, fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.weight(1f))

                            IconButton(
                                onClick = {
                                    repository.deleteCoupon(coupon.id)
                                    Toast.makeText(context, "Coupon ${coupon.code} deleted", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.width(50.dp)
                            ) {
                                Text("🗑️", fontSize = 13.sp)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}
