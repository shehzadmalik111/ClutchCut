package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ToolPanels(
    activeTab: EditorToolTab,
    project: Project,
    selectedClipId: String?,
    selectedTextId: String?,
    selectedStickerId: String?,
    isRecordingVoiceOver: Boolean,
    isAiProcessing: Boolean,
    aiProgressText: String,
    onClose: () -> Unit,
    onAddClip: (title: String, durationMs: Long) -> Unit,
    onAddAudio: (title: String) -> Unit,
    onExtractAudio: () -> Unit,
    onToggleVoiceOver: () -> Unit,
    onAddText: (String, TextAnimType, Long) -> Unit,
    onUpdateText: (TextLayer) -> Unit,
    onAddSticker: (String) -> Unit,
    onSelectEffect: (EffectType) -> Unit,
    onSelectTransition: (TransitionType, Long) -> Unit,
    onUpdateAdjustment: (ColorAdjustment) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onSelectAspectRatio: (AspectRatioEnum) -> Unit,
    onSelectCanvasBackground: (CanvasBackground) -> Unit,
    onAddKeyframe: () -> Unit,
    onAiAutoCaptions: () -> Unit,
    onAiEnhance: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeTab == EditorToolTab.NONE) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp),
        color = StudioSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header with Title and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeTab.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab-specific content
            when (activeTab) {
                EditorToolTab.MEDIA -> MediaPanel(onAddClip)
                EditorToolTab.AUDIO -> AudioPanel(
                    isRecordingVoiceOver = isRecordingVoiceOver,
                    onAddAudio = onAddAudio,
                    onExtractAudio = onExtractAudio,
                    onToggleVoiceOver = onToggleVoiceOver
                )
                EditorToolTab.TEXT -> TextPanel(
                    project = project,
                    selectedTextId = selectedTextId,
                    onAddText = onAddText,
                    onUpdateText = onUpdateText
                )
                EditorToolTab.STICKERS -> StickersPanel(onAddSticker)
                EditorToolTab.EFFECTS -> EffectsPanel(
                    selectedClip = project.videoClips.find { it.id == selectedClipId },
                    onSelectEffect = onSelectEffect
                )
                EditorToolTab.FILTERS, EditorToolTab.ADJUST -> AdjustPanel(
                    selectedClip = project.videoClips.find { it.id == selectedClipId },
                    onUpdateAdjustment = onUpdateAdjustment
                )
                EditorToolTab.TRANSITION -> TransitionPanel(
                    selectedClip = project.videoClips.find { it.id == selectedClipId },
                    onSelectTransition = onSelectTransition
                )
                EditorToolTab.SPEED -> SpeedPanel(
                    selectedClip = project.videoClips.find { it.id == selectedClipId },
                    onUpdateSpeed = onUpdateSpeed
                )
                EditorToolTab.CANVAS -> CanvasPanel(
                    currentRatio = project.aspectRatio,
                    onSelectAspectRatio = onSelectAspectRatio,
                    onSelectCanvasBackground = onSelectCanvasBackground
                )
                EditorToolTab.KEYFRAME -> KeyframePanel(
                    selectedClip = project.videoClips.find { it.id == selectedClipId },
                    onAddKeyframe = onAddKeyframe
                )
                EditorToolTab.AI_TOOLS -> AiToolsPanel(
                    isProcessing = isAiProcessing,
                    progressText = aiProgressText,
                    onAutoCaptions = onAiAutoCaptions,
                    onEnhance = onAiEnhance
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun MediaPanel(onAddClip: (String, Long) -> Unit) {
    Column {
        Text(
            text = "Select Media or Pro Stock Asset to Insert:",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        val presets = listOf(
            Pair("Drone Mountain Sunset", 5000L),
            Pair("City Cyberpunk Neon Walk", 4500L),
            Pair("Gaming Clutch Victory", 4000L),
            Pair("Beach Waves Chill 4K", 6000L),
            Pair("Vlog Talking Intro", 5000L)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { preset ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { onAddClip(preset.first, preset.second) },
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🎬 ${preset.first}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${preset.second / 1000}s HD",
                            fontSize = 10.sp,
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioPanel(
    isRecordingVoiceOver: Boolean,
    onAddAudio: (String) -> Unit,
    onExtractAudio: () -> Unit,
    onToggleVoiceOver: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice-over record button
            Button(
                onClick = onToggleVoiceOver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecordingVoiceOver) NeonRed else StudioSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = if (isRecordingVoiceOver) Color.White else NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRecordingVoiceOver) "Stop Rec" else "Voice-over",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }

            // Extract Audio button
            Button(
                onClick = onExtractAudio,
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = "Extract",
                    tint = NeonGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Extract", fontSize = 12.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Royalty-Free Audio Library:", fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))

        val musicList = listOf("Phonk Drift Beat", "Lo-Fi Midnight", "Cyberpunk Synth", "Acoustic Chill", "Cinematic Impact")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(musicList) { track ->
                AssistChip(
                    onClick = { onAddAudio(track) },
                    label = { Text("🎵 $track", fontSize = 11.sp, color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = StudioSurfaceHover)
                )
            }
        }
    }
}

@Composable
private fun TextPanel(
    project: Project,
    selectedTextId: String?,
    onAddText: (String, TextAnimType, Long) -> Unit,
    onUpdateText: (TextLayer) -> Unit
) {
    var newTextContent by remember { mutableStateOf("New Title") }
    var selectedAnim by remember { mutableStateOf(TextAnimType.POP_UP) }

    val activeLayer = project.textLayers.find { it.id == selectedTextId }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (activeLayer != null) {
            Text("Edit Selected Text Layer:", fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = activeLayer.text,
                onValueChange = { onUpdateText(activeLayer.copy(text = it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = StudioBorder
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Animation Style:", fontSize = 11.sp, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(TextAnimType.values()) { anim ->
                        FilterChip(
                            selected = activeLayer.animation == anim,
                            onClick = { onUpdateText(activeLayer.copy(animation = anim)) },
                            label = { Text(anim.displayName, fontSize = 10.sp) }
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTextContent,
                    onValueChange = { newTextContent = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = StudioBorder
                    )
                )
                Button(
                    onClick = { onAddText(newTextContent, selectedAnim, 0xFF00F0FF) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("+ Add", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                items(TextAnimType.values()) { anim ->
                    FilterChip(
                        selected = selectedAnim == anim,
                        onClick = { selectedAnim = anim },
                        label = { Text(anim.displayName, fontSize = 10.sp) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StickersPanel(onAddSticker: (String) -> Unit) {
    val stickers = listOf("🔥", "⚡", "🎯", "👑", "🚀", "💣", "💥", "🎮", "🏆", "✨", "💯", "🌊", "😎", "👾", "❤️", "💎")
    Column {
        Text("Tap to Add Sticker:", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(stickers) { s ->
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceVariant)
                        .clickable { onAddSticker(s) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = s, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun EffectsPanel(
    selectedClip: VideoClip?,
    onSelectEffect: (EffectType) -> Unit
) {
    Column {
        Text(
            text = "Active Clip Effect: ${selectedClip?.activeEffect?.displayName ?: "None"}",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EffectType.values()) { effect ->
                val isSelected = selectedClip?.activeEffect == effect
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) StudioSurfaceHover else StudioSurfaceVariant)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) NeonPink else StudioBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectEffect(effect) }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = effect.displayName,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonPink else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TransitionPanel(
    selectedClip: VideoClip?,
    onSelectTransition: (TransitionType, Long) -> Unit
) {
    var durationMs by remember(selectedClip) {
        mutableStateOf(selectedClip?.transitionDurationMs ?: 500L)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transition: ${selectedClip?.transition?.displayName ?: "None"}",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = "${durationMs}ms",
                fontSize = 11.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = durationMs.toFloat(),
            onValueChange = {
                durationMs = it.toLong()
                if (selectedClip != null && selectedClip.transition != TransitionType.NONE) {
                    onSelectTransition(selectedClip.transition, durationMs)
                }
            },
            valueRange = 200f..1500f,
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(TransitionType.values()) { trans ->
                val isSelected = selectedClip?.transition == trans
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectTransition(trans, durationMs) },
                    label = { Text(trans.displayName, fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
private fun SpeedPanel(
    selectedClip: VideoClip?,
    onUpdateSpeed: (Float) -> Unit
) {
    var speedVal by remember(selectedClip) {
        mutableStateOf(selectedClip?.speed ?: 1.0f)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Clip Playback Speed:", fontSize = 12.sp, color = TextSecondary)
            Text(
                text = String.format("%.1fx", speedVal),
                fontSize = 13.sp,
                color = NeonAmber,
                fontWeight = FontWeight.Bold
            )
        }

        Slider(
            value = speedVal,
            onValueChange = {
                speedVal = it
                onUpdateSpeed(speedVal)
            },
            valueRange = 0.2f..4.0f,
            colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0.2f, 0.5f, 1.0f, 2.0f, 4.0f).forEach { preset ->
                Button(
                    onClick = {
                        speedVal = preset
                        onUpdateSpeed(preset)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (speedVal == preset) NeonAmber else StudioSurfaceVariant
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "${preset}x",
                        fontSize = 11.sp,
                        color = if (speedVal == preset) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustPanel(
    selectedClip: VideoClip?,
    onUpdateAdjustment: (ColorAdjustment) -> Unit
) {
    val adj = selectedClip?.colorAdjustment ?: ColorAdjustment()

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        AdjustmentSlider("Brightness", adj.brightness, -0.5f..0.5f) {
            onUpdateAdjustment(adj.copy(brightness = it))
        }
        AdjustmentSlider("Contrast", adj.contrast, 0.5f..1.8f) {
            onUpdateAdjustment(adj.copy(contrast = it))
        }
        AdjustmentSlider("Saturation", adj.saturation, 0.0f..2.0f) {
            onUpdateAdjustment(adj.copy(saturation = it))
        }
        AdjustmentSlider("Temperature", adj.temperature, -0.5f..0.5f) {
            onUpdateAdjustment(adj.copy(temperature = it))
        }
        AdjustmentSlider("Vignette", adj.vignette, 0.0f..1.0f) {
            onUpdateAdjustment(adj.copy(vignette = it))
        }
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(80.dp))
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
        )
    }
}

@Composable
private fun CanvasPanel(
    currentRatio: AspectRatioEnum,
    onSelectAspectRatio: (AspectRatioEnum) -> Unit,
    onSelectCanvasBackground: (CanvasBackground) -> Unit
) {
    Column {
        Text("Aspect Ratio:", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(AspectRatioEnum.values()) { ratio ->
                FilterChip(
                    selected = currentRatio == ratio,
                    onClick = { onSelectAspectRatio(ratio) },
                    label = { Text(ratio.displayName, fontSize = 11.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Background Style:", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onSelectCanvasBackground(CanvasBackground("COLOR", 0xFF0D0C13)) },
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Studio Black", fontSize = 11.sp)
            }
            Button(
                onClick = { onSelectCanvasBackground(CanvasBackground("GRADIENT", 0xFF0F172A, 0xFF38BDF8)) },
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cyan Gradient", fontSize = 11.sp)
            }
            Button(
                onClick = { onSelectCanvasBackground(CanvasBackground("GRADIENT", 0xFF2E1065, 0xFFEC4899)) },
                colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Neon Glow", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun KeyframePanel(
    selectedClip: VideoClip?,
    onAddKeyframe: () -> Unit
) {
    Column {
        Text(
            text = "Clip Keyframes (${selectedClip?.keyframes?.size ?: 0} points added):",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAddKeyframe,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.AddLocation, contentDescription = "Keyframe", tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("+ Add Keyframe at Current Time", color = Color.White)
        }
    }
}

@Composable
private fun AiToolsPanel(
    isProcessing: Boolean,
    progressText: String,
    onAutoCaptions: () -> Unit,
    onEnhance: () -> Unit
) {
    Column {
        if (isProcessing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = progressText,
                    fontSize = 12.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAutoCaptions() },
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🤖 Auto Captions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sync speech-to-text kinetic subtitles", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEnhance() },
                    colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("✨ AI Neural Enhance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Auto color grade, denoise & sharpen", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
