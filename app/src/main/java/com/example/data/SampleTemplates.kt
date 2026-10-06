package com.example.data

import com.example.model.*
import java.util.UUID

object SampleTemplates {

    fun createGamingHighlightProject(): Project {
        return Project(
            id = UUID.randomUUID().toString(),
            title = "Cyberpunk Gaming Clutches",
            aspectRatio = AspectRatioEnum.RATIO_9_16,
            canvasBackground = CanvasBackground("COLOR", 0xFF0D0C13, 0xFF1E1B4B),
            videoClips = listOf(
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    title = "Intro Spawn",
                    durationMs = 3500L,
                    trimInMs = 0L,
                    trimOutMs = 3500L,
                    speed = 1.0f,
                    previewColor = 0xFF7C3AED,
                    activeEffect = EffectType.GLITCH,
                    transition = TransitionType.FLASH_WHITE,
                    transitionDurationMs = 400L,
                    colorAdjustment = ColorAdjustment(contrast = 1.25f, saturation = 1.4f, vignette = 0.4f)
                ),
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    title = "Headshot Triple Kill",
                    durationMs = 4000L,
                    trimInMs = 0L,
                    trimOutMs = 4000L,
                    speed = 0.5f, // Slow-mo ramp
                    previewColor = 0xFFDC2626,
                    activeEffect = EffectType.RGB_SPLIT,
                    transition = TransitionType.ZOOM_IN,
                    transitionDurationMs = 500L,
                    colorAdjustment = ColorAdjustment(exposure = 0.1f, saturation = 1.5f, sharpness = 0.5f)
                ),
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    title = "Victory Royale Finale",
                    durationMs = 3000L,
                    trimInMs = 0L,
                    trimOutMs = 3000L,
                    speed = 1.2f,
                    previewColor = 0xFF059669,
                    activeEffect = EffectType.GLOW,
                    transition = TransitionType.NONE,
                    colorAdjustment = ColorAdjustment(brightness = 0.05f, contrast = 1.2f)
                )
            ),
            textLayers = listOf(
                TextLayer(
                    id = UUID.randomUUID().toString(),
                    text = "INSANE 1v4 CLUTCH 🔥",
                    startMs = 500L,
                    durationMs = 3000L,
                    posY = 0.25f,
                    fontSize = 32f,
                    color = 0xFF00F0FF,
                    strokeColor = 0xFF000000,
                    strokeWidth = 3f,
                    animation = TextAnimType.POP_UP
                ),
                TextLayer(
                    id = UUID.randomUUID().toString(),
                    text = "SLOW-MO DROP",
                    startMs = 3800L,
                    durationMs = 2500L,
                    posY = 0.8f,
                    fontSize = 26f,
                    color = 0xFFFF0055,
                    strokeColor = 0xFF000000,
                    strokeWidth = 2f,
                    animation = TextAnimType.SHAKE_BOUNCE
                )
            ),
            stickerLayers = listOf(
                StickerLayer(
                    id = UUID.randomUUID().toString(),
                    emojiOrIcon = "🎯",
                    startMs = 3800L,
                    durationMs = 2500L,
                    posX = 0.5f,
                    posY = 0.45f,
                    scale = 1.8f
                ),
                StickerLayer(
                    id = UUID.randomUUID().toString(),
                    emojiOrIcon = "👑",
                    startMs = 8000L,
                    durationMs = 2500L,
                    posX = 0.5f,
                    posY = 0.2f,
                    scale = 1.5f
                )
            ),
            audioClips = listOf(
                AudioClip(
                    id = UUID.randomUUID().toString(),
                    title = "Phonk Bass Boost (140 BPM)",
                    startMs = 0L,
                    durationMs = 11000L,
                    trimInMs = 0L,
                    trimOutMs = 11000L,
                    volume = 0.9f,
                    fadeInMs = 200L,
                    fadeOutMs = 500L,
                    waveforms = List(24) { (it % 7 + 3) / 10f }
                )
            )
        )
    }

    fun createCinematicVlogProject(): Project {
        return Project(
            id = UUID.randomUUID().toString(),
            title = "Tokyo Midnight Reel",
            aspectRatio = AspectRatioEnum.RATIO_9_16,
            canvasBackground = CanvasBackground("COLOR", 0xFF090A0F, 0xFF1E293B),
            videoClips = listOf(
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    title = "Neon Crossing Shibuya",
                    durationMs = 4000L,
                    trimInMs = 0L,
                    trimOutMs = 4000L,
                    speed = 1.0f,
                    previewColor = 0xFF0284C7,
                    activeEffect = EffectType.FILM_GRAIN,
                    transition = TransitionType.DISSOLVE,
                    transitionDurationMs = 600L,
                    colorAdjustment = ColorAdjustment(temperature = -0.2f, contrast = 1.15f, vignette = 0.5f)
                ),
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    title = "Ramen Alley Steam",
                    durationMs = 3500L,
                    trimInMs = 0L,
                    trimOutMs = 3500L,
                    speed = 0.8f,
                    previewColor = 0xFFD97706,
                    activeEffect = EffectType.VHS_RETRO,
                    transition = TransitionType.SLIDE_LEFT,
                    transitionDurationMs = 500L,
                    colorAdjustment = ColorAdjustment(temperature = 0.3f, saturation = 1.2f)
                ),
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    title = "Rooftop Sky Tree Dawn",
                    durationMs = 3500L,
                    trimInMs = 0L,
                    trimOutMs = 3500L,
                    speed = 1.0f,
                    previewColor = 0xFF9333EA,
                    activeEffect = EffectType.CHROMATIC_ABERRATION,
                    transition = TransitionType.FADE,
                    colorAdjustment = ColorAdjustment(brightness = 0.05f, contrast = 1.1f)
                )
            ),
            textLayers = listOf(
                TextLayer(
                    id = UUID.randomUUID().toString(),
                    text = "TOKYO AFTER HOURS",
                    startMs = 500L,
                    durationMs = 3000L,
                    posY = 0.75f,
                    fontSize = 28f,
                    fontFamily = "Monospace",
                    color = 0xFFFFFFFF,
                    animation = TextAnimType.TYPEWRITER
                )
            ),
            stickerLayers = listOf(
                StickerLayer(
                    id = UUID.randomUUID().toString(),
                    emojiOrIcon = "✨",
                    startMs = 1000L,
                    durationMs = 2500L,
                    posX = 0.8f,
                    posY = 0.25f,
                    scale = 1.2f
                )
            ),
            audioClips = listOf(
                AudioClip(
                    id = UUID.randomUUID().toString(),
                    title = "Chill Lofi Beats Rain",
                    startMs = 0L,
                    durationMs = 11000L,
                    volume = 0.75f,
                    waveforms = List(20) { (it % 5 + 4) / 10f }
                )
            )
        )
    }

    fun createTravelMontageProject(): Project {
        return Project(
            id = UUID.randomUUID().toString(),
            title = "Island Adventure 4K",
            aspectRatio = AspectRatioEnum.RATIO_16_9,
            canvasBackground = CanvasBackground("COLOR", 0xFF05111A, 0xFF0284C7),
            videoClips = listOf(
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    title = "Drone Beach Reveal",
                    durationMs = 4500L,
                    speed = 1.0f,
                    previewColor = 0xFF0D9488,
                    activeEffect = EffectType.NONE,
                    transition = TransitionType.ZOOM_OUT,
                    transitionDurationMs = 600L,
                    colorAdjustment = ColorAdjustment(saturation = 1.35f, brightness = 0.05f, sharpness = 0.4f)
                ),
                VideoClip(
                    id = UUID.randomUUID().toString(),
                    title = "Cliff Diving Sunset",
                    durationMs = 4000L,
                    speed = 0.6f,
                    previewColor = 0xFFEA580C,
                    activeEffect = EffectType.MOTION_BLUR,
                    transition = TransitionType.CUBE_3D,
                    transitionDurationMs = 500L,
                    colorAdjustment = ColorAdjustment(temperature = 0.25f, contrast = 1.2f)
                )
            ),
            textLayers = listOf(
                TextLayer(
                    id = UUID.randomUUID().toString(),
                    text = "SUMMER NEVER ENDS 🌊",
                    startMs = 400L,
                    durationMs = 3500L,
                    posY = 0.2f,
                    fontSize = 32f,
                    color = 0xFFFDE047,
                    strokeColor = 0xFF000000,
                    strokeWidth = 2f,
                    animation = TextAnimType.FADE_IN
                )
            ),
            audioClips = listOf(
                AudioClip(
                    id = UUID.randomUUID().toString(),
                    title = "Tropical House Melodic",
                    startMs = 0L,
                    durationMs = 9000L,
                    volume = 0.85f,
                    waveforms = List(18) { (it % 6 + 3) / 10f }
                )
            )
        )
    }
}
