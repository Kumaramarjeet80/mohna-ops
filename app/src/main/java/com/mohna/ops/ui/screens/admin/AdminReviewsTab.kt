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
import com.mohna.ops.data.model.Review
import com.mohna.ops.data.repository.MohnaRepository

@Composable
fun AdminReviewsTab(
    reviews: List<Review>,
    searchQuery: String = ""
) {
    val context = LocalContext.current
    val repository = MohnaRepository.instance
    val horizontalScrollState = rememberScrollState()

    var editingReview by remember { mutableStateOf<Review?>(null) }
    var editedComment by remember { mutableStateOf("") }

    val filteredReviews = remember(reviews, searchQuery) {
        if (searchQuery.isEmpty()) reviews
        else reviews.filter {
            it.productName.contains(searchQuery, ignoreCase = true) ||
            it.customerName.contains(searchQuery, ignoreCase = true) ||
            it.comment.contains(searchQuery, ignoreCase = true)
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "⭐ Customer Reviews & Feedback Center",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Moderation hub for ratings, comments, and customer satisfaction logs.",
                        color = Color(0xFF64748B),
                        fontSize = 11.5.sp
                    )
                }

                IconButton(onClick = { repository.refreshAll() }) {
                    Text("🔄", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reviews Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(modifier = Modifier.width(920.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Product Name", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(220.dp))
                        Text("Customer", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(140.dp))
                        Text("Rating", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(90.dp))
                        Text("Feedback Comment", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(280.dp))
                        Text("Date", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(90.dp))
                        Text("Actions", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(100.dp))
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (filteredReviews.isEmpty()) {
                        Text(
                            text = "No customer reviews recorded.",
                            color = Color(0xFF64748B),
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 480.dp)) {
                            items(filteredReviews, key = { it.id }) { review ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(review.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A), modifier = Modifier.width(220.dp))
                                    Text(review.customerName, fontSize = 11.5.sp, color = Color(0xFF475569), modifier = Modifier.width(140.dp))

                                    Surface(color = Color(0xFFFEF3C7), shape = RoundedCornerShape(4.dp), modifier = Modifier.width(90.dp)) {
                                        Text("⭐ ${review.rating} / 5", color = Color(0xFFB45309), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(2.dp))
                                    }

                                    Text(review.comment, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.width(280.dp), maxLines = 2)
                                    Text(review.date, fontSize = 10.5.sp, color = Color(0xFF64748B), modifier = Modifier.width(90.dp))

                                    Row(modifier = Modifier.width(100.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingReview = review
                                                editedComment = review.comment
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("✏️", fontSize = 13.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                repository.deleteReview(review.id)
                                                Toast.makeText(context, "Review deleted.", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("🗑️", fontSize = 13.sp)
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

    // Inline Edit Review Modal
    editingReview?.let { rev ->
        AlertDialog(
            onDismissRequest = { editingReview = null },
            title = { Text("Edit Customer Review", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column {
                    Text("Product: ${rev.productName}", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = editedComment,
                        onValueChange = { editedComment = it },
                        label = { Text("Review Comment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateReview(rev.id, editedComment)
                        editingReview = null
                        Toast.makeText(context, "Review updated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingReview = null }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }
}
