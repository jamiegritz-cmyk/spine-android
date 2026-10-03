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
 * Procedural ultra-fine tactile grain texture brush.
 * Generates an extremely fine grain / soft paper or lightly brushed material texture
 * that adds organic tactile tooth to dark surfaces without obvious noise, speckles, or patterns.
 */
@Composable
fun rememberTactileTextureBrush(
    baseColor: Color = Color(0xFF141312),
    grainVariance: Int = 3,
    seed: Long = 42L
): ShaderBrush {
    return remember(baseColor, grainVariance, seed) {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val random = Random(seed)
        val pixels = IntArray(size * size)

        val rBase = (baseColor.red * 255f).toInt()
        val gBase = (baseColor.green * 255f).toInt()
        val bBase = (baseColor.blue * 255f).toInt()

        for (i in pixels.indices) {
            // Soft Gaussian micro-variation for fine paper-like tooth
            val delta = (random.nextGaussian() * grainVariance).toInt().coerceIn(-grainVariance * 2, grainVariance * 2)
            val r = (rBase + delta).coerceIn(0, 255)
            val g = (gBase + delta).coerceIn(0, 255)
            val b = (bBase + delta).coerceIn(0, 255)
            pixels[i] = android.graphics.Color.rgb(r, g, b)
        }

        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        val shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        ShaderBrush(shader)
    }
}
