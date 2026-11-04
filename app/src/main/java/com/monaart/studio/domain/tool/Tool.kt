package com.monaart.studio.domain.tool

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatPaintOff
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.HighlightAlt
import androidx.compose.material.icons.outlined.LineAxis
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/** Every tool the workspace can activate. */
enum class ToolId(val label: String, val group: ToolGroup) {
    Brush("Brush", ToolGroup.Paint),
    Pencil("Pencil", ToolGroup.Paint),
    Pen("Pen", ToolGroup.Paint),
    Eraser("Eraser", ToolGroup.Paint),
    Smudge("Smudge", ToolGroup.Paint),
    Fill("Fill", ToolGroup.Paint),
    Eyedropper("Eyedropper", ToolGroup.Utility),
    Line("Line", ToolGroup.Shape),
    Rectangle("Rectangle", ToolGroup.Shape),
    Ellipse("Ellipse", ToolGroup.Shape),
    SelectionRectangle("Rectangle select", ToolGroup.Selection),
    SelectionEllipse("Ellipse select", ToolGroup.Selection),
    SelectionLasso("Lasso select", ToolGroup.Selection),
    MagicWand("Magic wand", ToolGroup.Selection),
    ShowSelection("Selection", ToolGroup.Selection),
    Transform("Transform", ToolGroup.Transform),
    Liquify("Liquify", ToolGroup.Transform),
    Crop("Crop to selection", ToolGroup.Transform),
}

enum class ToolGroup { Paint, Shape, Selection, Transform, Utility }

/** An icon per tool, used by the tool rail. */
val ToolId.icon: ImageVector
    get() = when (this) {
        ToolId.Brush -> Icons.Outlined.Brush
        ToolId.Pencil -> Icons.Outlined.Edit
        ToolId.Pen -> Icons.Outlined.Timeline
        ToolId.Eraser -> Icons.Outlined.SquareFoot
        ToolId.Smudge -> Icons.Outlined.WaterDrop
        ToolId.Fill -> Icons.Outlined.FormatPaintOff
        ToolId.Eyedropper -> Icons.Outlined.Colorize
        ToolId.Line -> Icons.Outlined.LineAxis
        ToolId.Rectangle -> Icons.Outlined.Rectangle
        ToolId.Ellipse -> Icons.Outlined.Circle
        ToolId.SelectionRectangle -> Icons.Outlined.HighlightAlt
        ToolId.SelectionEllipse -> Icons.Outlined.Gesture
        ToolId.SelectionLasso -> Icons.Outlined.Gesture
        ToolId.MagicWand -> Icons.Outlined.AutoFixHigh
        ToolId.ShowSelection -> Icons.Outlined.Gesture
        ToolId.Transform -> Icons.Outlined.Crop
        ToolId.Liquify -> Icons.Outlined.BlurOn
        ToolId.Crop -> Icons.Outlined.Crop
    }

/** The rail shows primary tools; secondary ones live in the expandable group. */
val primaryRailTools = listOf(
    ToolId.Brush, ToolId.Pencil, ToolId.Pen, ToolId.Eraser, ToolId.Smudge,
    ToolId.Fill, ToolId.Eyedropper, ToolId.SelectionRectangle, ToolId.Transform,
)

val secondaryRailTools = listOf(
    ToolId.Line, ToolId.Rectangle, ToolId.Ellipse,
    ToolId.SelectionEllipse, ToolId.SelectionLasso, ToolId.MagicWand,
    ToolId.Liquify, ToolId.Crop,
)

/** A tool plus its current parameters, as shown in the inspector header. */
data class ActiveTool(val id: ToolId, val label: String = id.label, val icon: ImageVector = id.icon)
