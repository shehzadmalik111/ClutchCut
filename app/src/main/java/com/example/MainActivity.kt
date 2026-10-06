package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioBlack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioBlack
                ) {
                    val homeViewModel: HomeViewModel = viewModel()
                    val editorViewModel: EditorViewModel = viewModel()

                    var currentScreen by remember { mutableStateOf<String>("home") }
                    var activeProjectId by remember { mutableStateOf<String?>(null) }

                    AnimatedContent(
                        targetState = currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            "home" -> {
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    onOpenProject = { projectId ->
                                        activeProjectId = projectId
                                        editorViewModel.loadProject(projectId)
                                        currentScreen = "editor"
                                    }
                                )
                            }
                            "editor" -> {
                                EditorScreen(
                                    viewModel = editorViewModel,
                                    onNavigateBack = {
                                        currentScreen = "home"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
