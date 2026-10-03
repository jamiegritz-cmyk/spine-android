package com.spine.musicplayer.ui

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import java.util.Random

/**
 * Procedural dark matte physical material brush.
 * Provides broad, soft, natural non-repeating tonal variation across very dark black/charcoal.
 * Completely free of scratches, lines, digital noise, grain, speckles, or static.
 */
@Composable
fun rememberTactileTextureBrush(
    baseColor: Color = Color(0xFF0E0F11),
    grainVariance: Int = 3,
    seed: Long = 42L
): ShaderBrush {
    return remember(baseColor, seed) {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val random = Random(seed)

        val rBase = (baseColor.red * 255f).toInt()
        val gBase = (baseColor.green * 255f).toInt()
        val bBase = (baseColor.blue * 255f).toInt()

        // Broad, smooth organic tonal modulation (grid step 64 = broad undulating soft patches)
        val gridStep = 64
        val gridW = size / gridStep
        val grid = FloatArray(gridW * gridW)
        for (i in grid.indices) {
            // Extremely low-contrast variation (-3 to +3 RGB steps around near-black)
            grid[i] = (random.nextGaussian() * 1.5).toFloat().coerceIn(-3.0f, 3.0f)
        }

        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val gy0 = (y / gridStep) % gridW
            val gy1 = (gy0 + 1) % gridW
            val ty = (y % gridStep).toFloat() / gridStep.toFloat()
            // Smooth cubic Hermite interpolation curve
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
                val delta = (top + sy * (bot - top)).toInt().coerceIn(-3, 3)

                val r = (rBase + delta).coerceIn(0, 255)
                val g = (gBase + delta).coerceIn(0, 255)
                val b = (bBase + delta).coerceIn(0, 255)
                pixels[y * size + x] = android.graphics.Color.rgb(r, g, b)
            }
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)

        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        ShaderBrush(shader)
    }
}
