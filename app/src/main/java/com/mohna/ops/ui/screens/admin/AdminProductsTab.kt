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
import com.mohna.ops.data.model.Category
import com.mohna.ops.data.model.Product
import com.mohna.ops.data.repository.MohnaRepository

@Composable
fun AdminProductsTab(
    products: List<Product>,
    categories: List<Category>,
    searchQuery: String = ""
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance

    // Form fields
    var prodName by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf(categories.firstOrNull()?.slug ?: "edible-oils") }
    var prodScope by remember { mutableStateOf("both") } // "both", "retail", "wholesale"
    var prodRetailPrice by remember { mutableStateOf("180") }
    var prodWholesalePrice by remember { mutableStateOf("140") }
    var prodMrp by remember { mutableStateOf("210") }
    var prodStockQty by remember { mutableStateOf("100") }
    var prodDeliveryFee by remember { mutableStateOf("0") }
    var prodDescription by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isEmpty()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.id.contains(searchQuery, ignoreCase = true)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left Column: Add Product Form
        Card(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📦 Add Product to Catalog",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = prodName,
                    onValueChange = { prodName = it },
                    label = { Text("Product Title (e.g. Mustard Oil 1L)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prodRetailPrice,
                        onValueChange = { prodRetailPrice = it },
                        label = { Text("Retail (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prodWholesalePrice,
                        onValueChange = { prodWholesalePrice = it },
                        label = { Text("Wholesale (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prodMrp,
                        onValueChange = { prodMrp = it },
                        label = { Text("MRP (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prodStockQty,
                        onValueChange = { prodStockQty = it },
                        label = { Text("Stock Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prodDeliveryFee,
                        onValueChange = { prodDeliveryFee = it },
                        label = { Text("Delivery Fee (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = prodDescription,
                    onValueChange = { prodDescription = it },
                    label = { Text("Product Description / Details") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (prodName.isBlank()) {
                            Toast.makeText(context, "Please enter product name.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val product = Product(
                            id = "PRD-" + System.currentTimeMillis().toString().takeLast(5),
                            name = prodName.trim(),
                            category = prodCategory,
                            scope = prodScope,
                            retailPrice = prodRetailPrice.toDoubleOrNull() ?: 100.0,
                            wholesalePrice = prodWholesalePrice.toDoubleOrNull() ?: 80.0,
                            mrp = prodMrp.toDoubleOrNull() ?: 120.0,
                            stockQty = prodStockQty.toIntOrNull() ?: 50,
                            deliveryFee = prodDeliveryFee.toDoubleOrNull() ?: 0.0,
                            description = prodDescription.trim()
                        )
                        repository.addProduct(product)
                        prodName = ""
                        prodDescription = ""
                        Toast.makeText(context, "Product ${product.name} saved!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ Save & Publish Product", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Right Column: Products List
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
                        text = "📋 Inventory Catalog (${filteredProducts.size})",
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
                    Text("Product Name", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.8f))
                    Text("Scope", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.9f))
                    Text("Retail", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.9f))
                    Text("Stock", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.9f))
                    Text("Del", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(44.dp))
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(filteredProducts, key = { it.id }) { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.8f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A), maxLines = 1)
                                Text("SKU: ${prod.id}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }

                            Surface(color = Color(0xFFE0F2FE), shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(0.9f)) {
                                Text(prod.scope, color = Color(0xFF0369A1), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                            }

                            Text("₹${prod.retailPrice.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF16A34A), modifier = Modifier.weight(0.9f))
                            Text("${prod.stockQty}", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(0.9f))

                            IconButton(
                                onClick = {
                                    repository.deleteProduct(prod.id)
                                    Toast.makeText(context, "Product ${prod.name} removed.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.width(44.dp)
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
