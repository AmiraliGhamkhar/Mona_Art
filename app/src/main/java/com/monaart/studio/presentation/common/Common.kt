package com.monaart.studio.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.monaart.studio.core.design.Mona
import com.monaart.studio.domain.model.ProjectSummary
import java.io.File

/** Cross-session relative time ("3 min ago"). */
fun relativeTime(timestamp: Long): String {
    if (timestamp <= 0) return "never"
    val delta = System.currentTimeMillis() - timestamp
    return when {
        delta < 60_000 -> "just now"
        delta < 3_600_000 -> "${delta / 60_000} min ago"
        delta < 86_400_000 -> "${delta / 3_600_000} h ago"
        delta < 7L * 86_400_000 -> "${delta / 86_400_000} d ago"
        else -> java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
    }
}

/**
 * Decodes a project thumbnail from disk. Decoding is memoised per file version
 * so scrolling the gallery does not re-decode PNGs.
 */
@Composable
fun ProjectThumbnail(project: ProjectSummary, modifier: Modifier = Modifier) {
    val colors = Mona.colors
    val bitmap: ImageBitmap? = remember(project.thumbnailPath, project.modifiedAt) {
        project.thumbnailPath?.let { path ->
            runCatching {
                val file = File(path)
                if (!file.exists()) null else
                    android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
            }.getOrNull()
        }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(Mona.shapes.md))
            .background(colors.surface2),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "${project.name} preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                Icons.Outlined.BrokenImage,
                contentDescription = "No preview",
                tint = colors.textTertiary,
            )
        }
    }
}

/** A labelled key/value row used across settings and document info. */
@Composable
fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Mona.colors.textSecondary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = Mona.colors.textPrimary)
    }
}

/** Compact byte formatting for dense UI (layers panel, storage rows). */
fun formatBytesShort(bytes: Long): String = when {
    bytes >= 1L shl 30 -> "%.1f GB".format(bytes / (1L shl 30).toDouble())
    bytes >= 1L shl 20 -> "%.1f MB".format(bytes / (1L shl 20).toDouble())
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes B"
}

/** Checkerboard used behind transparent artwork. */
@Composable
fun CheckerboardBackground(modifier: Modifier = Modifier, cell: Float = 12f) {
    val colors = Mona.colors
    androidx.compose.foundation.Canvas(modifier) {
        val c = cell.dp.toPx()
        val light = if (colors.isDark) Color(0xFF2A2733) else Color(0xFFE8E4F0)
        val dark = if (colors.isDark) Color(0xFF201D28) else Color(0xFFD8D3E2)
        drawRect(light)
        var y = 0f
        var row = 0
        while (y < size.height) {
            var x = if (row % 2 == 0) c else 0f
            while (x < size.width) {
                drawRect(dark, topLeft = androidx.compose.ui.geometry.Offset(x, y), size = androidx.compose.ui.geometry.Size(c, c))
                x += c * 2
            }
            y += c
            row++
        }
    }
}
