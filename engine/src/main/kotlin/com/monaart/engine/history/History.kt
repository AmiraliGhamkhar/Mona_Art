package com.monaart.engine.history

import com.monaart.engine.paint.RectI

/**
 * Command-based undo history with coalescing of tile snapshots.
 *
 * Every mutation is a [DocCommand]; the stack is bounded by both a step count
 * and an estimated byte budget so long sessions cannot grow without limit.
 * Snapshot commands restore exact pixel state, which makes undo of brush,
 * filter and transform operations trivially correct.
 */
interface DocCommand {
    val label: String
    fun undo()
    fun redo()
    /** Estimated retained bytes, used for the memory budget. */
    fun byteSize(): Long = 0L
    /** Merge a following command into this one (e.g. consecutive tile edits of a stroke). */
    fun mergeNext(other: DocCommand): DocCommand? = null
}

class History(private val maxSteps: Int = 120, private val byteBudget: Long = 384L * 1024 * 1024) {
    private val undoStack = ArrayDeque<DocCommand>()
    private val redoStack = ArrayDeque<DocCommand>()

    var revision: Long = 0
        private set

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val undoLabel: String? get() = undoStack.lastOrNull()?.label
    val redoLabel: String? get() = redoStack.lastOrNull()?.label
    val undoCount: Int get() = undoStack.size
    val redoCount: Int get() = redoStack.size

    fun push(cmd: DocCommand) {
        redoStack.clear()
        val last = undoStack.lastOrNull()
        val merged = last?.mergeNext(cmd)
        if (merged != null) {
            undoStack.removeLast()
            undoStack.addLast(merged)
        } else {
            undoStack.addLast(cmd)
        }
        trim()
        revision++
    }

    fun undo(): Boolean {
        val cmd = undoStack.removeLastOrNull() ?: return false
        cmd.undo()
        redoStack.addLast(cmd)
        revision++
        return true
    }

    fun redo(): Boolean {
        val cmd = redoStack.removeLastOrNull() ?: return false
        cmd.redo()
        undoStack.addLast(cmd)
        revision++
        return true
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        revision++
    }

    /** Drops the oldest undo entries until both limits are satisfied. */
    private fun trim() {
        var bytes = estimateBytes()
        while (undoStack.size > maxSteps) {
            val removed = undoStack.removeFirst()
            bytes -= removed.byteSize()
        }
        while (undoStack.size > 1 && bytes > byteBudget) {
            val removed = undoStack.removeFirst()
            bytes -= removed.byteSize()
        }
    }

    fun estimateBytes(): Long {
        var n = 0L
        for (c in undoStack) n += c.byteSize()
        for (c in redoStack) n += c.byteSize()
        return n
    }

    /** A command that groups sub-commands into a single history step. */
    class Group(override val label: String, private val commands: List<DocCommand>) : DocCommand {
        override fun undo() { for (i in commands.indices.reversed()) commands[i].undo() }
        override fun redo() { for (c in commands) c.redo() }
        override fun byteSize(): Long = commands.sumOf { it.byteSize() }
    }

    /** A generic lambda command for non-pixel operations (layer order, properties, selection). */
    class Lambda(
        override val label: String,
        private val onUndo: () -> Unit,
        private val onRedo: () -> Unit,
        private val bytes: () -> Long = { 0L },
    ) : DocCommand {
        override fun undo() = onUndo()
        override fun redo() = onRedo()
        override fun byteSize(): Long = bytes()
    }

    /**
     * Undoable tile edit. Each entry maps a tile key to its content *before* and
     * *after* the edit; a null value means the tile did not exist at that point,
     * which is what makes "paint on an empty layer, then undo" restore a truly
     * blank layer rather than an allocated transparent one.
     */
    class TileDiff(
        override val label: String,
        val rect: RectI,
        private val before: Map<Long, IntArray?>,
        private val after: Map<Long, IntArray?>,
        private val target: com.monaart.engine.document.TileSurface,
    ) : DocCommand {
        override fun undo() = apply(before)
        override fun redo() = apply(after)

        private fun apply(state: Map<Long, IntArray?>) {
            for ((k, data) in state) {
                if (data == null) target.dropTileByKey(k) else target.putTileByKey(k, data)
            }
            target.touch()
        }

        override fun byteSize(): Long {
            var n = 0L
            for (v in before.values) if (v != null) n += v.size * 4L
            for (v in after.values) if (v != null) n += v.size * 4L
            return n
        }
    }

    /** Undoable canvas-region pixel edit captured as before/after snapshots. */
    class PixelDiff(
        override val label: String,
        val rect: RectI,
        private val before: com.monaart.engine.document.TileSnapshot,
        private val after: com.monaart.engine.document.TileSnapshot,
        private val target: com.monaart.engine.document.TileSurface,
    ) : DocCommand {
        override fun undo() { target.restore(before); target.touch() }
        override fun redo() { target.restore(after); target.touch() }
        override fun byteSize(): Long = before.byteSize() + after.byteSize()
    }
}
