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
 * Procedural dark physical material brush.
 * Provides broad, soft, organic low-frequency tonal variation across charcoal black (#090A0C to #101114).
 * Completely free of scratches, diagonal lines, digital noise, grain, speckles, or static.
 */
@Composable
fun rememberTactileTextureBrush(
    baseColor: Color = Color(0xFF0D0E10),
    grainVariance: Int = 2,
    seed: Long = 42L
): ShaderBrush {
    return remember(baseColor, seed) {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val random = Random(seed)

        val rBase = (baseColor.red * 255f).toInt()
        val gBase = (baseColor.green * 255f).toInt()
        val bBase = (baseColor.blue * 255f).toInt()

        // Broad, soft low-frequency organic tonal variation (grid step 128 = broad undulating smooth patches)
        val gridStep = 128
        val gridW = size / gridStep
        val grid = FloatArray(gridW * gridW)
        for (i in grid.indices) {
            // Extremely low contrast: 1-2 levels variation around charcoal black
            grid[i] = (random.nextGaussian() * 1.0).toFloat().coerceIn(-2.0f, 2.0f)
        }

        val pixels = IntArray(size * size)
        for (y in 0 until size) {
            val gy0 = (y / gridStep) % gridW
            val gy1 = (gy0 + 1) % gridW
            val ty = (y % gridStep).toFloat() / gridStep.toFloat()
            // Smooth cubic Hermite interpolation curve: 3t^2 - 2t^3
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
                // Variation strictly limited to 1-2 levels for dark tactile physical material
                val delta = (top + sy * (bot - top)).toInt().coerceIn(-2, 2)

                val r = (rBase + delta).coerceIn(9, 16)
                val g = (gBase + delta).coerceIn(10, 17)
                val b = (bBase + delta).coerceIn(12, 20)
                pixels[y * size + x] = android.graphics.Color.rgb(r, g, b)
            }
        }
        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)

        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        ShaderBrush(shader)
    }
}
