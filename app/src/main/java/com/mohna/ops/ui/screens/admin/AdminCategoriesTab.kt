package com.mohna.ops.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mohna.ops.data.model.Category
import com.mohna.ops.data.repository.MohnaRepository

@Composable
fun AdminCategoriesTab(
    categories: List<Category>,
    searchQuery: String = ""
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance

    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryScope by remember { mutableStateOf("both") }

    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isEmpty()) categories
        else categories.filter { it.name.contains(searchQuery, ignoreCase = true) || it.slug.contains(searchQuery, ignoreCase = true) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                    text = "🏷️ Scoped Catalog Categories",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF0F172A)
                )
                IconButton(onClick = { repository.refreshAll() }) {
                    Text("🔄", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add Category Form Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text("Category Name (e.g. Edible Oils)") },
                    singleLine = true,
                    modifier = Modifier.weight(1.5f)
                )

                Button(
                    onClick = {
                        if (newCategoryName.isBlank()) {
                            Toast.makeText(context, "Please enter category name.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val slug = newCategoryName.trim().lowercase().replace(" ", "-")
                        val cat = Category(
                            id = "CAT-" + (categories.size + 1),
                            name = newCategoryName.trim(),
                            slug = slug,
                            scope = newCategoryScope
                        )
                        repository.addCategory(cat)
                        newCategoryName = ""
                        Toast.makeText(context, "Category ${cat.name} added!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("+ Add Category", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Categories Table
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                Text("Category Name", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(2f))
                Text("Scope", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(1f))
                Text("URL Slug", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.5f))
                Text("Action", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(60.dp))
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(filteredCategories, key = { it.id }) { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(2f))
                        Surface(color = Color(0xFFE0F2FE), shape = RoundedCornerShape(4.dp), modifier = Modifier.weight(1f)) {
                            Text(cat.scope, color = Color(0xFF0369A1), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                        }
                        Text(cat.slug, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1.5f))
                        IconButton(
                            onClick = {
                                repository.deleteCategory(cat.id)
                                Toast.makeText(context, "Category deleted.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.width(60.dp)
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
