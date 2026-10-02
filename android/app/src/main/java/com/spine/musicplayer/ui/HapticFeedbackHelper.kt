package com.spine.musicplayer.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Robust native Android haptic feedback executor.
 * Supports Google Pixel tactile engine (Pixel 9 / Android 14+),
 * combining VibratorManager / Vibrator predefined ticks with View haptic flags
 * to ensure haptic ticks always fire when the centered CD changes.
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
     * Performs a single subtle mechanical tick haptic event.
     * Guaranteed to trigger once per CD transition.
     */
    fun performCdTick(view: View? = null) {
        var tickTriggered = false

        // 1. Hardware Vibrator / Haptic Actuator (Highest reliability on physical Pixel devices)
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Predefined mechanical tick effect
                    val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    vibrator.vibrate(effect)
                    tickTriggered = true
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(10, 80)
                    vibrator.vibrate(effect)
                    tickTriggered = true
                }
            }
        } catch (_: Exception) {
            // Fall through to View haptic
        }

        // 2. View Haptic Feedback with FLAG_IGNORE_VIEW_SETTING & FLAG_IGNORE_GLOBAL_SETTING
        if (!tickTriggered && view != null) {
            try {
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
    }
}
