package com.mohna.ops.ui.screens

import android.annotation.SuppressLint
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mohna.ops.data.model.CustomerQrPayload
import com.mohna.ops.data.model.ParcelQrPayload
import com.mohna.ops.data.model.Rider
import com.mohna.ops.data.repository.MohnaRepository
import com.mohna.ops.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

enum class ScannerMode {
    BATCH_PICKUP,
    STEP1_PARCEL,
    STEP2_CUSTOMER
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScannerOverlay(
    mode: ScannerMode,
    targetOrderId: String? = null,
    currentRider: Rider?,
    onClose: () -> Unit,
    onParcelVerifiedForInspection: (orderId: String, parcelToken: String?) -> Unit,
    onDeliveryCompleted: (orderId: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = MohnaRepository.instance
    val scope = rememberCoroutineScope()

    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var batchCount by remember { mutableStateOf(0) }
    val processedBatchIds = remember { mutableSetOf<String>() }
    var isThrottled by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // (message, isError)

    // Temporary toast auto-dismiss
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            delay(2400)
            notificationMessage = null
        }
    }

    val title = when (mode) {
        ScannerMode.BATCH_PICKUP -> "📷 Continuous Batch Scan"
        ScannerMode.STEP1_PARCEL -> if (targetOrderId != null) "Scan Parcel ($targetOrderId)" else "📷 Common Scanner: Scan Any Parcel"
        ScannerMode.STEP2_CUSTOMER -> "Step 2/2: Scan Customer In-App QR"
    }

    val subtitle = when (mode) {
        ScannerMode.BATCH_PICKUP -> "Hold camera over parcel stickers continuously. Auto-accepts each."
        ScannerMode.STEP1_PARCEL -> "Align camera with QR code on parcel label to verify package"
        ScannerMode.STEP2_CUSTOMER -> "Scan delivery QR displayed on customer's phone for Order $targetOrderId"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. CameraX PreviewView
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                val executor = Executors.newSingleThreadExecutor()
                val barcodeScanner = BarcodeScanning.getClient()

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(executor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !isThrottled) {
                                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(inputImage)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            val raw = barcode.rawValue ?: continue
                                            scope.launch {
                                                handleScannedText(
                                                    raw = raw,
                                                    mode = mode,
                                                    targetOrderId = targetOrderId,
                                                    currentRider = currentRider,
                                                    repository = repository,
                                                    processedBatchIds = processedBatchIds,
                                                    isThrottled = isThrottled,
                                                    setThrottled = { isThrottled = it },
                                                    incrementBatch = { batchCount++ },
                                                    showToast = { msg, isErr -> notificationMessage = Pair(msg, isErr) },
                                                    onParcelVerifiedForInspection = onParcelVerifiedForInspection,
                                                    onDeliveryCompleted = onDeliveryCompleted
                                                )
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Viewfinder Targeting Frame & Corner Brackets
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
        )

        // 3. Top Controls & Header Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Notification Toast
            AnimatedVisibility(visible = notificationMessage != null) {
                notificationMessage?.let { (msg, isError) ->
                    Surface(
                        color = if (isError) Color(0xFFDC2626) else Color(0xFF16A34A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCC1E293B), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFEF4444)
                    )
                }
            }

            // Batch Counter Pill
            if (mode == ScannerMode.BATCH_PICKUP) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📦 Accepted in this batch: ",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$batchCount bags",
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // 4. Bottom Action Bar (Torch + Manual Scan Simulator for Emulator/Testing)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Torch Button
            IconButton(
                onClick = {
                    isTorchOn = !isTorchOn
                    cameraControl?.enableTorch(isTorchOn)
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xCC1E293B), CircleShape)
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flashlight",
                    tint = if (isTorchOn) Color(0xFFF59E0B) else Color.White
                )
            }

            // Manual Demo Quick-Scan (Allows verification on emulators without webcam)
            Button(
                onClick = {
                    val sampleOrder = when (mode) {
                        ScannerMode.BATCH_PICKUP -> repository.orders.value.firstOrNull { it.status.contains("PACKED") }
                        ScannerMode.STEP1_PARCEL -> repository.orders.value.firstOrNull { it.orderId == (targetOrderId ?: it.orderId) && it.status.contains("OUT") }
                        ScannerMode.STEP2_CUSTOMER -> repository.orders.value.firstOrNull { it.orderId == targetOrderId }
                    }

                    if (sampleOrder != null) {
                        val mockRaw = when (mode) {
                            ScannerMode.BATCH_PICKUP, ScannerMode.STEP1_PARCEL ->
                                Gson().toJson(ParcelQrPayload(orderId = sampleOrder.orderId, parcelToken = sampleOrder.parcelToken ?: "PTKN_${sampleOrder.orderId}"))
                            ScannerMode.STEP2_CUSTOMER ->
                                Gson().toJson(CustomerQrPayload(orderId = sampleOrder.orderId, deliveryToken = "DELIV_TKN_${sampleOrder.orderId}"))
                        }
                        scope.launch {
                            handleScannedText(
                                raw = mockRaw,
                                mode = mode,
                                targetOrderId = targetOrderId,
                                currentRider = currentRider,
                                repository = repository,
                                processedBatchIds = processedBatchIds,
                                isThrottled = false,
                                setThrottled = {},
                                incrementBatch = { batchCount++ },
                                showToast = { msg, isErr -> notificationMessage = Pair(msg, isErr) },
                                onParcelVerifiedForInspection = onParcelVerifiedForInspection,
                                onDeliveryCompleted = onDeliveryCompleted
                            )
                        }
                    } else {
                        notificationMessage = Pair("No eligible order to simulate for $mode", true)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("⚡ Test Scan (Simulate)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private suspend fun handleScannedText(
    raw: String,
    mode: ScannerMode,
    targetOrderId: String?,
    currentRider: Rider?,
    repository: MohnaRepository,
    processedBatchIds: MutableSet<String>,
    isThrottled: Boolean,
    setThrottled: (Boolean) -> Unit,
    incrementBatch: () -> Unit,
    showToast: (String, Boolean) -> Unit,
    onParcelVerifiedForInspection: (String, String?) -> Unit,
    onDeliveryCompleted: (String) -> Unit
) {
    if (isThrottled) return
    setThrottled(true)

    try {
        when (mode) {
            ScannerMode.BATCH_PICKUP -> {
                val payload = Gson().fromJson(raw, ParcelQrPayload::class.java)
                if (payload.type != "MOHNA_PARCEL_LABEL" || payload.orderId.isEmpty()) {
                    SoundManager.playError()
                    showToast("⚠️ Not a valid Mohna parcel label.", true)
                    return
                }

                if (processedBatchIds.contains(payload.orderId)) {
                    showToast("ℹ️ ${payload.orderId} already collected!", false)
                    return
                }

                if (currentRider == null) {
                    SoundManager.playError()
                    showToast("⚠️ Rider not logged in.", true)
                    return
                }

                val success = repository.assignRiderViaScan(payload.orderId, payload.parcelToken, currentRider)
                if (success) {
                    processedBatchIds.add(payload.orderId)
                    incrementBatch()
                    SoundManager.playSuccess()
                    showToast("✅ Collected: ${payload.orderId}", false)
                } else {
                    SoundManager.playError()
                    showToast("⚠️ Failed to accept ${payload.orderId}", true)
                }
            }

            ScannerMode.STEP1_PARCEL -> {
                val payload = Gson().fromJson(raw, ParcelQrPayload::class.java)
                if (payload.type != "MOHNA_PARCEL_LABEL" || payload.orderId.isEmpty()) {
                    SoundManager.playError()
                    showToast("⚠️ Not a valid parcel label QR!", true)
                    return
                }

                if (targetOrderId != null && payload.orderId != targetOrderId) {
                    SoundManager.playError()
                    showToast("⚠️ Wrong parcel! Expected $targetOrderId", true)
                    return
                }

                val riderOrders = repository.orders.value.filter {
                    it.status.contains("OUT", ignoreCase = true) &&
                    (currentRider == null || it.riderEmail.equals(currentRider.email, ignoreCase = true))
                }
                val matched = riderOrders.find { it.orderId == payload.orderId }
                if (matched == null) {
                    SoundManager.playError()
                    showToast("⚠️ Order ${payload.orderId} is not in your In-Transit list!", true)
                    return
                }

                SoundManager.playSuccess()
                onParcelVerifiedForInspection(matched.orderId, payload.parcelToken ?: matched.parcelToken)
            }

            ScannerMode.STEP2_CUSTOMER -> {
                val payload = Gson().fromJson(raw, CustomerQrPayload::class.java)
                if (payload.type != "MOHNA_DELIVERY_HANDOVER" || (targetOrderId != null && payload.orderId != targetOrderId)) {
                    SoundManager.playError()
                    showToast("❌ Handover Failed: Customer QR does not match!", true)
                    return
                }

                val (success, msg) = repository.verifyTwoFactorDelivery(
                    orderId = payload.orderId,
                    parcelToken = null,
                    deliveryToken = payload.deliveryToken
                )

                if (success) {
                    SoundManager.playSuccess()
                    onDeliveryCompleted(payload.orderId)
                } else {
                    SoundManager.playError()
                    showToast("❌ $msg", true)
                }
            }
        }
    } catch (_: Exception) {
        SoundManager.playError()
        showToast("⚠️ Unrecognized QR format.", true)
    } finally {
        delay(900)
        setThrottled(false)
    }
}
