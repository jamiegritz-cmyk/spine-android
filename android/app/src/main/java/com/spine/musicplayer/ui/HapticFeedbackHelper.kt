package com.spine.musicplayer.ui

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Dedicated native Android haptic feedback executor.
 * Tailored for Google Pixel haptic engine (Pixel 9 / Android 14+),
 * combining hardware vibrator actuator with touch attributes and View haptic flags
 * to ensure exactly ONE subtle mechanical tick fires per CD transition.
 */
class HapticFeedbackHelper(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Performs a single subtle mechanical tick haptic event on centered-CD state transition.
     */
    fun performCdTick(view: View? = null) {
        // 1. View-based Haptic with flags to override muted settings
        if (view != null) {
            try {
                view.isHapticFeedbackEnabled = true
                val flags = HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING or
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK, flags)
            } catch (_: Exception) {
                try {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                } catch (_: Exception) {
                    // Ignore
                }
            }
        }

        // 2. Hardware Vibrator / Actuator with TOUCH usage attributes (crucial on Pixel 9)
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attributes = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_TOUCH)
                        .build()
                    val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    vibrator.vibrate(effect, attributes)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    vibrator.vibrate(effect)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(12)
                }
            }
        } catch (_: Exception) {
            // Hardware fallback handled
        }
    }
}
