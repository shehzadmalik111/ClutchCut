package com.example.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.data.SampleTemplates
import com.example.engine.AudioEngine
import com.example.engine.VideoExportEngine
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

data class EditorUiState(
    val project: Project = SampleTemplates.createGamingHighlightProject(),
    val currentPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val selectedClipId: String? = null,
    val selectedTextId: String? = null,
    val selectedStickerId: String? = null,
    val selectedAudioId: String? = null,
    val activeToolTab: EditorToolTab = EditorToolTab.NONE,
    val timelineZoomScale: Float = 1.0f,
    val isRecordingVoiceOver: Boolean = false,
    val isAiProcessing: Boolean = false,
    val aiProgressText: String = "",
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportStatusText: String = "",
    val exportedFile: File? = null,
    val exportError: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)
    private val exportEngine = VideoExportEngine(application)
    private val audioEngine = AudioEngine(application)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    private var playbackJob: Job? = null

    init {
        // Select first clip by default if available
        val initProject = _uiState.value.project
        _uiState.value = _uiState.value.copy(
            selectedClipId = initProject.videoClips.firstOrNull()?.id
        )
    }

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            val proj = repository.getProject(projectId)
            if (proj != null) {
                undoStack.clear()
                redoStack.clear()
                _uiState.value = _uiState.value.copy(
                    project = proj,
                    currentPositionMs = 0L,
                    isPlaying = false,
                    selectedClipId = proj.videoClips.firstOrNull()?.id,
                    selectedTextId = null,
                    selectedStickerId = null,
                    canUndo = false,
                    canRedo = false
                )
            }
        }
    }

    private fun pushHistoryState() {
        undoStack.add(_uiState.value.project)
        if (undoStack.size > 25) undoStack.removeAt(0)
        redoStack.clear()
        _uiState.value = _uiState.value.copy(
            canUndo = undoStack.isNotEmpty(),
            canRedo = false
        )
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(_uiState.value.project)
            _uiState.value = _uiState.value.copy(
                project = prev,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
            autoSave(prev)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(_uiState.value.project)
            _uiState.value = _uiState.value.copy(
                project = next,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
            autoSave(next)
        }
    }

    private fun autoSave(project: Project) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveProject(project)
        }
    }

    fun setToolTab(tab: EditorToolTab) {
        _uiState.value = _uiState.value.copy(
            activeToolTab = if (_uiState.value.activeToolTab == tab) EditorToolTab.NONE else tab
        )
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlaying = true)
        val totalMs = _uiState.value.project.totalDurationMs

        playbackJob = viewModelScope.launch {
            val stepMs = 33L // ~30 fps update
            while (isActive && _uiState.value.isPlaying) {
                delay(stepMs)
                var nextPos = _uiState.value.currentPositionMs + stepMs
                if (nextPos >= totalMs) {
                    nextPos = 0L
                    _uiState.value = _uiState.value.copy(currentPositionMs = 0L, isPlaying = false)
                    break
                }
                _uiState.value = _uiState.value.copy(currentPositionMs = nextPos)
            }
        }
    }

    fun pause() {
        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }

    fun seekTo(ms: Long) {
        val total = _uiState.value.project.totalDurationMs
        val clamped = ms.coerceIn(0L, total)
        _uiState.value = _uiState.value.copy(currentPositionMs = clamped)
    }

    fun setZoomScale(scale: Float) {
        _uiState.value = _uiState.value.copy(timelineZoomScale = scale.coerceIn(0.5f, 3.0f))
    }

    fun selectClip(id: String?) {
        _uiState.value = _uiState.value.copy(
            selectedClipId = id,
            selectedTextId = null,
            selectedStickerId = null
        )
    }

    fun selectText(id: String?) {
        _uiState.value = _uiState.value.copy(
            selectedTextId = id,
            selectedClipId = null,
            selectedStickerId = null
        )
    }

    fun selectSticker(id: String?) {
        _uiState.value = _uiState.value.copy(
            selectedStickerId = id,
            selectedClipId = null,
            selectedTextId = null
        )
    }

    fun selectAudio(id: String?) {
        _uiState.value = _uiState.value.copy(selectedAudioId = id)
    }

    // --- Video Clip Operations ---

    fun addVideoClip(title: String, durationMs: Long, isPhoto: Boolean = false, uri: String? = null) {
        pushHistoryState()
        val colors = listOf(0xFF2563EBL, 0xFF7C3AEDL, 0xFF059669L, 0xFFDC2626L, 0xFFD97706L)
        val newClip = VideoClip(
            id = UUID.randomUUID().toString(),
            title = title,
            durationMs = durationMs,
            trimInMs = 0L,
            trimOutMs = durationMs,
            isPhoto = isPhoto,
            uri = uri,
            previewColor = colors[(_uiState.value.project.videoClips.size) % colors.size]
        )
        val updated = _uiState.value.project.copy(
            videoClips = _uiState.value.project.videoClips + newClip
        )
        _uiState.value = _uiState.value.copy(
            project = updated,
            selectedClipId = newClip.id
        )
        autoSave(updated)
    }

    fun splitClipAtPlayhead() {
        val currentPlayhead = _uiState.value.currentPositionMs
        val clips = _uiState.value.project.videoClips
        var accumulated = 0L
        var clipToSplitIndex = -1

        for (i in clips.indices) {
            val clip = clips[i]
            val dur = clip.effectiveDurationMs
            if (currentPlayhead in accumulated until (accumulated + dur)) {
                clipToSplitIndex = i
                break
            }
            accumulated += dur
        }

        if (clipToSplitIndex == -1) return
        val targetClip = clips[clipToSplitIndex]
        val localOffset = currentPlayhead - accumulated
        if (localOffset <= 200L || localOffset >= targetClip.effectiveDurationMs - 200L) return

        pushHistoryState()
        val splitSourceTime = targetClip.trimInMs + (localOffset * targetClip.speed).toLong()

        val clipA = targetClip.copy(
            id = UUID.randomUUID().toString(),
            title = "${targetClip.title} Part 1",
            trimOutMs = splitSourceTime
        )
        val clipB = targetClip.copy(
            id = UUID.randomUUID().toString(),
            title = "${targetClip.title} Part 2",
            trimInMs = splitSourceTime
        )

        val newClips = clips.toMutableList()
        newClips.removeAt(clipToSplitIndex)
        newClips.add(clipToSplitIndex, clipA)
        newClips.add(clipToSplitIndex + 1, clipB)

        val updated = _uiState.value.project.copy(videoClips = newClips)
        _uiState.value = _uiState.value.copy(project = updated, selectedClipId = clipB.id)
        autoSave(updated)
    }

    fun deleteSelectedClip() {
        val id = _uiState.value.selectedClipId ?: return
        if (_uiState.value.project.videoClips.size <= 1) return // Keep at least one clip
        pushHistoryState()
        val updatedClips = _uiState.value.project.videoClips.filterNot { it.id == id }
        val updated = _uiState.value.project.copy(videoClips = updatedClips)
        _uiState.value = _uiState.value.copy(
            project = updated,
            selectedClipId = updatedClips.firstOrNull()?.id
        )
        autoSave(updated)
    }

    fun duplicateSelectedClip() {
        val id = _uiState.value.selectedClipId ?: return
        val clip = _uiState.value.project.videoClips.find { it.id == id } ?: return
        pushHistoryState()
        val copy = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} (Dup)"
        )
        val idx = _uiState.value.project.videoClips.indexOf(clip)
        val newClips = _uiState.value.project.videoClips.toMutableList()
        newClips.add(idx + 1, copy)
        val updated = _uiState.value.project.copy(videoClips = newClips)
        _uiState.value = _uiState.value.copy(project = updated, selectedClipId = copy.id)
        autoSave(updated)
    }

    fun updateSelectedClipSpeed(speed: Float) {
        val id = _uiState.value.selectedClipId ?: return
        pushHistoryState()
        val updatedClips = _uiState.value.project.videoClips.map {
            if (it.id == id) it.copy(speed = speed.coerceIn(0.1f, 10.0f)) else it
        }
        val updated = _uiState.value.project.copy(videoClips = updatedClips)
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    fun updateSelectedClipEffect(effect: EffectType) {
        val id = _uiState.value.selectedClipId ?: return
        pushHistoryState()
        val updatedClips = _uiState.value.project.videoClips.map {
            if (it.id == id) it.copy(activeEffect = effect) else it
        }
        val updated = _uiState.value.project.copy(videoClips = updatedClips)
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    fun updateSelectedClipTransition(trans: TransitionType, durationMs: Long = 500L) {
        val id = _uiState.value.selectedClipId ?: return
        pushHistoryState()
        val updatedClips = _uiState.value.project.videoClips.map {
            if (it.id == id) it.copy(transition = trans, transitionDurationMs = durationMs) else it
        }
        val updated = _uiState.value.project.copy(videoClips = updatedClips)
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    fun updateSelectedClipAdjustment(adj: ColorAdjustment) {
        val id = _uiState.value.selectedClipId ?: return
        val updatedClips = _uiState.value.project.videoClips.map {
            if (it.id == id) it.copy(colorAdjustment = adj) else it
        }
        val updated = _uiState.value.project.copy(videoClips = updatedClips)
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    fun addKeyframeToSelectedClip() {
        val id = _uiState.value.selectedClipId ?: return
        val clip = _uiState.value.project.videoClips.find { it.id == id } ?: return
        pushHistoryState()

        // Calculate offset in clip
        var accumulated = 0L
        for (c in _uiState.value.project.videoClips) {
            if (c.id == id) break
            accumulated += c.effectiveDurationMs
        }
        val offset = (_uiState.value.currentPositionMs - accumulated).coerceIn(0L, clip.effectiveDurationMs)

        val newKf = ClipKeyframe(timeOffsetMs = offset, scale = 1.2f, rotation = 5f)
        val newKfs = (clip.keyframes + newKf).sortedBy { it.timeOffsetMs }

        val updatedClips = _uiState.value.project.videoClips.map {
            if (it.id == id) it.copy(keyframes = newKfs) else it
        }
        val updated = _uiState.value.project.copy(videoClips = updatedClips)
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    // --- Text Operations ---

    fun addTextLayer(text: String = "My Title", animation: TextAnimType = TextAnimType.POP_UP, color: Long = 0xFF00F0FF) {
        pushHistoryState()
        val newLayer = TextLayer(
            id = UUID.randomUUID().toString(),
            text = text,
            startMs = _uiState.value.currentPositionMs,
            durationMs = 3000L,
            color = color,
            animation = animation
        )
        val updated = _uiState.value.project.copy(
            textLayers = _uiState.value.project.textLayers + newLayer
        )
        _uiState.value = _uiState.value.copy(
            project = updated,
            selectedTextId = newLayer.id,
            activeToolTab = EditorToolTab.TEXT
        )
        autoSave(updated)
    }

    fun updateSelectedTextLayer(layer: TextLayer) {
        val updatedTexts = _uiState.value.project.textLayers.map {
            if (it.id == layer.id) layer else it
        }
        val updated = _uiState.value.project.copy(textLayers = updatedTexts)
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    fun setTextPosition(id: String, x: Float, y: Float) {
        val updatedTexts = _uiState.value.project.textLayers.map {
            if (it.id == id) it.copy(posX = x, posY = y) else it
        }
        val updated = _uiState.value.project.copy(textLayers = updatedTexts)
        _uiState.value = _uiState.value.copy(project = updated)
    }

    fun deleteSelectedText() {
        val id = _uiState.value.selectedTextId ?: return
        pushHistoryState()
        val updated = _uiState.value.project.copy(
            textLayers = _uiState.value.project.textLayers.filterNot { it.id == id }
        )
        _uiState.value = _uiState.value.copy(project = updated, selectedTextId = null)
        autoSave(updated)
    }

    // --- Sticker Operations ---

    fun addSticker(emoji: String, category: String = "Emoji") {
        pushHistoryState()
        val sticker = StickerLayer(
            id = UUID.randomUUID().toString(),
            emojiOrIcon = emoji,
            category = category,
            startMs = _uiState.value.currentPositionMs,
            durationMs = 3000L
        )
        val updated = _uiState.value.project.copy(
            stickerLayers = _uiState.value.project.stickerLayers + sticker
        )
        _uiState.value = _uiState.value.copy(
            project = updated,
            selectedStickerId = sticker.id,
            activeToolTab = EditorToolTab.STICKERS
        )
        autoSave(updated)
    }

    fun setStickerPosition(id: String, x: Float, y: Float) {
        val updatedStickers = _uiState.value.project.stickerLayers.map {
            if (it.id == id) it.copy(posX = x, posY = y) else it
        }
        val updated = _uiState.value.project.copy(stickerLayers = updatedStickers)
        _uiState.value = _uiState.value.copy(project = updated)
    }

    fun deleteSelectedSticker() {
        val id = _uiState.value.selectedStickerId ?: return
        pushHistoryState()
        val updated = _uiState.value.project.copy(
            stickerLayers = _uiState.value.project.stickerLayers.filterNot { it.id == id }
        )
        _uiState.value = _uiState.value.copy(project = updated, selectedStickerId = null)
        autoSave(updated)
    }

    // --- Audio Operations ---

    fun addAudioTrack(title: String, durationMs: Long = 8000L) {
        pushHistoryState()
        val audio = AudioClip(
            id = UUID.randomUUID().toString(),
            title = title,
            startMs = _uiState.value.currentPositionMs,
            durationMs = durationMs,
            trimInMs = 0L,
            trimOutMs = durationMs,
            waveforms = List(20) { (it % 6 + 3) / 10f }
        )
        val updated = _uiState.value.project.copy(
            audioClips = _uiState.value.project.audioClips + audio
        )
        _uiState.value = _uiState.value.copy(project = updated, selectedAudioId = audio.id)
        autoSave(updated)
    }

    fun extractAudioFromSelectedClip() {
        val id = _uiState.value.selectedClipId ?: return
        val clip = _uiState.value.project.videoClips.find { it.id == id } ?: return
        pushHistoryState()
        val extracted = audioEngine.extractAudioFromVideoClip(clip.title, clip.effectiveDurationMs)
        val updated = _uiState.value.project.copy(
            audioClips = _uiState.value.project.audioClips + extracted
        )
        _uiState.value = _uiState.value.copy(project = updated, selectedAudioId = extracted.id)
        autoSave(updated)
    }

    fun startVoiceOverRecording() {
        val file = audioEngine.startVoiceOverRecording()
        if (file != null) {
            _uiState.value = _uiState.value.copy(isRecordingVoiceOver = true)
        }
    }

    fun stopVoiceOverRecording() {
        val file = audioEngine.stopVoiceOverRecording()
        _uiState.value = _uiState.value.copy(isRecordingVoiceOver = false)
        if (file != null) {
            pushHistoryState()
            val voClip = AudioClip(
                id = UUID.randomUUID().toString(),
                title = "Voice-over Record",
                uri = file.absolutePath,
                startMs = _uiState.value.currentPositionMs,
                durationMs = 4000L,
                isVoiceOver = true,
                waveforms = List(16) { 0.4f + (it % 4) * 0.15f }
            )
            val updated = _uiState.value.project.copy(
                audioClips = _uiState.value.project.audioClips + voClip
            )
            _uiState.value = _uiState.value.copy(project = updated, selectedAudioId = voClip.id)
            autoSave(updated)
        }
    }

    // --- Canvas & Aspect Ratio ---

    fun setAspectRatio(ratio: AspectRatioEnum) {
        pushHistoryState()
        val updated = _uiState.value.project.copy(aspectRatio = ratio)
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    fun setCanvasBackground(bg: CanvasBackground) {
        pushHistoryState()
        val updated = _uiState.value.project.copy(canvasBackground = bg)
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    // --- AI Features ---

    fun generateAutoCaptions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAiProcessing = true,
                aiProgressText = "Analyzing audio waveforms & transcribing speech..."
            )
            delay(800)
            _uiState.value = _uiState.value.copy(
                aiProgressText = "Generating beat-aligned kinetic subtitles..."
            )
            delay(700)

            pushHistoryState()
            val sampleCaptions = listOf(
                "Welcome to the NovaCut Studio! 🎬",
                "Cut, trim and slice with precision ✂️",
                "Add insane glitch & RGB effects ⚡",
                "Exporting in crystal-clear 4K Ultra HD 🚀"
            )

            val totalDur = _uiState.value.project.totalDurationMs
            val sliceDur = (totalDur / sampleCaptions.size).coerceAtLeast(1500L)
            val generatedLayers = sampleCaptions.mapIndexed { idx, text ->
                TextLayer(
                    id = UUID.randomUUID().toString(),
                    text = text,
                    startMs = idx * sliceDur,
                    durationMs = (sliceDur - 200L).coerceAtLeast(1200L),
                    posY = 0.78f,
                    fontSize = 26f,
                    color = 0xFF00F0FF,
                    strokeColor = 0xFF000000,
                    strokeWidth = 2.5f,
                    animation = TextAnimType.TYPEWRITER
                )
            }

            val updated = _uiState.value.project.copy(
                textLayers = _uiState.value.project.textLayers + generatedLayers
            )
            _uiState.value = _uiState.value.copy(
                project = updated,
                isAiProcessing = false,
                aiProgressText = ""
            )
            autoSave(updated)
        }
    }

    fun applyAiEnhanceToSelectedClip() {
        val id = _uiState.value.selectedClipId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAiProcessing = true,
                aiProgressText = "AI Neural Video Enhancement: Color Grading & Denoising..."
            )
            delay(1000)
            pushHistoryState()
            val enhancedClips = _uiState.value.project.videoClips.map {
                if (it.id == id) {
                    it.copy(
                        colorAdjustment = ColorAdjustment(
                            contrast = 1.25f,
                            saturation = 1.35f,
                            exposure = 0.05f,
                            sharpness = 0.5f,
                            vignette = 0.35f
                        )
                    )
                } else it
            }
            val updated = _uiState.value.project.copy(videoClips = enhancedClips)
            _uiState.value = _uiState.value.copy(
                project = updated,
                isAiProcessing = false,
                aiProgressText = ""
            )
            autoSave(updated)
        }
    }

    // --- Export Pipeline ---

    fun startExport(config: ExportConfig) {
        viewModelScope.launch {
            pause()
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportProgress = 0f,
                exportStatusText = "Preparing render pipeline...",
                exportedFile = null,
                exportError = null
            )

            val result = exportEngine.exportProject(_uiState.value.project, config) { progress, curFrame, totalFrames, status ->
                _uiState.value = _uiState.value.copy(
                    exportProgress = progress,
                    exportStatusText = "$status ($curFrame/$totalFrames)"
                )
            }

            result.fold(
                onSuccess = { file ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportedFile = file,
                        exportProgress = 1f,
                        exportStatusText = "Export Successful!"
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = err.message ?: "Export failed"
                    )
                }
            )
        }
    }

    fun cancelExport() {
        exportEngine.cancelExport()
        _uiState.value = _uiState.value.copy(
            isExporting = false,
            exportProgress = 0f,
            exportStatusText = "Cancelled"
        )
    }

    fun dismissExportResult() {
        _uiState.value = _uiState.value.copy(
            exportedFile = null,
            exportError = null
        )
    }

    fun renameProject(newTitle: String) {
        if (newTitle.isBlank()) return
        val updated = _uiState.value.project.copy(title = newTitle.trim())
        _uiState.value = _uiState.value.copy(project = updated)
        autoSave(updated)
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        audioEngine.release()
    }
}
