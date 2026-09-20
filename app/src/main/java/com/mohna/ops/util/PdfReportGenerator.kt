package com.mohna.ops.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.mohna.ops.AppConfig
import com.mohna.ops.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)

    /**
     * Generates and triggers Android PrintManager for Multi-Order Parcel Labels (8 per A4 sheet)
     */
    fun printParcelLabels(context: Context, orders: List<Order>) {
        val pendingOrders = orders.filter { !it.status.contains("Delivered", ignoreCase = true) }
        if (pendingOrders.isEmpty()) {
            Toast.makeText(context, "No pending orders available to print.", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        canvas.drawColor(android.graphics.Color.WHITE)

        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            textSize = 8.5f
            isAntiAlias = true
        }

        canvas.drawText("⚡ MOHNA EXPRESS - MULTI-ORDER 2FA PARCEL LABELS", 24f, 26f, titlePaint)
        canvas.drawText("A4 Print Sheet (8 Labels Max) • Generated: ${dateFormat.format(Date())}", 24f, 38f, subPaint)

        // 2 Columns x 4 Rows = 8 Labels
        val marginX = 24f
        val marginY = 48f
        val labelW = 265f
        val labelH = 188f
        val gapX = 17f
        val gapY = 8f

        val ordersToPrint = pendingOrders.take(8)

        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        val headerBarPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#2563EB")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val textHeaderPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 8f
            isAntiAlias = true
        }
        val greenPricePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#16A34A")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        ordersToPrint.forEachIndexed { index, order ->
            val col = index % 2
            val row = index / 2
            val left = marginX + col * (labelW + gapX)
            val top = marginY + row * (labelH + gapY)
            val right = left + labelW
            val bottom = top + labelH

            // Label Card Box
            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, borderPaint)

            // Header banner
            val headerRect = RectF(left, top, right, top + 22f)
            canvas.drawRoundRect(headerRect, 8f, 8f, headerBarPaint)
            canvas.drawText("⚡ MOHNA EXPRESS", left + 8f, top + 15f, textHeaderPaint)

            val orderIdBadge = order.orderId
            val orderIdW = boldPaint.measureText(orderIdBadge)
            canvas.drawText(orderIdBadge, right - orderIdW - 8f, top + 15f, textHeaderPaint)

            // Generate QR Code bitmap
            val qrPayload = Gson().toJson(
                ParcelQrPayload(
                    orderId = order.orderId,
                    parcelToken = order.parcelToken ?: "PTKN_${order.orderId}"
                )
            )
            val qrBmp = QrCodeGenerator.generateQrBitmap(qrPayload, 140)
            val qrRect = RectF(left + 8f, top + 30f, left + 90f, top + 112f)
            canvas.drawBitmap(qrBmp, null, qrRect, null)

            // Recipient & Destination Details
            var textY = top + 42f
            canvas.drawText("To: ${order.name}", left + 98f, textY, boldPaint)
            textY += 14f
            canvas.drawText("Phone: ${order.phone}", left + 98f, textY, bodyPaint)
            textY += 14f
            val shortAddr = if (order.address.length > 32) order.address.take(30) + "..." else order.address
            canvas.drawText("Addr: $shortAddr", left + 98f, textY, bodyPaint)
            textY += 14f
            val shortItem = if (order.item.length > 30) order.item.take(28) + "..." else order.item
            canvas.drawText("Item: $shortItem", left + 98f, textY, bodyPaint)

            // Footer separator
            canvas.drawLine(left + 6f, bottom - 30f, right - 6f, bottom - 30f, borderPaint)
            canvas.drawText("SECURE 2FA PARCEL LABEL", left + 8f, bottom - 12f, subPaint)
            canvas.drawText("TOTAL: ${order.amount}", right - 80f, bottom - 12f, greenPricePaint)
        }

        pdfDoc.finishPage(page)
        printPdfDocument(context, "Mohna_Parcel_Labels", pdfDoc)
    }

    /**
     * Prints A4 Dispatch Docket Manifest
     */
    fun printDispatchDocket(context: Context, orders: List<Order>) {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(android.graphics.Color.WHITE)

        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 9f
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 8.5f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            isAntiAlias = true
        }

        // Header
        canvas.drawText("⚡ MOHNA EXPRESS DISPATCH DOCKET", 30f, 40f, titlePaint)
        canvas.drawText("Official Packing Manifest & Rider Assignment Voucher (A4 Master)", 30f, 54f, subPaint)
        canvas.drawText("Printed: ${dateFormat.format(Date())} • Total: ${orders.size} Orders", 30f, 68f, subPaint)

        canvas.drawLine(30f, 78f, 565f, 78f, Paint().apply { color = android.graphics.Color.parseColor("#0F172A"); strokeWidth = 2f })

        // Table Header
        val headerY = 96f
        canvas.drawRect(30f, 84f, 565f, 102f, Paint().apply { color = android.graphics.Color.parseColor("#F1F5F9") })
        canvas.drawText("Order ID", 34f, headerY, boldPaint)
        canvas.drawText("Customer & Phone", 110f, headerY, boldPaint)
        canvas.drawText("Destination Address", 230f, headerY, boldPaint)
        canvas.drawText("Items (Qty)", 370f, headerY, boldPaint)
        canvas.drawText("Total", 470f, headerY, boldPaint)
        canvas.drawText("ETA", 515f, headerY, boldPaint)
        canvas.drawText("Check", 545f, headerY, boldPaint)

        var curY = 118f
        val printList = orders.take(18)
        printList.forEachIndexed { i, order ->
            if (i % 2 == 1) {
                canvas.drawRect(30f, curY - 12f, 565f, curY + 6f, Paint().apply { color = android.graphics.Color.parseColor("#F8FAFC") })
            }
            canvas.drawText(order.orderId, 34f, curY, boldPaint)
            val custText = "${order.name} (${order.phone.takeLast(10)})"
            canvas.drawText(if (custText.length > 22) custText.take(20) + ".." else custText, 110f, curY, textPaint)
            val addrText = if (order.address.length > 26) order.address.take(24) + ".." else order.address
            canvas.drawText(addrText, 230f, curY, textPaint)
            val itemText = if (order.item.length > 20) order.item.take(18) + ".." else order.item
            canvas.drawText("$itemText (${order.qty})", 370f, curY, textPaint)
            canvas.drawText(order.amount, 470f, curY, boldPaint)
            canvas.drawText("${order.etaMinutes}m", 515f, curY, textPaint)
            canvas.drawText("☐", 548f, curY, boldPaint)

            canvas.drawLine(30f, curY + 8f, 565f, curY + 8f, linePaint)
            curY += 22f
        }

        // Footer Checklist & Signatures
        val footerY = 750f
        canvas.drawLine(30f, footerY, 565f, footerY, linePaint)
        canvas.drawText("Dispatch Verification Checklist:", 34f, footerY + 16f, boldPaint)
        canvas.drawText("✔ All bags packed & checked against barcode SKU.", 34f, footerY + 28f, subPaint)
        canvas.drawText("✔ Rider assigned with GPS turn-by-turn routing.", 34f, footerY + 40f, subPaint)

        canvas.drawText("Amarjeet Kumar", 450f, footerY + 26f, boldPaint)
        canvas.drawText("Warehouse Dispatcher", 450f, footerY + 38f, subPaint)
        canvas.drawText("Official Seal & Signature", 450f, footerY + 50f, subPaint)

        pdfDoc.finishPage(page)
        printPdfDocument(context, "Mohna_Dispatch_Docket", pdfDoc)
    }

    /**
     * Generates and prints Executive Audit PDF Reports (Users, Orders, Inventory)
     */
    fun printAuditReport(
        context: Context,
        reportType: String, // "users", "orders", "products"
        orders: List<Order>,
        users: List<User>,
        products: List<Product>
    ) {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(android.graphics.Color.WHITE)

        val refNumber = "REP-" + (100000 + Random().nextInt(900000))
        val timestamp = dateFormat.format(Date())

        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#64748B")
            textSize = 8.5f
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#334155")
            textSize = 8.5f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E2E8F0")
            strokeWidth = 1f
            isAntiAlias = true
        }

        // 1. Corporate Official Header
        canvas.drawText("⚡ MOHNA EXPRESS", 34f, 40f, titlePaint)
        canvas.drawText(AppConfig.COMPANY_FULL_NAME, 34f, 54f, Paint().apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        })
        canvas.drawText("CIN: ${AppConfig.COMPANY_CIN} | GSTIN: ${AppConfig.COMPANY_GSTIN}", 34f, 66f, subPaint)
        canvas.drawText("HQ: ${AppConfig.COMPANY_HQ} • Email: ${AppConfig.COMPANY_EMAIL}", 34f, 78f, subPaint)

        // Reference Block
        val refBoxPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E0F2FE")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(420f, 26f, 560f, 52f), 6f, 6f, refBoxPaint)
        canvas.drawText("OFFICIAL AUDIT RECORD", 428f, 42f, Paint().apply {
            color = android.graphics.Color.parseColor("#0369A1")
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        })
        canvas.drawText("Ref: $refNumber", 428f, 65f, subPaint)
        canvas.drawText("Date: $timestamp", 428f, 77f, subPaint)

        // Accent divider
        canvas.drawLine(34f, 88f, 560f, 88f, Paint().apply { color = android.graphics.Color.parseColor("#0284C7"); strokeWidth = 2.5f })

        // 2. Report Sub-banner
        val reportBannerRect = RectF(34f, 96f, 560f, 126f)
        canvas.drawRoundRect(reportBannerRect, 6f, 6f, Paint().apply { color = android.graphics.Color.parseColor("#F8FAFC") })
        canvas.drawRoundRect(reportBannerRect, 6f, 6f, Paint().apply { color = android.graphics.Color.parseColor("#CBD5E1"); style = Paint.Style.STROKE; strokeWidth = 1f })

        val reportHeading = when (reportType) {
            "users" -> "REGISTERED CUSTOMERS AUDIT STATEMENT"
            "products" -> "INVENTORY VALUATION & STOCK LEVEL REPORT"
            else -> "EXECUTIVE SALES & DISPATCH AUDIT REPORT"
        }
        canvas.drawText(reportHeading, 44f, 114f, boldPaint)

        val totalRecords = when (reportType) {
            "users" -> users.size
            "products" -> products.size
            else -> orders.size
        }
        canvas.drawText("Total Records: $totalRecords", 460f, 114f, Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        })

        // 3. Table Header
        val tableTop = 138f
        canvas.drawRect(34f, tableTop, 560f, tableTop + 20f, Paint().apply { color = android.graphics.Color.parseColor("#0F172A") })
        val tableHeaderPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var rowY = tableTop + 34f
        when (reportType) {
            "users" -> {
                canvas.drawText("USER ID", 40f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("NAME", 110f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("EMAIL", 220f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("PHONE", 360f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("WALLET", 460f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("STATUS", 515f, tableTop + 14f, tableHeaderPaint)

                users.take(20).forEachIndexed { idx, u ->
                    if (idx % 2 == 1) canvas.drawRect(34f, rowY - 12f, 560f, rowY + 6f, Paint().apply { color = android.graphics.Color.parseColor("#F8FAFC") })
                    canvas.drawText(u.id, 40f, rowY, boldPaint)
                    canvas.drawText(u.name, 110f, rowY, textPaint)
                    canvas.drawText(u.email, 220f, rowY, textPaint)
                    canvas.drawText(u.phone, 360f, rowY, textPaint)
                    canvas.drawText("₹${u.walletBalance}", 460f, rowY, boldPaint)
                    canvas.drawText(u.status, 515f, rowY, textPaint)
                    canvas.drawLine(34f, rowY + 8f, 560f, rowY + 8f, linePaint)
                    rowY += 20f
                }
            }
            "products" -> {
                canvas.drawText("SKU / ID", 40f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("PRODUCT TITLE", 110f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("SCOPE", 280f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("RETAIL", 360f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("WHOLESALE", 430f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("STOCK", 515f, tableTop + 14f, tableHeaderPaint)

                products.take(20).forEachIndexed { idx, p ->
                    if (idx % 2 == 1) canvas.drawRect(34f, rowY - 12f, 560f, rowY + 6f, Paint().apply { color = android.graphics.Color.parseColor("#F8FAFC") })
                    canvas.drawText(p.id, 40f, rowY, boldPaint)
                    val shortName = if (p.name.length > 28) p.name.take(26) + ".." else p.name
                    canvas.drawText(shortName, 110f, rowY, textPaint)
                    canvas.drawText(p.scope, 280f, rowY, textPaint)
                    canvas.drawText("₹${p.retailPrice}", 360f, rowY, boldPaint)
                    canvas.drawText("₹${p.wholesalePrice}", 430f, rowY, textPaint)
                    canvas.drawText("${p.stockQty} Units", 515f, rowY, boldPaint)
                    canvas.drawLine(34f, rowY + 8f, 560f, rowY + 8f, linePaint)
                    rowY += 20f
                }
            }
            else -> { // orders
                canvas.drawText("ORDER ID", 40f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("CUSTOMER", 120f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("ITEMS", 220f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("RIDER", 350f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("TOTAL", 460f, tableTop + 14f, tableHeaderPaint)
                canvas.drawText("STATUS", 505f, tableTop + 14f, tableHeaderPaint)

                orders.take(20).forEachIndexed { idx, o ->
                    if (idx % 2 == 1) canvas.drawRect(34f, rowY - 12f, 560f, rowY + 6f, Paint().apply { color = android.graphics.Color.parseColor("#F8FAFC") })
                    canvas.drawText(o.orderId, 40f, rowY, boldPaint)
                    canvas.drawText(o.name, 120f, rowY, textPaint)
                    val shortItem = if (o.item.length > 20) o.item.take(18) + ".." else o.item
                    canvas.drawText(shortItem, 220f, rowY, textPaint)
                    canvas.drawText(o.riderName ?: "Unassigned", 350f, rowY, textPaint)
                    canvas.drawText(o.amount, 460f, rowY, boldPaint)
                    canvas.drawText(o.status.take(12), 505f, rowY, textPaint)
                    canvas.drawLine(34f, rowY + 8f, 560f, rowY + 8f, linePaint)
                    rowY += 20f
                }
            }
        }

        // 4. Verification Seal & Dual Signature Blocks
        val sealY = 740f
        canvas.drawLine(34f, sealY, 560f, sealY, Paint().apply { color = android.graphics.Color.parseColor("#CBD5E1"); strokeWidth = 1.5f })

        // Official Seal Circle Stamp
        val sealPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawCircle(110f, sealY + 45f, 32f, sealPaint)
        canvas.drawText("MOHNA EXPRESS", 75f, sealY + 38f, Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        })
        canvas.drawText("★ VERIFIED ★", 84f, sealY + 50f, Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            textSize = 6.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        })

        // Signatures
        canvas.drawText(AppConfig.DISPATCHER_NAME, 260f, sealY + 40f, boldPaint)
        canvas.drawText(AppConfig.DISPATCHER_TITLE, 260f, sealY + 52f, subPaint)

        canvas.drawText("Mohna Management", 440f, sealY + 40f, boldPaint)
        canvas.drawText(AppConfig.DIRECTOR_TITLE, 440f, sealY + 52f, subPaint)

        pdfDoc.finishPage(page)
        printPdfDocument(context, "Mohna_Audit_${reportType.replaceFirstChar { it.uppercase() }}", pdfDoc)
    }

    private fun printPdfDocument(context: Context, jobName: String, pdfDocument: PdfDocument) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service unavailable on this device.", Toast.LENGTH_SHORT).show()
            pdfDocument.close()
            return
        }

        val cacheFile = File(context.cacheDir, "$jobName.pdf")
        try {
            FileOutputStream(cacheFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("$jobName.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        FileInputStream(cacheFile).use { input ->
                            FileOutputStream(destination?.fileDescriptor).use { output ->
                                input.copyTo(output)
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }

            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
        } catch (e: Exception) {
            Toast.makeText(context, "PDF generation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
