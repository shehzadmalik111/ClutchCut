package com.example.data

import android.content.Context
import com.example.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProjectRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val dao = database.projectDao()

    val projectsFlow: Flow<List<Project>> = dao.getAllProjectsFlow().map { entities ->
        entities.map { entity ->
            try {
                ProjectJsonParser.fromJson(entity.serializedData)
            } catch (_: Exception) {
                // Fallback minimal project
                Project(
                    id = entity.id,
                    title = entity.title,
                    createdAt = entity.createdAt,
                    lastModified = entity.lastModified
                )
            }
        }
    }

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val existing = dao.getAllProjects()
        if (existing.isEmpty()) {
            val template1 = SampleTemplates.createGamingHighlightProject()
            val template2 = SampleTemplates.createCinematicVlogProject()
            val template3 = SampleTemplates.createTravelMontageProject()
            saveProject(template1)
            saveProject(template2)
            saveProject(template3)
        }
    }

    suspend fun getProject(id: String): Project? = withContext(Dispatchers.IO) {
        val entity = dao.getProjectById(id) ?: return@withContext null
        try {
            ProjectJsonParser.fromJson(entity.serializedData)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveProject(project: Project) = withContext(Dispatchers.IO) {
        val json = ProjectJsonParser.toJson(project)
        val entity = ProjectEntity(
            id = project.id,
            title = project.title,
            createdAt = project.createdAt,
            lastModified = System.currentTimeMillis(),
            durationMs = project.totalDurationMs,
            aspectRatioName = project.aspectRatio.name,
            clipCount = project.videoClips.size,
            thumbnailUri = project.videoClips.firstOrNull()?.uri,
            serializedData = json
        )
        dao.insertProject(entity)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        dao.deleteProjectById(id)
    }

    suspend fun duplicateProject(original: Project): Project = withContext(Dispatchers.IO) {
        val copy = original.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
        saveProject(copy)
        copy
    }
}
