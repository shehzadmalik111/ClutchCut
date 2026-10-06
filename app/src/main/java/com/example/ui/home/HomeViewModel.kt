package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.data.SampleTemplates
import com.example.model.AspectRatioEnum
import com.example.model.Project
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProjectRepository(application)

    val projects: StateFlow<List<Project>> = repository.projectsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    fun createNewProject(aspectRatio: AspectRatioEnum, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val newProject = Project(
                id = java.util.UUID.randomUUID().toString(),
                title = "NovaCut Project ${projects.value.size + 1}",
                aspectRatio = aspectRatio,
                videoClips = listOf(
                    SampleTemplates.createGamingHighlightProject().videoClips.first().copy(
                        id = java.util.UUID.randomUUID().toString(),
                        title = "Main Scene HD"
                    )
                )
            )
            repository.saveProject(newProject)
            onCreated(newProject.id)
        }
    }

    fun duplicateProject(project: Project) {
        viewModelScope.launch {
            repository.duplicateProject(project)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }

    fun loadTemplate(templateType: String, onLoaded: (String) -> Unit) {
        viewModelScope.launch {
            val template = when (templateType) {
                "cinematic" -> SampleTemplates.createCinematicVlogProject()
                "travel" -> SampleTemplates.createTravelMontageProject()
                else -> SampleTemplates.createGamingHighlightProject()
            }
            repository.saveProject(template)
            onLoaded(template.id)
        }
    }

    fun clearAppCache() {
        viewModelScope.launch {
            try {
                val app = getApplication<Application>()
                app.cacheDir.deleteRecursively()
                val exportDir = File(app.getExternalFilesDir(null), "NovaCutExports")
                if (exportDir.exists()) exportDir.deleteRecursively()
            } catch (_: Exception) {}
        }
    }
}
