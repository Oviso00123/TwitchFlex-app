package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.TwitchBlack
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    isFrontCamera: Boolean,
    hasCameraPermission: Boolean,
    onFrameCaptured: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
    isBlurred: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isCameraBound by remember { mutableStateOf(false) }

    if (hasCameraPermission) {
        val orientation = context.resources.configuration.orientation
        val previewView = remember {
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

        LaunchedEffect(isFrontCamera, orientation) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()

                    val cameraSelector = if (isFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    val displayRotation = try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            context.display?.rotation ?: android.view.Surface.ROTATION_0
                        } else {
                            @Suppress("DEPRECATION")
                            (context.getSystemService(android.content.Context.WINDOW_SERVICE) as? android.view.WindowManager)?.defaultDisplay?.rotation ?: android.view.Surface.ROTATION_0
                        }
                    } catch (_: Exception) {
                        android.view.Surface.ROTATION_0
                    }

                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER

                    // Dedicated dynamic 16:9 aspect ratio configuration with no stretching
                    val preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setTargetRotation(displayRotation)
                        .build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    // Image Analysis for local streaming frames matching aspect ratio
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setTargetRotation(displayRotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()

                    var lastFrameTime = 0L
                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val now = System.currentTimeMillis()
                        if (now - lastFrameTime >= 100) { // ~10 FPS
                            lastFrameTime = now
                            try {
                                val nv21 = yuv420ToNv21(imageProxy)
                                val yuvImage = YuvImage(
                                    nv21,
                                    ImageFormat.NV21,
                                    imageProxy.width,
                                    imageProxy.height,
                                    null
                                )
                                val out = ByteArrayOutputStream()
                                yuvImage.compressToJpeg(
                                    Rect(0, 0, imageProxy.width, imageProxy.height),
                                    70,
                                    out
                                )
                                val jpegBytes = out.toByteArray()
                                onFrameCaptured(jpegBytes)
                            } catch (_: Exception) {}
                        }
                        imageProxy.close()
                    }

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    isCameraBound = true
                } catch (_: Exception) {
                    isCameraBound = false
                }
            }, ContextCompat.getMainExecutor(context))
        }

        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                factory = { previewView },
                update = { view ->
                    view.scaleType = PreviewView.ScaleType.FILL_CENTER
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camera_preview_view")
            )

            // Privacy Blur Cam "Torno Subito" Tu Style Glass Overlay
            AnimatedVisibility(
                visible = isBlurred,
                enter = fadeIn(tween(350)),
                exit = fadeOut(tween(350)),
                modifier = Modifier.fillMaxSize()
            ) {
                val infinitePulse = rememberInfiniteTransition(label = "blur_pulse")
                val pulseGlow by infinitePulse.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_glow"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.90f))
                        .testTag("privacy_blur_cam_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF181822).copy(alpha = 0.92f))
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    listOf(TuStyleNeonCyan.copy(alpha = pulseGlow), TuStylePurple, Color.White.copy(alpha = 0.8f))
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("☕", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "TORNO SUBITO",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pausa breve per lo streamer • La diretta continua!",
                                color = TuStyleNeonCyan,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Fallback Animated Simulation when permission not granted or emulator
        CameraFallbackView(modifier = modifier, isBlurred = isBlurred)
    }
}

@Composable
fun CameraFallbackView(modifier: Modifier = Modifier, isBlurred: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TwitchBlack)
            .testTag("camera_fallback_view"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .rotate(rotation)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFF9146FF),
                            Color(0xFF00F5D4),
                            Color(0xFFFF2A6D),
                            Color(0xFF9146FF)
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .background(TwitchBlack, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🎥",
                    fontSize = 54.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "CAM DIRETTA SIMULATA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Trasmissione P2P & Live Attiva",
                    color = Color(0xFF00F5D4),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (isBlurred) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.90f))
                    .testTag("privacy_blur_cam_fallback_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF181822).copy(alpha = 0.92f))
                        .border(2.dp, TuStyleNeonCyan, RoundedCornerShape(24.dp))
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("☕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("TORNO SUBITO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Pausa breve per lo streamer • La diretta continua!", color = TuStyleNeonCyan, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun yuv420ToNv21(image: androidx.camera.core.ImageProxy): ByteArray {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + image.width * image.height / 2)
    yBuffer.get(nv21, 0, ySize)

    val uRowStride = image.planes[1].rowStride
    val uPixelStride = image.planes[1].pixelStride

    var pos = ySize
    val height = image.height
    val width = image.width

    for (row in 0 until height / 2) {
        for (col in 0 until width / 2) {
            val vIndex = row * uRowStride + col * uPixelStride
            nv21[pos++] = vBuffer.get(vIndex)
            nv21[pos++] = uBuffer.get(vIndex)
        }
    }

    return nv21
}
