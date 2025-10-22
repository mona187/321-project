package com.example.cpen_321.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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

        // If image is already small enough, return copy instead of original
        if (width <= maxSize && height <= maxSize) {
            return original.copy(original.config ?: Bitmap.Config.ARGB_8888, false)
        }

        // Calculate scaling factor
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
     * Get approximate file size in KB
     */
    fun getBase64SizeKB(base64String: String): Double {
        // Remove data URI prefix if present
        val base64Only = base64String.substringAfter("base64,")
        // Base64 encoding increases size by ~33%
        return (base64Only.length * 0.75) / 1024.0
    }
}