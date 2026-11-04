package com.monaart.studio.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Project metadata index. Pixel data lives in `.mona` files on disk (see
 * ProjectRepository); this table only holds what the gallery needs to render
 * without opening projects, which keeps the gallery instant on large libraries.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val dpi: Int,
    @ColumnInfo(name = "layer_count") val layerCount: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "modified_at") val modifiedAt: Long,
    @ColumnInfo(name = "last_opened_at") val lastOpenedAt: Long,
    @ColumnInfo(name = "thumbnail_path") val thumbnailPath: String?,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    val favorite: Boolean = false,
    val folder: String? = null,
    @ColumnInfo(name = "recovery_state") val recoveryState: String = "Ok",
    @ColumnInfo(name = "deleted_at") val deletedAt: Long = 0,
)

/** Saved canvas size presets (built-ins are provided by code, customs live here). */
@Entity(tableName = "canvas_presets")
data class CanvasPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val width: Int,
    val height: Int,
    val dpi: Int,
    val transparent: Boolean,
    val backgroundArgb: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE deleted_at = 0 ORDER BY modified_at DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE deleted_at = 0 ORDER BY modified_at DESC")
    suspend fun all(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun byId(id: Long): ProjectEntity?

    @Upsert
    suspend fun upsert(project: ProjectEntity)

    @Query("UPDATE projects SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE projects SET deleted_at = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("UPDATE projects SET last_opened_at = :time WHERE id = :id")
    suspend fun markOpened(id: Long, time: Long)

    @Query("SELECT * FROM projects WHERE deleted_at != 0")
    suspend fun trashed(): List<ProjectEntity>

    @Query("UPDATE projects SET deleted_at = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("SELECT SUM(size_bytes) FROM projects WHERE deleted_at = 0")
    suspend fun totalBytes(): Long?

    @Query("SELECT * FROM canvas_presets ORDER BY created_at DESC")
    fun observePresets(): Flow<List<CanvasPresetEntity>>

    @Upsert
    suspend fun upsertPreset(preset: CanvasPresetEntity)

    @Query("DELETE FROM canvas_presets WHERE id = :id")
    suspend fun deletePreset(id: String)
}

@Database(
    entities = [ProjectEntity::class, CanvasPresetEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MonaDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}
