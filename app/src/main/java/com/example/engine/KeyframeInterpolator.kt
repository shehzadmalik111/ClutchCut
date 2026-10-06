package com.example.engine

import com.example.model.ClipKeyframe

object KeyframeInterpolator {

    data class InterpolatedTransform(
        val posX: Float,
        val posY: Float,
        val scale: Float,
        val rotation: Float,
        val opacity: Float,
        val volume: Float
    )

    fun interpolate(
        keyframes: List<ClipKeyframe>,
        timeOffsetMs: Long,
        defaultTransform: InterpolatedTransform = InterpolatedTransform(0f, 0f, 1f, 0f, 1f, 1f)
    ): InterpolatedTransform {
        if (keyframes.isEmpty()) return defaultTransform
        val sorted = keyframes.sortedBy { it.timeOffsetMs }

        // Before first keyframe
        if (timeOffsetMs <= sorted.first().timeOffsetMs) {
            val k = sorted.first()
            return InterpolatedTransform(k.posX, k.posY, k.scale, k.rotation, k.opacity, k.volume)
        }

        // After last keyframe
        if (timeOffsetMs >= sorted.last().timeOffsetMs) {
            val k = sorted.last()
            return InterpolatedTransform(k.posX, k.posY, k.scale, k.rotation, k.opacity, k.volume)
        }

        // Between two keyframes
        for (i in 0 until sorted.size - 1) {
            val k1 = sorted[i]
            val k2 = sorted[i + 1]
            if (timeOffsetMs in k1.timeOffsetMs..k2.timeOffsetMs) {
                val span = (k2.timeOffsetMs - k1.timeOffsetMs).toFloat().coerceAtLeast(1f)
                val fraction = (timeOffsetMs - k1.timeOffsetMs) / span
                val eased = easeInOut(fraction)

                return InterpolatedTransform(
                    posX = lerp(k1.posX, k2.posX, eased),
                    posY = lerp(k1.posY, k2.posY, eased),
                    scale = lerp(k1.scale, k2.scale, eased),
                    rotation = lerp(k1.rotation, k2.rotation, eased),
                    opacity = lerp(k1.opacity, k2.opacity, eased),
                    volume = lerp(k1.volume, k2.volume, eased)
                )
            }
        }

        return defaultTransform
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction
    }

    private fun easeInOut(t: Float): Float {
        return if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
    }
}
