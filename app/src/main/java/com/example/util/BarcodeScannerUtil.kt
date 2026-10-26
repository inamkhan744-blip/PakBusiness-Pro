package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Utility for barcode scanning, camera availability checks,
 * and haptic feedback on successful inventory scans.
 */
object BarcodeScannerUtil {

    /**
     * Checks if the device has an available physical camera hardware module.
     */
    fun hasCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    /**
     * Triggers a brief haptic feedback vibration when a barcode is successfully detected.
     */
    fun triggerSuccessHaptic(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        } catch (_: Exception) {
            // Safe fallback if vibration permission or hardware is absent
        }
    }

    /**
     * Cleans and formats scanned barcode strings (removes leading/trailing whitespace, control characters).
     */
    fun sanitizeBarcode(rawCode: String): String {
        return rawCode.trim().filter { it.isLetterOrDigit() || it == '-' || it == '_' }
    }

    /**
     * Returns true if the barcode string adheres to standard commercial symbology lengths:
     * EAN-13 (13 digits), UPC-A (12 digits), EAN-8 (8 digits), or Code 128 (alphanumeric).
     */
    fun isValidBarcode(code: String): Boolean {
        val sanitized = sanitizeBarcode(code)
        return sanitized.length in 4..48
    }
}
