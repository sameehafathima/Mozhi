package com.nit.voicelibrarymvp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

class TesseractManager(
    private val context: Context,
    private val languageCode: String = "mal+eng"
) {

    private val TAG = "TesseractManager"

    init {
        if (languageCode.contains("mal")) copyLanguageFile("mal.traineddata")
        if (languageCode.contains("eng")) copyLanguageFile("eng.traineddata")
    }

    private fun copyLanguageFile(fileName: String) {
        val tessDir = File(context.filesDir, "tessdata")
        if (!tessDir.exists()) {
            tessDir.mkdirs()
        }

        val trainedDataFile = File(tessDir, fileName)
        if (!trainedDataFile.exists()) {
            try {
                context.assets.open("tessdata/$fileName").use { input ->
                    FileOutputStream(trainedDataFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "Successfully copied $fileName to $trainedDataFile")
            } catch (e: Exception) {
                Log.e(TAG, "Error copying $fileName from assets", e)
            }
        }
    }

    /**
     * Advanced preprocessing for Malayalam script.
     * Malayalam has complex curves and delicate vowel signs (chinnangal).
     */
    private fun preprocessBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        // 1. Significant Scaling (only if small)
        // Complex Malayalam characters need enough pixels to be resolved correctly.
        // We target a reasonable size (e.g., max 2000px) to avoid OutOfMemoryError.
        val targetMaxDimension = 2000
        val currentMaxDimension = maxOf(width, height)
        
        val scaleFactor = if (currentMaxDimension < targetMaxDimension) {
            targetMaxDimension.toFloat() / currentMaxDimension
        } else {
            1.0f
        }
        
        val scaledWidth = (width * scaleFactor).toInt()
        val scaledHeight = (height * scaleFactor).toInt()
        
        val scaledBitmap = if (scaleFactor != 1.0f) {
            Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        } else {
            bitmap
        }

        // 2. Convert to Grayscale with high contrast (Using RGB_565 to save 50% RAM)
        val processedBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(processedBitmap)
        val paint = Paint()
        
        // Custom color matrix for higher contrast and grayscale
        val colorMatrix = ColorMatrix(floatArrayOf(
            1.5f, 1.5f, 1.5f, 0f, -100f,
            1.5f, 1.5f, 1.5f, 0f, -100f,
            1.5f, 1.5f, 1.5f, 0f, -100f,
            0f,   0f,   0f,   1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
        
        // Immediate clean up of intermediate bitmap
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        
        return processedBitmap
    }

    fun extractText(bitmap: Bitmap): String {
        val tessBaseAPI = TessBaseAPI()
        var processed: Bitmap? = null
        try {
            val datapath = context.filesDir.absolutePath
            
            // LSTM Engine is significantly better for Malayalam
            val success = tessBaseAPI.init(datapath, languageCode)
            if (!success) return ""

            // Tuning for Malayalam accuracy
            if (languageCode.contains("mal")) {
                tessBaseAPI.setVariable("load_system_dawg", "false")
                tessBaseAPI.setVariable("load_freq_dawg", "false")
                tessBaseAPI.setVariable("textord_heavy_nr", "true") 
            }

            tessBaseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO_OSD
            
            processed = preprocessBitmap(bitmap)
            tessBaseAPI.setImage(processed)
            
            val text = tessBaseAPI.utF8Text
            Log.d(TAG, "Extracted Text: $text")
            
            return text ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Extraction error", e)
            return ""
        } finally {
            tessBaseAPI.recycle()
            processed?.recycle() // Free memory immediately
        }
    }
}
