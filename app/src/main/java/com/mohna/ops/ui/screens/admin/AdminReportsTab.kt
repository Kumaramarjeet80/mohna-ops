package com.mohna.ops.ui.screens.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.mohna.ops.data.model.*
import com.mohna.ops.util.PdfReportGenerator
import java.io.File
import java.io.FileOutputStream

@Composable
fun AdminReportsTab(
    orders: List<Order>,
    users: List<User>,
    products: List<Product>
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "📊 Instant Executive CSV & High-Resolution PDF Reports Center",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
            Text(
                text = "Exports data strictly constrained to the active Master Date Filter with official company headers, seal blocks, and signatures.",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Users
                ReportExportCard(
                    modifier = Modifier.weight(1f),
                    title = "👥 Users Database",
                    recordCount = users.size,
                    onCsvExport = { exportUsersCsv(context, users) },
                    onPdfExport = { PdfReportGenerator.printAuditReport(context, "users", orders, users, products) }
                )

                // Card 2: Orders
                ReportExportCard(
                    modifier = Modifier.weight(1f),
                    title = "🧾 Orders & Timestamps",
                    recordCount = orders.size,
                    onCsvExport = { exportOrdersCsv(context, orders) },
                    onPdfExport = { PdfReportGenerator.printAuditReport(context, "orders", orders, users, products) }
                )

                // Card 3: Inventory
                ReportExportCard(
                    modifier = Modifier.weight(1f),
                    title = "📦 Inventory & Stock",
                    recordCount = products.size,
                    onCsvExport = { exportProductsCsv(context, products) },
                    onPdfExport = { PdfReportGenerator.printAuditReport(context, "products", orders, users, products) }
                )
            }
        }
    }
}

@Composable
private fun ReportExportCard(
    modifier: Modifier = Modifier,
    title: String,
    recordCount: Int,
    onCsvExport: () -> Unit,
    onPdfExport: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF0F172A))
            Text("Filtered records: $recordCount", fontSize = 11.5.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCsvExport,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("📥 CSV", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPdfExport,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("📄 PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun exportUsersCsv(context: Context, users: List<User>) {
    val builder = StringBuilder()
    builder.append("User ID,Customer Name,Email,Phone,Wallet Balance,Status,Date\n")
    users.forEach {
        builder.append("\"${it.id}\",\"${it.name}\",\"${it.email}\",\"${it.phone}\",\"₹${it.walletBalance}\",\"${it.status}\",\"${it.date}\"\n")
    }
    shareCsvFile(context, "Mohna_Users_Audit.csv", builder.toString())
}

private fun exportOrdersCsv(context: Context, orders: List<Order>) {
    val builder = StringBuilder()
    builder.append("Order ID,Customer,Phone,Address,Items,Total Amount,Rider,Status\n")
    orders.forEach {
        builder.append("\"${it.orderId}\",\"${it.name}\",\"${it.phone}\",\"${it.address}\",\"${it.item}\",\"${it.amount}\",\"${it.riderName ?: "Unassigned"}\",\"${it.status}\"\n")
    }
    shareCsvFile(context, "Mohna_Orders_Audit.csv", builder.toString())
}

private fun exportProductsCsv(context: Context, products: List<Product>) {
    val builder = StringBuilder()
    builder.append("SKU ID,Product Name,Category,Scope,Retail Price,Wholesale Price,Stock Qty\n")
    products.forEach {
        builder.append("\"${it.id}\",\"${it.name}\",\"${it.category}\",\"${it.scope}\",\"₹${it.retailPrice}\",\"₹${it.wholesalePrice}\",\"${it.stockQty}\"\n")
    }
    shareCsvFile(context, "Mohna_Inventory_Audit.csv", builder.toString())
}

private fun shareCsvFile(context: Context, fileName: String, content: String) {
    try {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { it.write(content.toByteArray()) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export $fileName"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
