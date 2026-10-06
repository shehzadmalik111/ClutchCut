package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TimelineView(
    project: Project,
    currentPositionMs: Long,
    zoomScale: Float,
    selectedClipId: String?,
    selectedTextId: String?,
    selectedStickerId: String?,
    selectedAudioId: String?,
    onSeek: (Long) -> Unit,
    onClipClick: (String) -> Unit,
    onTextClick: (String) -> Unit,
    onStickerClick: (String) -> Unit,
    onAudioClick: (String) -> Unit,
    onAddTransitionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pxPerSecond = (80f * zoomScale).coerceIn(40f, 250f)
    val totalSeconds = (project.totalDurationMs / 1000f).coerceAtLeast(3f)
    val timelineWidthDp = (totalSeconds * pxPerSecond).dp

    val scrollState = rememberScrollState()

    // Auto-scroll timeline to follow playhead smoothly
    LaunchedEffect(currentPositionMs) {
        val targetScrollPx = ((currentPositionMs / 1000f) * pxPerSecond * 2.5f).toInt() - 300
        if (targetScrollPx in 0..scrollState.maxValue && !scrollState.isScrollInProgress) {
            scrollState.scrollTo(targetScrollPx)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg)
            .border(1.dp, StudioBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(vertical = 6.dp)
    ) {
        // Timeline Scrub Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .pointerInput(project.totalDurationMs, zoomScale) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val clickX = change.position.x
                        val seconds = clickX / (pxPerSecond * density)
                        val ms = (seconds * 1000).toLong().coerceIn(0L, project.totalDurationMs)
                        onSeek(ms)
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .width(timelineWidthDp + 200.dp)
                    .padding(start = 16.dp, end = 60.dp)
            ) {
                // 1. Time Ruler
                TimeRuler(
                    totalSeconds = totalSeconds.toInt() + 2,
                    pxPerSecond = pxPerSecond
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Video Clips Track
                VideoTrackRow(
                    project = project,
                    pxPerSecond = pxPerSecond,
                    selectedClipId = selectedClipId,
                    onClipClick = onClipClick,
                    onAddTransitionClick = onAddTransitionClick
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Text Layers Track
                if (project.textLayers.isNotEmpty()) {
                    TextTrackRow(
                        project = project,
                        pxPerSecond = pxPerSecond,
                        selectedTextId = selectedTextId,
                        onTextClick = onTextClick
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 4. Sticker Layers Track
                if (project.stickerLayers.isNotEmpty()) {
                    StickerTrackRow(
                        project = project,
                        pxPerSecond = pxPerSecond,
                        selectedStickerId = selectedStickerId,
                        onStickerClick = onStickerClick
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 5. Audio Tracks
                if (project.audioClips.isNotEmpty()) {
                    AudioTrackRow(
                        project = project,
                        pxPerSecond = pxPerSecond,
                        selectedAudioId = selectedAudioId,
                        onAudioClick = onAudioClick
                    )
                }
            }

            // Playhead Vertical Line with glowing diamond pin
            val playheadOffsetDp = ((currentPositionMs / 1000f) * pxPerSecond).dp + 16.dp
            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp)
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(PlayheadColor)
            ) {
                // Pin head on top of playhead
                Box(
                    modifier = Modifier
                        .offset(x = (-6).dp, y = (-2).dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(PlayheadColor)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun TimeRuler(
    totalSeconds: Int,
    pxPerSecond: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (sec in 0..totalSeconds) {
            Box(
                modifier = Modifier.width(pxPerSecond.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                // Major tick line
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(10.dp)
                        .background(StudioBorder)
                )
                Text(
                    text = String.format("%02d:%02d", sec / 60, sec % 60),
                    fontSize = 9.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoTrackRow(
    project: Project,
    pxPerSecond: Float,
    selectedClipId: String?,
    onClipClick: (String) -> Unit,
    onAddTransitionClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        project.videoClips.forEachIndexed { index, clip ->
            val clipWidthDp = ((clip.effectiveDurationMs / 1000f) * pxPerSecond).coerceAtLeast(40f).dp
            val isSelected = clip.id == selectedClipId

            // Video Clip Bar
            Box(
                modifier = Modifier
                    .width(clipWidthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(clip.previewColor).copy(alpha = 0.85f))
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onClipClick(clip.id) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = clip.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        if (clip.speed != 1.0f) {
                            Text(
                                text = "${clip.speed}x",
                                fontSize = 9.sp,
                                color = NeonAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%.1fs", clip.effectiveDurationMs / 1000f),
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        if (clip.activeEffect != EffectType.NONE) {
                            Text(
                                text = "✨",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Transition node button between clips
            if (index < project.videoClips.size - 1) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (clip.transition != TransitionType.NONE) NeonCyan else StudioSurfaceHover)
                        .border(1.dp, StudioBorder, CircleShape)
                        .clickable { onAddTransitionClick(clip.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (clip.transition != TransitionType.NONE) Icons.Default.FlashOn else Icons.Default.Add,
                        contentDescription = "Transition",
                        tint = if (clip.transition != TransitionType.NONE) Color.Black else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TextTrackRow(
    project: Project,
    pxPerSecond: Float,
    selectedTextId: String?,
    onTextClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        project.textLayers.forEach { layer ->
            val offsetDp = ((layer.startMs / 1000f) * pxPerSecond).dp
            val widthDp = ((layer.durationMs / 1000f) * pxPerSecond).coerceAtLeast(30f).dp
            val isSelected = layer.id == selectedTextId

            Box(
                modifier = Modifier
                    .offset(x = offsetDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TrackTextColor.copy(alpha = 0.85f))
                    .border(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onTextClick(layer.id) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "T: ${layer.text}",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StickerTrackRow(
    project: Project,
    pxPerSecond: Float,
    selectedStickerId: String?,
    onStickerClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        project.stickerLayers.forEach { sticker ->
            val offsetDp = ((sticker.startMs / 1000f) * pxPerSecond).dp
            val widthDp = ((sticker.durationMs / 1000f) * pxPerSecond).coerceAtLeast(28f).dp
            val isSelected = sticker.id == selectedStickerId

            Box(
                modifier = Modifier
                    .offset(x = offsetDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TrackStickerColor.copy(alpha = 0.85f))
                    .border(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onStickerClick(sticker.id) }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sticker.emojiOrIcon,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AudioTrackRow(
    project: Project,
    pxPerSecond: Float,
    selectedAudioId: String?,
    onAudioClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        project.audioClips.forEach { audio ->
            val offsetDp = ((audio.startMs / 1000f) * pxPerSecond).dp
            val widthDp = ((audio.effectiveDurationMs / 1000f) * pxPerSecond).coerceAtLeast(36f).dp
            val isSelected = audio.id == selectedAudioId

            Box(
                modifier = Modifier
                    .offset(x = offsetDp)
                    .width(widthDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(TrackAudioColor.copy(alpha = 0.85f))
                    .border(
                        width = if (isSelected) 2.dp else 0.5.dp,
                        color = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onAudioClick(audio.id) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (audio.isVoiceOver) Icons.Default.Mic else Icons.Default.MusicNote,
                        contentDescription = "Audio",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = audio.title,
                        fontSize = 10.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
