package com.example.ui.editor

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.model.ExportConfig
import com.example.ui.theme.*
import java.io.File

@Composable
fun ExportDialog(
    durationMs: Long,
    isExporting: Boolean,
    exportProgress: Float,
    statusText: String,
    exportedFile: File?,
    exportError: String?,
    onDismiss: () -> Unit,
    onStartExport: (ExportConfig) -> Unit,
    onCancelExport: () -> Unit
) {
    val context = LocalContext.current
    var resolution by remember { mutableStateOf("1080p") }
    var fps by remember { mutableStateOf(30) }
    var quality by remember { mutableStateOf("High") }

    // Estimate file size in MB
    val durationSec = durationMs / 1000f
    val bitrateMbps = when (resolution) {
        "480p" -> 3f
        "720p" -> 6f
        "1080p" -> 14f
        "1440p" -> 24f
        "4K" -> 45f
        else -> 12f
    } * (if (quality == "High") 1.2f else if (quality == "Low") 0.7f else 1.0f) * (fps / 30f)
    val estimatedMb = String.format("%.1f", (bitrateMbps * durationSec) / 8f)

    Dialog(onDismissRequest = { if (!isExporting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (exportedFile != null) "Export Succeeded!" else "Export Video",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (!isExporting) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (exportedFile != null) {
                    // Exported Successfully Screen
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = NeonGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Video saved to Gallery / Movies",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = exportedFile.name,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        exportedFile
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                                } catch (_: Exception) {
                                    // Fallback general share
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "Exported with NovaCut Video Editor: ${exportedFile.name}")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            modifier = Modifier.weight(1f).testTag("share_video_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Done", color = Color.White)
                        }
                    }
                } else if (isExporting) {
                    // Progress Screen
                    CircularProgressIndicator(
                        progress = { exportProgress },
                        modifier = Modifier.size(72.dp),
                        color = NeonCyan,
                        strokeWidth = 6.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${(exportProgress * 100).toInt()}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = onCancelExport,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                        modifier = Modifier.testTag("cancel_export_button")
                    ) {
                        Text("Cancel Export")
                    }
                } else {
                    // Settings configuration before export
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Resolution", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("720p", "1080p", "1440p", "4K").forEach { res ->
                                FilterChip(
                                    selected = resolution == res,
                                    onClick = { resolution = res },
                                    label = { Text(res, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Frame Rate (FPS)", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(24, 30, 60).forEach { f ->
                                FilterChip(
                                    selected = fps == f,
                                    onClick = { fps = f },
                                    label = { Text("${f}fps", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Quality / Bitrate", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Low", "Medium", "High").forEach { q ->
                                FilterChip(
                                    selected = quality == q,
                                    onClick = { quality = q },
                                    label = { Text(q, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Size:", fontSize = 12.sp, color = TextSecondary)
                                Text("~${estimatedMb} MB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                            }
                        }

                        if (exportError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Error: $exportError", fontSize = 11.sp, color = NeonRed)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                onStartExport(
                                    ExportConfig(
                                        resolution = resolution,
                                        fps = fps,
                                        quality = quality,
                                        bitrateMbps = bitrateMbps.toInt()
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_export_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
