package com.nit.voicelibrarymvp

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.tasks.await

object BarcodeHelper {
    private const val TAG = "BarcodeHelper"

    // Use ALL_FORMATS to ensure we don't miss anything, especially internal library labels
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    suspend fun scanQrCode(bitmap: Bitmap, rotation: Int = 0): String? {
        Log.d(TAG, "Scanning for QR Code. Bitmap: ${bitmap.width}x${bitmap.height}, Rotation: $rotation")
        val image = InputImage.fromBitmap(bitmap, rotation)
        return try {
            val barcodes = scanner.process(image).await()
            Log.d(TAG, "Scan complete. Found ${barcodes.size} barcodes.")
            
            // Log what was found for debugging
            barcodes.forEach { 
                Log.d(TAG, "Found barcode: ${it.rawValue} type: ${it.format}")
            }

            barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.let {
                Log.d(TAG, "Selected QR Code: ${it.rawValue}")
                it.rawValue
            }
        } catch (e: Exception) {
            Log.e(TAG, "QR Scan failed", e)
            null
        }
    }

    suspend fun scanIsbn(bitmap: Bitmap, rotation: Int = 0): String? {
        Log.d(TAG, "Scanning for ISBN. Bitmap: ${bitmap.width}x${bitmap.height}, Rotation: $rotation")
        val image = InputImage.fromBitmap(bitmap, rotation)
        return try {
            val barcodes = scanner.process(image).await()
            Log.d(TAG, "Scan complete. Found ${barcodes.size} barcodes.")
            
            barcodes.forEach { 
                Log.d(TAG, "Found barcode: ${it.rawValue} type: ${it.format}")
            }

            // ISBN logic:
            // 1. Filter for barcodes that have 10 or 13 digits (standard ISBN)
            // 2. Prefer EAN_13 (standard for modern book barcodes)
            val candidates = barcodes.filter { 
                val value = it.rawValue ?: ""
                val digitsOnly = value.filter { c -> c.isDigit() }
                digitsOnly.length == 13 || digitsOnly.length == 10
            }

            val result = candidates.firstOrNull { it.format == Barcode.FORMAT_EAN_13 }
                ?: candidates.firstOrNull()
                // If no 10/13 digit barcode found, fallback to any EAN_13 or UPC_A
                ?: barcodes.firstOrNull { it.format == Barcode.FORMAT_EAN_13 }
                ?: barcodes.firstOrNull { it.format == Barcode.FORMAT_UPC_A }
                ?: barcodes.firstOrNull()
            
            result?.let {
                val raw = it.rawValue ?: ""
                Log.d(TAG, "Selected Barcode: $raw (Format: ${it.format})")
                // Cleanup: some barcodes might have non-digit prefixes
                raw.filter { c -> c.isDigit() || c.uppercaseChar() == 'X' }
            }
        } catch (e: Exception) {
            Log.e(TAG, "ISBN Scan failed", e)
            null
        }
    }
}
