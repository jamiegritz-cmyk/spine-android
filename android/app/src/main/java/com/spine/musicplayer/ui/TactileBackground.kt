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
 * Creates an authentic dark charcoal/black physical material surface with soft organic
 * tooth and visibly perceptible, fine irregular handling wear/hairline scratches.
 * Free of digital static, TV noise, speckles, or artificial gradients.
 */
@Composable
fun rememberTactileTextureBrush(
    baseColor: Color = Color(0xFF101114),
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

        // 1. Generate smooth, seamlessly wrap-around multi-scale organic tooth grid
        // Octave 1: 32x32 cells (macro surface texture)
        val gridStep1 = 32
        val gridW1 = size / gridStep1
        val grid1 = FloatArray(gridW1 * gridW1)
        for (i in grid1.indices) {
            grid1[i] = (random.nextGaussian() * 3.8).toFloat().coerceIn(-8.0f, 8.0f)
        }

        // Octave 2: 16x16 cells (micro paper/slate fibers)
        val gridStep2 = 16
        val gridW2 = size / gridStep2
        val grid2 = FloatArray(gridW2 * gridW2)
        for (i in grid2.indices) {
            grid2[i] = (random.nextGaussian() * 2.2).toFloat().coerceIn(-5.0f, 5.0f)
        }

        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            // Sample Octave 1
            val gy0_1 = (y / gridStep1) % gridW1
            val gy1_1 = (gy0_1 + 1) % gridW1
            val ty1 = (y % gridStep1).toFloat() / gridStep1.toFloat()
            val sy1 = ty1 * ty1 * (3f - 2f * ty1)

            // Sample Octave 2
            val gy0_2 = (y / gridStep2) % gridW2
            val gy1_2 = (gy0_2 + 1) % gridW2
            val ty2 = (y % gridStep2).toFloat() / gridStep2.toFloat()
            val sy2 = ty2 * ty2 * (3f - 2f * ty2)

            for (x in 0 until size) {
                // Octave 1 interpolation
                val gx0_1 = (x / gridStep1) % gridW1
                val gx1_1 = (gx0_1 + 1) % gridW1
                val tx1 = (x % gridStep1).toFloat() / gridStep1.toFloat()
                val sx1 = tx1 * tx1 * (3f - 2f * tx1)

                val v00_1 = grid1[gy0_1 * gridW1 + gx0_1]
                val v10_1 = grid1[gy0_1 * gridW1 + gx1_1]
                val v01_1 = grid1[gy1_1 * gridW1 + gx0_1]
                val v11_1 = grid1[gy1_1 * gridW1 + gx1_1]
                val top1 = v00_1 + sx1 * (v10_1 - v00_1)
                val bot1 = v01_1 + sx1 * (v11_1 - v01_1)
                val oct1 = top1 + sy1 * (bot1 - top1)

                // Octave 2 interpolation
                val gx0_2 = (x / gridStep2) % gridW2
                val gx1_2 = (gx0_2 + 1) % gridW2
                val tx2 = (x % gridStep2).toFloat() / gridStep2.toFloat()
                val sx2 = tx2 * tx2 * (3f - 2f * tx2)

                val v00_2 = grid2[gy0_2 * gridW2 + gx0_2]
                val v10_2 = grid2[gy0_2 * gridW2 + gx1_2]
                val v01_2 = grid2[gy1_2 * gridW2 + gx0_2]
                val v11_2 = grid2[gy1_2 * gridW2 + gx1_2]
                val top2 = v00_2 + sx2 * (v10_2 - v00_2)
                val bot2 = v01_2 + sx2 * (v11_2 - v01_2)
                val oct2 = top2 + sy2 * (bot2 - top2)

                val delta = (oct1 + oct2).toInt().coerceIn(-12, 12)

                val r = (rBase + delta).coerceIn(0, 255)
                val g = (gBase + delta).coerceIn(0, 255)
                val b = (bBase + delta).coerceIn(0, 255)
                pixels[y * size + x] = android.graphics.Color.rgb(r, g, b)
            }
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)

        // 2. Overlay visible, delicate physical hairline scratches & organic handling wear
        val canvas = Canvas(bitmap)
        val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.85f
        }
        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.85f
        }

        // Draw ~26 subtle but distinctly perceptible hairline scratches
        for (i in 0 until 26) {
            val x0 = random.nextFloat() * size
            val y0 = random.nextFloat() * size
            val angle = (random.nextFloat() * 1.1f + 0.20f) * Math.PI.toFloat()
            val len = 14f + random.nextFloat() * 32f
            val x1 = x0 + Math.cos(angle.toDouble()).toFloat() * len
            val y1 = y0 + Math.sin(angle.toDouble()).toFloat() * len

            val isLight = random.nextBoolean()
            if (isLight) {
                val alpha = (28 + random.nextInt(26)) // Clearly visible on OLED without being bright
                lightPaint.color = android.graphics.Color.argb(alpha, 225, 225, 230)
                canvas.drawLine(x0, y0, x1, y1, lightPaint)
            } else {
                val alpha = (35 + random.nextInt(30))
                darkPaint.color = android.graphics.Color.argb(alpha, 0, 0, 0)
                canvas.drawLine(x0, y0, x1, y1, darkPaint)
            }
        }

        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        ShaderBrush(shader)
    }
}
