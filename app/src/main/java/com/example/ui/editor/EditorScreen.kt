package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.VideoCanvasPreview
import com.example.model.EditorToolTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(state.project.title) }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            renameText = state.project.title
                            showRenameDialog = true
                        }
                    ) {
                        Text(
                            text = state.project.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = state.canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (state.canUndo) Color.White else TextMuted
                        )
                    }

                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = state.canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (state.canRedo) Color.White else TextMuted
                        )
                    }

                    // Aspect ratio badge
                    Surface(
                        color = StudioSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { viewModel.setToolTab(EditorToolTab.CANVAS) }
                    ) {
                        Text(
                            text = state.project.aspectRatio.ratioWidth.toInt().toString() + ":" + state.project.aspectRatio.ratioHeight.toInt().toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Export Button
                    Button(
                        onClick = { showExportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("export_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Export",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioBlack,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioBlack)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Active Tool Panel Drawer
                ToolPanels(
                    activeTab = state.activeToolTab,
                    project = state.project,
                    selectedClipId = state.selectedClipId,
                    selectedTextId = state.selectedTextId,
                    selectedStickerId = state.selectedStickerId,
                    isRecordingVoiceOver = state.isRecordingVoiceOver,
                    isAiProcessing = state.isAiProcessing,
                    aiProgressText = state.aiProgressText,
                    onClose = { viewModel.setToolTab(EditorToolTab.NONE) },
                    onAddClip = { title, dur -> viewModel.addVideoClip(title, dur) },
                    onAddAudio = { title -> viewModel.addAudioTrack(title) },
                    onExtractAudio = { viewModel.extractAudioFromSelectedClip() },
                    onToggleVoiceOver = {
                        if (state.isRecordingVoiceOver) viewModel.stopVoiceOverRecording()
                        else viewModel.startVoiceOverRecording()
                    },
                    onAddText = { txt, anim, col -> viewModel.addTextLayer(txt, anim, col) },
                    onUpdateText = { layer -> viewModel.updateSelectedTextLayer(layer) },
                    onAddSticker = { emoji -> viewModel.addSticker(emoji) },
                    onSelectEffect = { eff -> viewModel.updateSelectedClipEffect(eff) },
                    onSelectTransition = { trans, dur -> viewModel.updateSelectedClipTransition(trans, dur) },
                    onUpdateAdjustment = { adj -> viewModel.updateSelectedClipAdjustment(adj) },
                    onUpdateSpeed = { spd -> viewModel.updateSelectedClipSpeed(spd) },
                    onSelectAspectRatio = { ratio -> viewModel.setAspectRatio(ratio) },
                    onSelectCanvasBackground = { bg -> viewModel.setCanvasBackground(bg) },
                    onAddKeyframe = { viewModel.addKeyframeToSelectedClip() },
                    onAiAutoCaptions = { viewModel.generateAutoCaptions() },
                    onAiEnhance = { viewModel.applyAiEnhanceToSelectedClip() }
                )

                // Bottom Tool Icon Ribbon
                EditorToolRibbon(
                    activeTab = state.activeToolTab,
                    onTabSelected = { viewModel.setToolTab(it) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(StudioBlack)
        ) {
            // Center Canvas Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(StudioDarkBg)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                VideoCanvasPreview(
                    project = state.project,
                    currentPositionMs = state.currentPositionMs,
                    selectedTextId = state.selectedTextId,
                    selectedStickerId = state.selectedStickerId,
                    onTextMoved = { id, nx, ny -> viewModel.setTextPosition(id, nx, ny) },
                    onStickerMoved = { id, nx, ny -> viewModel.setStickerPosition(id, nx, ny) },
                    modifier = Modifier.fillMaxSize()
                )

                // Timestamp overlay at bottom of preview
                val curSec = state.currentPositionMs / 1000f
                val totalSec = state.project.totalDurationMs / 1000f
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${String.format("%02d:%04.1f", (curSec / 60).toInt(), curSec % 60)} / ${String.format("%02d:%04.1f", (totalSec / 60).toInt(), totalSec % 60)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Timeline Quick Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Toggle
                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                        .testTag("play_pause_button")
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Split Action
                    TimelineActionButton(
                        icon = Icons.Default.ContentCut,
                        label = "Split",
                        onClick = { viewModel.splitClipAtPlayhead() },
                        testTag = "split_clip_button"
                    )

                    // Duplicate Action
                    TimelineActionButton(
                        icon = Icons.Default.ContentCopy,
                        label = "Duplicate",
                        onClick = { viewModel.duplicateSelectedClip() }
                    )

                    // Delete Action
                    TimelineActionButton(
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        onClick = {
                            if (state.selectedTextId != null) viewModel.deleteSelectedText()
                            else if (state.selectedStickerId != null) viewModel.deleteSelectedSticker()
                            else viewModel.deleteSelectedClip()
                        },
                        tint = NeonRed
                    )

                    // Keyframe Action
                    TimelineActionButton(
                        icon = Icons.Default.AddLocation,
                        label = "Keyframe",
                        onClick = { viewModel.addKeyframeToSelectedClip() },
                        tint = NeonPurple
                    )
                }

                // Zoom Timeline scale
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.setZoomScale(state.timelineZoomScale - 0.25f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${(state.timelineZoomScale * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    IconButton(
                        onClick = { viewModel.setZoomScale(state.timelineZoomScale + 0.25f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Multi-Track Timeline
            TimelineView(
                project = state.project,
                currentPositionMs = state.currentPositionMs,
                zoomScale = state.timelineZoomScale,
                selectedClipId = state.selectedClipId,
                selectedTextId = state.selectedTextId,
                selectedStickerId = state.selectedStickerId,
                selectedAudioId = state.selectedAudioId,
                onSeek = { viewModel.seekTo(it) },
                onClipClick = { viewModel.selectClip(it) },
                onTextClick = { viewModel.selectText(it) },
                onStickerClick = { viewModel.selectSticker(it) },
                onAudioClick = { viewModel.selectAudio(it) },
                onAddTransitionClick = {
                    viewModel.selectClip(it)
                    viewModel.setToolTab(EditorToolTab.TRANSITION)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }

    // Export Dialog
    if (showExportDialog || state.isExporting || state.exportedFile != null) {
        ExportDialog(
            durationMs = state.project.totalDurationMs,
            isExporting = state.isExporting,
            exportProgress = state.exportProgress,
            statusText = state.exportStatusText,
            exportedFile = state.exportedFile,
            exportError = state.exportError,
            onDismiss = {
                showExportDialog = false
                viewModel.dismissExportResult()
            },
            onStartExport = { config -> viewModel.startExport(config) },
            onCancelExport = { viewModel.cancelExport() }
        )
    }

    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Project", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = StudioBorder
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameProject(renameText)
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = StudioSurface
        )
    }
}

@Composable
private fun TimelineActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
    testTag: String? = null
) {
    Surface(
        onClick = onClick,
        color = StudioSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = tint,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EditorToolRibbon(
    activeTab: EditorToolTab,
    onTabSelected: (EditorToolTab) -> Unit
) {
    val tools = listOf(
        Pair(EditorToolTab.MEDIA, Icons.Default.VideoLibrary),
        Pair(EditorToolTab.AUDIO, Icons.Default.MusicNote),
        Pair(EditorToolTab.TEXT, Icons.Default.TextFields),
        Pair(EditorToolTab.STICKERS, Icons.Default.SentimentSatisfiedAlt),
        Pair(EditorToolTab.EFFECTS, Icons.Default.AutoAwesome),
        Pair(EditorToolTab.FILTERS, Icons.Default.FilterVintage),
        Pair(EditorToolTab.TRANSITION, Icons.Default.SwapHoriz),
        Pair(EditorToolTab.SPEED, Icons.Default.Speed),
        Pair(EditorToolTab.ADJUST, Icons.Default.Tune),
        Pair(EditorToolTab.CANVAS, Icons.Default.Crop),
        Pair(EditorToolTab.AI_TOOLS, Icons.Default.Psychology)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(StudioSurface)
            .border(0.5.dp, StudioBorder)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tools.forEach { (tab, icon) ->
            val isSelected = activeTab == tab
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.title,
                    tint = if (isSelected) NeonCyan else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tab.title,
                    fontSize = 10.sp,
                    color = if (isSelected) NeonCyan else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
