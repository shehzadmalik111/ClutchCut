package com.example.model

import java.util.UUID

enum class AspectRatioEnum(
    val displayName: String,
    val ratioWidth: Float,
    val ratioHeight: Float,
    val exportWidth: Int,
    val exportHeight: Int
) {
    RATIO_9_16("9:16 (TikTok/Reels)", 9f, 16f, 1080, 1920),
    RATIO_16_9("16:9 (YouTube)", 16f, 9f, 1920, 1080),
    RATIO_1_1("1:1 (Square)", 1f, 1f, 1080, 1080),
    RATIO_4_5("4:5 (Portrait Feed)", 4f, 5f, 1080, 1350),
    RATIO_3_4("3:4 (Classic)", 3f, 4f, 1080, 1440);

    val ratio: Float get() = ratioWidth / ratioHeight
}

data class ColorAdjustment(
    val brightness: Float = 0f,     // -1f .. 1f
    val contrast: Float = 1f,       // 0f .. 2f
    val saturation: Float = 1f,     // 0f .. 2f
    val exposure: Float = 0f,       // -1f .. 1f
    val temperature: Float = 0f,    // -1f .. 1f
    val tint: Float = 0f,           // -1f .. 1f
    val highlights: Float = 0f,     // -1f .. 1f
    val shadows: Float = 0f,        // -1f .. 1f
    val sharpness: Float = 0f,      // 0f .. 1f
    val vignette: Float = 0f,       // 0f .. 1f
    val fade: Float = 0f            // 0f .. 1f
)

enum class EffectType(val displayName: String, val icon: String) {
    NONE("None", "none"),
    GLITCH("Glitch", "glitch"),
    RGB_SPLIT("RGB Split", "rgb"),
    SHAKE("Shake", "shake"),
    ZOOM_BURST("Zoom", "zoom"),
    FLASH("Flash", "flash"),
    VHS_RETRO("VHS 80s", "vhs"),
    FILM_GRAIN("Film", "film"),
    NOISE("Noise", "noise"),
    BLUR("Blur", "blur"),
    MOTION_BLUR("Motion Blur", "motion"),
    CHROMATIC_ABERRATION("Chromatic", "prism"),
    GLOW("Glow", "glow"),
    VIGNETTE_DARK("Vignette", "vignette"),
    LENS_DISTORTION("Lens Distort", "lens")
}

enum class TransitionType(val displayName: String) {
    NONE("None"),
    FADE("Fade to Black"),
    DISSOLVE("Dissolve"),
    ZOOM_IN("Zoom In"),
    ZOOM_OUT("Zoom Out"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    PUSH_UP("Push Up"),
    SPIN("Spin 360"),
    GLITCH_WIPE("Glitch Wipe"),
    FLASH_WHITE("Flash White"),
    WIPE_RIGHT("Wipe Right"),
    CUBE_3D("3D Flip")
}

enum class TextAnimType(val displayName: String) {
    NONE("Static"),
    TYPEWRITER("Typewriter"),
    POP_UP("Pop-Up"),
    FADE_IN("Fade In"),
    SHAKE_BOUNCE("Shake & Bounce"),
    GLITCH_TEXT("Glitch")
}

data class ClipKeyframe(
    val timeOffsetMs: Long,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val volume: Float = 1f
)

data class SpeedPoint(
    val timeFraction: Float, // 0..1
    val speedMultiplier: Float // 0.1 .. 10.0
)

data class VideoClip(
    val id: String = UUID.randomUUID().toString(),
    val uri: String? = null,
    val title: String = "Clip",
    val isPhoto: Boolean = false,
    val durationMs: Long = 5000L,
    val trimInMs: Long = 0L,
    val trimOutMs: Long = 5000L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isReversed: Boolean = false,
    val isFrozen: Boolean = false,
    val colorAdjustment: ColorAdjustment = ColorAdjustment(),
    val activeEffect: EffectType = EffectType.NONE,
    val transition: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 500L,
    val keyframes: List<ClipKeyframe> = emptyList(),
    val speedPoints: List<SpeedPoint> = emptyList(),
    val previewColor: Long = 0xFF2563EB
) {
    val effectiveDurationMs: Long
        get() {
            val trimmed = (trimOutMs - trimInMs).coerceAtLeast(100L)
            return (trimmed / speed.coerceIn(0.1f, 10.0f)).toLong()
        }
}

data class TextLayer(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "Sample Text",
    val startMs: Long = 0L,
    val durationMs: Long = 4000L,
    val posX: Float = 0.5f,
    val posY: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val color: Long = 0xFFFFFFFF,
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 2f,
    val backgroundColor: Long = 0x00000000,
    val fontSize: Float = 28f,
    val fontFamily: String = "Bold",
    val opacity: Float = 1.0f,
    val letterSpacing: Float = 0f,
    val lineSpacing: Float = 1f,
    val animation: TextAnimType = TextAnimType.NONE,
    val keyframes: List<ClipKeyframe> = emptyList()
)

data class StickerLayer(
    val id: String = UUID.randomUUID().toString(),
    val emojiOrIcon: String = "🔥",
    val customUri: String? = null,
    val category: String = "Emoji",
    val startMs: Long = 0L,
    val durationMs: Long = 3000L,
    val posX: Float = 0.5f,
    val posY: Float = 0.4f,
    val scale: Float = 1.2f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f,
    val isAnimated: Boolean = true
)

data class AudioClip(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Background Track",
    val uri: String? = null,
    val startMs: Long = 0L,
    val durationMs: Long = 6000L,
    val trimInMs: Long = 0L,
    val trimOutMs: Long = 6000L,
    val volume: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val speed: Float = 1.0f,
    val isVoiceOver: Boolean = false,
    val isNoiseReduction: Boolean = false,
    val waveforms: List<Float> = emptyList(),
    val beatMarkersMs: List<Long> = emptyList()
) {
    val effectiveDurationMs: Long
        get() = ((trimOutMs - trimInMs).coerceAtLeast(100L) / speed.coerceIn(0.1f, 5.0f)).toLong()
}

data class CanvasBackground(
    val type: String = "COLOR", // COLOR, GRADIENT, BLUR
    val color: Long = 0xFF0A0910,
    val gradientColor2: Long = 0xFF1F1D2C,
    val blurIntensity: Float = 0.5f
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "My Project",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val aspectRatio: AspectRatioEnum = AspectRatioEnum.RATIO_9_16,
    val videoClips: List<VideoClip> = emptyList(),
    val textLayers: List<TextLayer> = emptyList(),
    val stickerLayers: List<StickerLayer> = emptyList(),
    val audioClips: List<AudioClip> = emptyList(),
    val canvasBackground: CanvasBackground = CanvasBackground(),
    val fps: Int = 30
) {
    val totalDurationMs: Long
        get() {
            var maxMs = 0L
            var currentVideoStart = 0L
            for (clip in videoClips) {
                currentVideoStart += clip.effectiveDurationMs
            }
            if (currentVideoStart > maxMs) maxMs = currentVideoStart

            for (txt in textLayers) {
                val end = txt.startMs + txt.durationMs
                if (end > maxMs) maxMs = end
            }
            for (stk in stickerLayers) {
                val end = stk.startMs + stk.durationMs
                if (end > maxMs) maxMs = end
            }
            for (aud in audioClips) {
                val end = aud.startMs + aud.effectiveDurationMs
                if (end > maxMs) maxMs = end
            }
            return maxMs.coerceAtLeast(3000L) // Minimum 3s baseline
        }
}

enum class EditorToolTab(val title: String) {
    NONE("Tools"),
    MEDIA("Media"),
    AUDIO("Audio"),
    TEXT("Text"),
    STICKERS("Stickers"),
    EFFECTS("Effects"),
    FILTERS("Filters"),
    TRANSITION("Transition"),
    SPEED("Speed"),
    ADJUST("Adjust"),
    CANVAS("Canvas"),
    KEYFRAME("Keyframe"),
    AI_TOOLS("AI Tools")
}

data class ExportConfig(
    val resolution: String = "1080p",
    val fps: Int = 30,
    val quality: String = "High",
    val bitrateMbps: Int = 16
)
