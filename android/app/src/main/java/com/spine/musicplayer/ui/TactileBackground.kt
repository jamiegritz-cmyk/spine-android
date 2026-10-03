package com.spine.musicplayer.ui

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import java.util.Random

/**
 * Procedural physical tactile surface brush.
 * Creates a very dark charcoal/black physical material surface with soft organic
 * tooth and ultra-subtle, fine irregular handling wear/hairline scratches.
 * Free of digital static, tv noise, speckles, or harsh gradients.
 */
@Composable
fun rememberTactileTextureBrush(
    baseColor: Color = Color(0xFF0F1012),
    grainVariance: Int = 3,
    seed: Long = 42L
): ShaderBrush {
    return remember(baseColor, grainVariance, seed) {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val random = Random(seed)

        val rBase = (baseColor.red * 255f).toInt()
        val gBase = (baseColor.green * 255f).toInt()
        val bBase = (baseColor.blue * 255f).toInt()

        // 1. Generate smooth, seamlessly wrap-around low-frequency organic tooth grid (32x32 cells)
        val gridStep = 32
        val gridW = size / gridStep
        val gridH = size / gridStep
        val grid = FloatArray(gridW * gridH)
        for (i in grid.indices) {
            grid[i] = (random.nextGaussian() * 1.6).toFloat().coerceIn(-3.5f, 3.5f)
        }

        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val gy0 = (y / gridStep) % gridH
            val gy1 = (gy0 + 1) % gridH
            val ty = (y % gridStep).toFloat() / gridStep.toFloat()
            val sy = ty * ty * (3f - 2f * ty)

            for (x in 0 until size) {
                val gx0 = (x / gridStep) % gridW
                val gx1 = (gx0 + 1) % gridW
                val tx = (x % gridStep).toFloat() / gridStep.toFloat()
                val sx = tx * tx * (3f - 2f * tx)

                val v00 = grid[gy0 * gridW + gx0]
                val v10 = grid[gy0 * gridW + gx1]
                val v01 = grid[gy1 * gridW + gx0]
                val v11 = grid[gy1 * gridW + gx1]

                val top = v00 + sx * (v10 - v00)
                val bot = v01 + sx * (v11 - v01)
                val delta = (top + sy * (bot - top)).toInt()

                val r = (rBase + delta).coerceIn(0, 255)
                val g = (gBase + delta).coerceIn(0, 255)
                val b = (bBase + delta).coerceIn(0, 255)
                pixels[y * size + x] = android.graphics.Color.rgb(r, g, b)
            }
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)

        // 2. Overlay very subtle, fine irregular hairline scratches and handling wear
        val canvas = Canvas(bitmap)
        val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.7f
        }
        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.7f
        }

        // Draw ~22 subtle microscopic wear hairlines
        for (i in 0 until 22) {
            val x0 = random.nextFloat() * size
            val y0 = random.nextFloat() * size
            val angle = (random.nextFloat() * 1.1f + 0.25f) * Math.PI.toFloat()
            val len = 10f + random.nextFloat() * 24f
            val x1 = x0 + Math.cos(angle.toDouble()).toFloat() * len
            val y1 = y0 + Math.sin(angle.toDouble()).toFloat() * len

            val isLight = random.nextBoolean()
            if (isLight) {
                val alpha = (8 + random.nextInt(12)) // extremely subtle alpha
                lightPaint.color = android.graphics.Color.argb(alpha, 235, 235, 240)
                canvas.drawLine(x0, y0, x1, y1, lightPaint)
            } else {
                val alpha = (10 + random.nextInt(14))
                darkPaint.color = android.graphics.Color.argb(alpha, 0, 0, 0)
                canvas.drawLine(x0, y0, x1, y1, darkPaint)
            }
        }

        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        ShaderBrush(shader)
    }
}
