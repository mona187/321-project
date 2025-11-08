package com.example.cpen_321.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Helper object for image encoding with compression
 */
object Base64ImageHelper {

    /**
     * Encode image to Base64 with compression
     * Compresses image to max 800x800 and 70% quality
     *
     * @param imageUri The URI of the selected image
     * @param context Android context
     * @return Result with Base64 string
     */
    suspend fun encodeImageToBase64(
        imageUri: Uri,
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        var originalBitmap: Bitmap? = null
        var compressedBitmap: Bitmap? = null

        try {
            // 1. Read image from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                return@withContext Result.failure(Exception("Failed to decode image"))
            }

            // 2. Compress image
            compressedBitmap = compressBitmap(originalBitmap)

            // 3. Convert to JPEG bytes with quality compression
            val outputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val imageBytes = outputStream.toByteArray()
            outputStream.close()

            // 4. Encode to Base64
            val base64 = android.util.Base64.encodeToString(
                imageBytes,
                android.util.Base64.NO_WRAP
            )

            // 5. Add data URI prefix
            val result = "data:image/jpeg;base64,$base64"

            android.util.Log.d("Base64ImageHelper",
                "Image encoded successfully - " +
                        "Original: ${originalBitmap.width}x${originalBitmap.height}, " +
                        "Compressed: ${compressedBitmap.width}x${compressedBitmap.height}, " +
                        "Base64 length: ${result.length} chars"
            )

            Result.success(result)
        } catch (e: Exception) {
            android.util.Log.e("Base64ImageHelper", "Failed to encode image", e)
            Result.failure(e)
        } finally {
            // Clean up bitmaps AFTER everything is done
            try {
                originalBitmap?.recycle()
                compressedBitmap?.recycle()
            } catch (e: Exception) {
                android.util.Log.w("Base64ImageHelper", "Error recycling bitmaps", e)
            }
        }
    }

    /**
     * Compress bitmap to max 800x800 while maintaining aspect ratio
     */
    private fun compressBitmap(original: Bitmap): Bitmap {
        val maxSize = 800
        val width = original.width
        val height = original.height

        if (width <= maxSize && height <= maxSize) {
            return original.copy(original.config ?: Bitmap.Config.ARGB_8888, false)
        }

        val scale = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    }

    /**
     * Decode Base64 data URI to Bitmap
     */
    suspend fun decodeBase64ToBitmap(base64DataUri: String): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            // Remove data URI prefix if present
            val base64String = if (base64DataUri.startsWith("data:image/")) {
                base64DataUri.substringAfter("base64,")
            } else {
                base64DataUri
            }

            // Decode Base64 to bytes
            val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            
            // Convert bytes to Bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            
            if (bitmap == null) {
                Result.failure(Exception("Failed to decode Base64 to Bitmap"))
            } else {
                Result.success(bitmap)
            }
        } catch (e: Exception) {
            android.util.Log.e("Base64ImageHelper", "Failed to decode Base64", e)
            Result.failure(e)
        }
    }

    /**
     * Get approximate file size in KB
     */
    fun getBase64SizeKB(base64String: String): Double {
        // Remove data URI prefix if present
        val base64Only = base64String.substringAfter("base64,")
        // Base64 encoding increases size by ~33%
        return (base64Only.length * 0.75) / 1024.0
    }
}

/**
 * Composable function to create a painter for Base64 data URIs
 */
@Composable
fun rememberBase64ImagePainter(base64DataUri: String): androidx.compose.ui.graphics.painter.Painter {
    val context = LocalContext.current
    val bitmap = remember(base64DataUri) {
        kotlinx.coroutines.runBlocking {
            Base64ImageHelper.decodeBase64ToBitmap(base64DataUri).getOrNull()
        }
    }
    
    return remember(bitmap) {
        bitmap?.let { 
            BitmapPainter(it.asImageBitmap())
        } ?: androidx.compose.ui.graphics.painter.ColorPainter(androidx.compose.ui.graphics.Color.Gray)
    }
}