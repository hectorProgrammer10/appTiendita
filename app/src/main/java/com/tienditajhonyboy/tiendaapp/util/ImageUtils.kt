package com.tienditajhonyboy.tiendaapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageUtils {

    private const val MAX_DIMENSION = 700
    private const val COMPRESS_QUALITY = 80 // 0-100
    private const val IMAGE_DIR_NAME = "app_product_images"

    /**
     * Re-sizes and compresses the given image URI and saves it internally.
     * Returns the absolute path URI as a String to save in Room DB.
     */
    suspend fun saveAndCompressImage(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)
            options.inJustDecodeBounds = false

            val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            } ?: return@withContext null

            val scaledBitmap = scaleBitmapToMaxDimensions(bitmap, MAX_DIMENSION)

            val directory = context.getDir(IMAGE_DIR_NAME, Context.MODE_PRIVATE)
            if (!directory.exists()) directory.mkdirs()

            val fileName = "IMG_PROD_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out)
            }

            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }

            return@withContext Uri.fromFile(file).toString()

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Tries to delete a permanently saved image from app storage.
     */
    fun deleteImageFile(uriString: String?) {
        if (uriString.isNullOrEmpty()) return
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val file = uri.path?.let { File(it) }
                if (file != null && file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Encodes a local file URI to a Base64 string for exporting.
     */
    fun encodeImageToBase64(uriString: String?): String? {
        if (uriString.isNullOrEmpty()) return null
        return try {
            val file = File(Uri.parse(uriString).path ?: return null)
            if (!file.exists()) return null
            val bytes = file.readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decodes a Base64 string and saves it as a JPG in the app internal storage.
     */
    suspend fun saveBase64ToImage(context: Context, base64Str: String?): String? = withContext(Dispatchers.IO) {
        if (base64Str.isNullOrEmpty()) return@withContext null
        try {
            val bytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
            val directory = context.getDir(IMAGE_DIR_NAME, Context.MODE_PRIVATE)
            if (!directory.exists()) directory.mkdirs()

            val fileName = "IMG_PROD_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            FileOutputStream(file).use { it.write(bytes) }

            return@withContext Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun scaleBitmapToMaxDimensions(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap // No scaling needed
        }

        val ratio = width.toFloat() / height.toFloat()
        val finalWidth: Int
        val finalHeight: Int
        if (width > height) {
            finalWidth = maxDimension
            finalHeight = (maxDimension / ratio).toInt()
        } else {
            finalHeight = maxDimension
            finalWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
}
