package com.example.engine

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import com.example.model.ColorAdjustment
import com.example.model.EffectType
import com.example.model.TransitionType
import kotlin.math.PI
import kotlin.math.sin

object EffectRenderer {

    fun createColorFilter(adj: ColorAdjustment, effect: EffectType, timeMs: Long): ColorMatrixColorFilter {
        val cm = ColorMatrix()

        // 1. Brightness (-1 .. 1)
        var brightnessOffset = adj.brightness * 255f
        if (effect == EffectType.FLASH) {
            val flashFreq = (timeMs % 400L) / 400f
            brightnessOffset += (1f - flashFreq) * 120f
        }

        // 2. Contrast (0 .. 2, default 1)
        val contrast = adj.contrast
        val contrastScale = contrast
        val contrastTranslate = (-0.5f * contrastScale + 0.5f) * 255f

        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrastScale, 0f, 0f, 0f, contrastTranslate + brightnessOffset,
                0f, contrastScale, 0f, 0f, contrastTranslate + brightnessOffset,
                0f, 0f, contrastScale, 0f, contrastTranslate + brightnessOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)

        // 3. Saturation (0 .. 2, default 1)
        var sat = adj.saturation
        if (effect == EffectType.VHS_RETRO) {
            sat *= 0.7f // slightly desaturated vintage
        }
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(sat.coerceIn(0f, 3f))
        cm.postConcat(satMatrix)

        // 4. Temperature / Tint
        if (adj.temperature != 0f || adj.tint != 0f || effect == EffectType.VHS_RETRO) {
            val temp = adj.temperature + (if (effect == EffectType.VHS_RETRO) 0.25f else 0f)
            val tint = adj.tint
            val tempMatrix = ColorMatrix(
                floatArrayOf(
                    1f + temp * 0.3f, 0f, 0f, 0f, 0f,
                    0f, 1f + tint * 0.2f, 0f, 0f, 0f,
                    0f, 0f, 1f - temp * 0.3f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(tempMatrix)
        }

        // 5. Glitch / RGB Split effects color shift
        if (effect == EffectType.RGB_SPLIT || effect == EffectType.GLITCH) {
            val glitchPhase = sin(timeMs / 120.0).toFloat()
            val glitchShift = ColorMatrix(
                floatArrayOf(
                    1.2f, 0f, 0f, 0f, glitchPhase * 25f,
                    0f, 0.9f, 0f, 0f, 0f,
                    0f, 0f, 1.3f, 0f, -glitchPhase * 25f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            cm.postConcat(glitchShift)
        }

        return ColorMatrixColorFilter(cm)
    }

    data class TransitionTransform(
        val alphaA: Float = 1f,
        val alphaB: Float = 0f,
        val offsetXA: Float = 0f,
        val offsetYA: Float = 0f,
        val offsetXB: Float = 0f,
        val offsetYB: Float = 0f,
        val scaleA: Float = 1f,
        val scaleB: Float = 1f,
        val rotationA: Float = 0f,
        val rotationB: Float = 0f,
        val flashAlpha: Float = 0f
    )

    fun calculateTransition(type: TransitionType, progress: Float): TransitionTransform {
        val p = progress.coerceIn(0f, 1f)
        return when (type) {
            TransitionType.NONE -> TransitionTransform(alphaA = 1f, alphaB = 0f)
            TransitionType.FADE -> {
                // Fade to black then fade in
                if (p < 0.5f) {
                    TransitionTransform(alphaA = 1f - p * 2f, alphaB = 0f)
                } else {
                    TransitionTransform(alphaA = 0f, alphaB = (p - 0.5f) * 2f)
                }
            }
            TransitionType.DISSOLVE -> {
                TransitionTransform(alphaA = 1f - p, alphaB = p)
            }
            TransitionType.ZOOM_IN -> {
                TransitionTransform(
                    alphaA = (1f - p * 1.5f).coerceAtLeast(0f),
                    alphaB = (p * 1.5f - 0.5f).coerceAtLeast(0f).coerceAtMost(1f),
                    scaleA = 1f + p * 0.8f,
                    scaleB = 0.5f + p * 0.5f
                )
            }
            TransitionType.ZOOM_OUT -> {
                TransitionTransform(
                    alphaA = (1f - p),
                    alphaB = p,
                    scaleA = 1f - p * 0.4f,
                    scaleB = 1.6f - p * 0.6f
                )
            }
            TransitionType.SLIDE_LEFT -> {
                TransitionTransform(
                    alphaA = 1f,
                    alphaB = 1f,
                    offsetXA = -p,
                    offsetXB = 1f - p
                )
            }
            TransitionType.SLIDE_RIGHT -> {
                TransitionTransform(
                    alphaA = 1f,
                    alphaB = 1f,
                    offsetXA = p,
                    offsetXB = -1f + p
                )
            }
            TransitionType.PUSH_UP -> {
                TransitionTransform(
                    alphaA = 1f,
                    alphaB = 1f,
                    offsetYA = -p,
                    offsetYB = 1f - p
                )
            }
            TransitionType.SPIN -> {
                TransitionTransform(
                    alphaA = 1f - p,
                    alphaB = p,
                    rotationA = p * 180f,
                    rotationB = -180f + p * 180f,
                    scaleA = 1f - p * 0.5f,
                    scaleB = 0.5f + p * 0.5f
                )
            }
            TransitionType.FLASH_WHITE -> {
                val flash = sin(p * PI).toFloat()
                TransitionTransform(
                    alphaA = if (p < 0.5f) 1f else 0f,
                    alphaB = if (p >= 0.5f) 1f else 0f,
                    flashAlpha = flash
                )
            }
            TransitionType.GLITCH_WIPE -> {
                val glitchStep = (p * 5).toInt() / 5f
                TransitionTransform(
                    alphaA = 1f - glitchStep,
                    alphaB = glitchStep,
                    offsetXA = if (p in 0.3f..0.7f) (sin(p * 50f) * 0.05f) else 0f,
                    offsetXB = if (p in 0.3f..0.7f) (-sin(p * 50f) * 0.05f) else 0f
                )
            }
            TransitionType.WIPE_RIGHT -> {
                TransitionTransform(
                    alphaA = (1f - p).coerceIn(0f, 1f),
                    alphaB = 1f,
                    offsetXB = -1f + p
                )
            }
            TransitionType.CUBE_3D -> {
                TransitionTransform(
                    alphaA = 1f - p,
                    alphaB = p,
                    scaleA = 1f - p * 0.25f,
                    scaleB = 0.75f + p * 0.25f,
                    rotationA = p * 45f,
                    rotationB = -45f + p * 45f
                )
            }
        }
    }
}
