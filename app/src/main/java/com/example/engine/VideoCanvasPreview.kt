package com.example.engine

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.*
import kotlin.math.*
import kotlin.random.Random

@Composable
fun VideoCanvasPreview(
    project: Project,
    currentPositionMs: Long,
    selectedTextId: String?,
    selectedStickerId: String?,
    onTextMoved: ((id: String, newX: Float, newY: Float) -> Unit)? = null,
    onStickerMoved: ((id: String, newX: Float, newY: Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth.value
        val containerHeight = maxHeight.value

        // Calculate aspect ratio fit dimensions
        val targetRatio = project.aspectRatio.ratio
        val containerRatio = containerWidth / containerHeight

        val canvasW: Float
        val canvasH: Float
        if (targetRatio > containerRatio) {
            canvasW = containerWidth
            canvasH = containerWidth / targetRatio
        } else {
            canvasH = containerHeight
            canvasW = containerHeight * targetRatio
        }

        Box(
            modifier = Modifier
                .width(canvasW.dp)
                .height(canvasH.dp)
                .pointerInput(selectedTextId, selectedStickerId) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaX = dragAmount.x / size.width
                        val deltaY = dragAmount.y / size.height

                        if (selectedTextId != null && onTextMoved != null) {
                            val layer = project.textLayers.find { it.id == selectedTextId }
                            if (layer != null) {
                                val nx = (layer.posX + deltaX).coerceIn(0.05f, 0.95f)
                                val ny = (layer.posY + deltaY).coerceIn(0.05f, 0.95f)
                                onTextMoved(selectedTextId, nx, ny)
                            }
                        } else if (selectedStickerId != null && onStickerMoved != null) {
                            val layer = project.stickerLayers.find { it.id == selectedStickerId }
                            if (layer != null) {
                                val nx = (layer.posX + deltaX).coerceIn(0.05f, 0.95f)
                                val ny = (layer.posY + deltaY).coerceIn(0.05f, 0.95f)
                                onStickerMoved(selectedStickerId, nx, ny)
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Draw Canvas Background
                drawBackground(project.canvasBackground, w, h)

                // 2. Identify active video clip and transitions
                drawVideoTracks(project, currentPositionMs, w, h)

                // 3. Draw Text Layers
                drawTextLayers(project.textLayers, currentPositionMs, selectedTextId, w, h)

                // 4. Draw Sticker Layers
                drawStickerLayers(project.stickerLayers, currentPositionMs, selectedStickerId, w, h)
            }
        }
    }
}

private fun DrawScope.drawBackground(bg: CanvasBackground, w: Float, h: Float) {
    when (bg.type) {
        "GRADIENT" -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(bg.color), Color(bg.gradientColor2))
                ),
                size = Size(w, h)
            )
        }
        else -> {
            drawRect(
                color = Color(bg.color),
                size = Size(w, h)
            )
        }
    }
}

private fun DrawScope.drawVideoTracks(
    project: Project,
    timeMs: Long,
    w: Float,
    h: Float
) {
    if (project.videoClips.isEmpty()) {
        // Empty project placeholder screen
        drawEmptyPlaceholder(w, h)
        return
    }

    // Determine which clip is active at timeMs
    var accumulatedTime = 0L
    var activeClipIndex = -1
    var clipLocalTimeMs = 0L

    for (i in project.videoClips.indices) {
        val clip = project.videoClips[i]
        val clipDur = clip.effectiveDurationMs
        if (timeMs in accumulatedTime until (accumulatedTime + clipDur)) {
            activeClipIndex = i
            clipLocalTimeMs = timeMs - accumulatedTime
            break
        }
        accumulatedTime += clipDur
    }

    // If past all clips, show last clip or end
    if (activeClipIndex == -1) {
        val lastClip = project.videoClips.last()
        drawClipFrame(lastClip, lastClip.effectiveDurationMs, w, h, 1f, Offset.Zero, 1f, 0f)
        return
    }

    val activeClip = project.videoClips[activeClipIndex]
    val transDuration = activeClip.transitionDurationMs

    // Check if in transition to next clip
    if (activeClip.transition != TransitionType.NONE &&
        activeClipIndex < project.videoClips.size - 1 &&
        (activeClip.effectiveDurationMs - clipLocalTimeMs) <= transDuration
    ) {
        val nextClip = project.videoClips[activeClipIndex + 1]
        val transitionProgress = 1f - (activeClip.effectiveDurationMs - clipLocalTimeMs).toFloat() / transDuration.toFloat()
        val transform = EffectRenderer.calculateTransition(activeClip.transition, transitionProgress)

        // Draw Clip A
        if (transform.alphaA > 0f) {
            drawClipFrame(
                activeClip,
                clipLocalTimeMs,
                w, h,
                alpha = transform.alphaA,
                offset = Offset(transform.offsetXA * w, transform.offsetYA * h),
                scale = transform.scaleA,
                rotation = transform.rotationA
            )
        }

        // Draw Clip B
        if (transform.alphaB > 0f) {
            drawClipFrame(
                nextClip,
                0L,
                w, h,
                alpha = transform.alphaB,
                offset = Offset(transform.offsetXB * w, transform.offsetYB * h),
                scale = transform.scaleB,
                rotation = transform.rotationB
            )
        }

        // Transition flash overlay if any
        if (transform.flashAlpha > 0f) {
            drawRect(
                color = Color.White.copy(alpha = transform.flashAlpha.coerceIn(0f, 1f)),
                size = Size(w, h)
            )
        }
    } else {
        // Normal single clip frame
        drawClipFrame(activeClip, clipLocalTimeMs, w, h, 1f, Offset.Zero, 1f, 0f)
    }
}

private fun DrawScope.drawClipFrame(
    clip: VideoClip,
    localTimeMs: Long,
    w: Float,
    h: Float,
    alpha: Float,
    offset: Offset,
    scale: Float,
    rotation: Float
) {
    // Interpolate keyframes if present
    val kf = KeyframeInterpolator.interpolate(clip.keyframes, localTimeMs)
    val totalScale = scale * kf.scale
    val totalRot = rotation + kf.rotation
    val totalAlpha = (alpha * kf.opacity).coerceIn(0f, 1f)

    // Shake effect calculations
    var jitterX = kf.posX * w + offset.x
    var jitterY = kf.posY * h + offset.y

    if (clip.activeEffect == EffectType.SHAKE) {
        val shakeAmount = 14f
        val seed = (localTimeMs / 40L).toInt()
        val rnd = Random(seed)
        jitterX += (rnd.nextFloat() - 0.5f) * shakeAmount
        jitterY += (rnd.nextFloat() - 0.5f) * shakeAmount
    }

    // Base clip canvas background with subtle gradient to simulate realistic high-fidelity footage
    val baseColor = Color(clip.previewColor)
    val darkerColor = Color(
        red = (baseColor.red * 0.45f).coerceIn(0f, 1f),
        green = (baseColor.green * 0.45f).coerceIn(0f, 1f),
        blue = (baseColor.blue * 0.45f).coerceIn(0f, 1f),
        alpha = totalAlpha
    )

    // Draw main frame rectangle
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(baseColor.copy(alpha = totalAlpha), darkerColor),
            center = Offset(w / 2f + jitterX, h / 2f + jitterY),
            radius = max(w, h) * 0.8f * totalScale
        ),
        topLeft = Offset(jitterX, jitterY),
        size = Size(w, h)
    )

    // Atmospheric visual textures (Cinema grid, film reel lines)
    val linePaintColor = Color.White.copy(alpha = 0.08f * totalAlpha)
    for (step in 1..4) {
        val yPos = h * (step / 5f)
        drawLine(
            color = linePaintColor,
            start = Offset(0f, yPos),
            end = Offset(w, yPos),
            strokeWidth = 1.5f
        )
    }

    // Clip Info Badge inside frame preview
    drawIntoCanvas { canvas ->
        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            this.alpha = (210 * totalAlpha).toInt()
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawText("🎬 ${clip.title}", 30f + jitterX, 60f + jitterY, textPaint)

        // Show active effect badge on top-right of frame
        if (clip.activeEffect != EffectType.NONE) {
            val effPaint = Paint().apply {
                color = android.graphics.Color.CYAN
                this.alpha = (220 * totalAlpha).toInt()
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.nativeCanvas.drawText("✨ [${clip.activeEffect.displayName}]", w - 240f, 60f + jitterY, effPaint)
        }
    }

    // Glitch / RGB Split visual artifact lines
    if (clip.activeEffect == EffectType.GLITCH) {
        val glitchPhase = (localTimeMs / 80L).toInt()
        val rnd = Random(glitchPhase)
        for (i in 0 until 5) {
            val bandY = rnd.nextFloat() * h
            val bandH = rnd.nextFloat() * 24f + 6f
            val shift = (rnd.nextFloat() - 0.5f) * 40f
            drawRect(
                color = Color.Cyan.copy(alpha = 0.35f * totalAlpha),
                topLeft = Offset(shift, bandY),
                size = Size(w, bandH)
            )
            drawRect(
                color = Color.Magenta.copy(alpha = 0.25f * totalAlpha),
                topLeft = Offset(-shift, bandY + 4f),
                size = Size(w, bandH * 0.8f)
            )
        }
    }

    // VHS Retro Scanlines
    if (clip.activeEffect == EffectType.VHS_RETRO) {
        for (sy in 0 until h.toInt() step 6) {
            drawLine(
                color = Color.Black.copy(alpha = 0.15f * totalAlpha),
                start = Offset(0f, sy.toFloat()),
                end = Offset(w, sy.toFloat()),
                strokeWidth = 2f
            )
        }
        drawIntoCanvas { canvas ->
            val vhsPaint = Paint().apply {
                color = android.graphics.Color.YELLOW
                this.alpha = (200 * totalAlpha).toInt()
                textSize = 24f
                typeface = Typeface.MONOSPACE
            }
            val seconds = localTimeMs / 1000
            val frames = (localTimeMs % 1000) / 33
            canvas.nativeCanvas.drawText("SP PLAY 00:00:${String.format("%02d", seconds)}:${String.format("%02d", frames)}", 30f, h - 30f, vhsPaint)
        }
    }

    // Film Grain simulation
    if (clip.activeEffect == EffectType.FILM_GRAIN) {
        val rnd = Random(localTimeMs / 30L)
        for (i in 0 until 35) {
            val gx = rnd.nextFloat() * w
            val gy = rnd.nextFloat() * h
            drawCircle(
                color = Color.White.copy(alpha = rnd.nextFloat() * 0.25f * totalAlpha),
                radius = rnd.nextFloat() * 2.5f,
                center = Offset(gx, gy)
            )
        }
    }

    // Vignette Darkening
    val vigAmount = clip.colorAdjustment.vignette.coerceIn(0f, 1f)
    if (vigAmount > 0.05f || clip.activeEffect == EffectType.VIGNETTE_DARK) {
        val effVig = max(vigAmount, if (clip.activeEffect == EffectType.VIGNETTE_DARK) 0.65f else 0f)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = effVig * totalAlpha)),
                center = Offset(w / 2f, h / 2f),
                radius = max(w, h) * 0.7f
            ),
            size = Size(w, h)
        )
    }
}

private fun DrawScope.drawTextLayers(
    textLayers: List<TextLayer>,
    timeMs: Long,
    selectedId: String?,
    w: Float,
    h: Float
) {
    for (layer in textLayers) {
        val start = layer.startMs
        val end = layer.startMs + layer.durationMs
        if (timeMs in start..end) {
            val elapsed = timeMs - start
            val progress = (elapsed.toFloat() / layer.durationMs.toFloat()).coerceIn(0f, 1f)

            // Text animation calculations
            var displayContent = layer.text
            var animScale = layer.scale
            var animAlpha = layer.opacity

            when (layer.animation) {
                TextAnimType.TYPEWRITER -> {
                    val charCount = (progress * 3f * layer.text.length).toInt().coerceIn(0, layer.text.length)
                    displayContent = layer.text.take(charCount)
                }
                TextAnimType.POP_UP -> {
                    if (progress < 0.2f) {
                        val p = progress / 0.2f
                        animScale *= sin(p * PI / 2f).toFloat() * 1.25f
                    }
                }
                TextAnimType.FADE_IN -> {
                    if (progress < 0.25f) {
                        animAlpha *= (progress / 0.25f)
                    }
                }
                TextAnimType.SHAKE_BOUNCE -> {
                    val shake = sin(elapsed / 80.0).toFloat() * 12f
                    animScale += sin(elapsed / 120.0).toFloat() * 0.08f
                }
                else -> {}
            }

            val centerX = layer.posX * w
            val centerY = layer.posY * h

            drawIntoCanvas { canvas ->
                val tf = when (layer.fontFamily) {
                    "Serif" -> Typeface.SERIF
                    "Monospace" -> Typeface.MONOSPACE
                    "Bold" -> Typeface.DEFAULT_BOLD
                    else -> Typeface.DEFAULT
                }

                // Background box if set
                if (layer.backgroundColor != 0L) {
                    val bgPaint = Paint().apply {
                        color = layer.backgroundColor.toInt()
                        alpha = (255 * animAlpha).toInt()
                    }
                    val pad = 16f * animScale
                    canvas.nativeCanvas.drawRoundRect(
                        centerX - 120f * animScale,
                        centerY - 35f * animScale,
                        centerX + 120f * animScale,
                        centerY + 35f * animScale,
                        12f, 12f, bgPaint
                    )
                }

                // Stroke
                if (layer.strokeWidth > 0f) {
                    val strokePaint = Paint().apply {
                        color = layer.strokeColor.toInt()
                        alpha = (255 * animAlpha).toInt()
                        textSize = layer.fontSize * animScale
                        typeface = tf
                        style = Paint.Style.STROKE
                        this.strokeWidth = layer.strokeWidth * animScale
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawText(displayContent, centerX, centerY + (layer.fontSize * animScale * 0.35f), strokePaint)
                }

                // Main Fill
                val fillPaint = Paint().apply {
                    color = layer.color.toInt()
                    alpha = (255 * animAlpha).toInt()
                    textSize = layer.fontSize * animScale
                    typeface = tf
                    style = Paint.Style.FILL
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(displayContent, centerX, centerY + (layer.fontSize * animScale * 0.35f), fillPaint)

                // Selection Box indicator
                if (layer.id == selectedId) {
                    val boxPaint = Paint().apply {
                        color = android.graphics.Color.CYAN
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                    }
                    val halfW = (layer.fontSize * displayContent.length * 0.32f * animScale).coerceAtLeast(60f)
                    val halfH = (layer.fontSize * 0.7f * animScale).coerceAtLeast(30f)
                    canvas.nativeCanvas.drawRect(
                        centerX - halfW,
                        centerY - halfH,
                        centerX + halfW,
                        centerY + halfH,
                        boxPaint
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawStickerLayers(
    stickerLayers: List<StickerLayer>,
    timeMs: Long,
    selectedId: String?,
    w: Float,
    h: Float
) {
    for (sticker in stickerLayers) {
        val start = sticker.startMs
        val end = sticker.startMs + sticker.durationMs
        if (timeMs in start..end) {
            val elapsed = timeMs - start
            var scale = sticker.scale
            if (sticker.isAnimated) {
                // Gentle pulse animation
                scale *= (1f + sin(elapsed / 180.0).toFloat() * 0.08f)
            }

            val cx = sticker.posX * w
            val cy = sticker.posY * h

            drawIntoCanvas { canvas ->
                val emojiPaint = Paint().apply {
                    textSize = 58f * scale
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(sticker.emojiOrIcon, cx, cy + (20f * scale), emojiPaint)

                if (sticker.id == selectedId) {
                    val selectPaint = Paint().apply {
                        color = android.graphics.Color.MAGENTA
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                    }
                    canvas.nativeCanvas.drawCircle(cx, cy, 45f * scale, selectPaint)
                }
            }
        }
    }
}

private fun DrawScope.drawEmptyPlaceholder(w: Float, h: Float) {
    drawRect(
        color = Color(0xFF14131F),
        size = Size(w, h)
    )
    drawIntoCanvas { canvas ->
        val p = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.nativeCanvas.drawText("🎬 Tap '+ Add Media' to Start", w / 2f, h / 2f, p)
    }
}
