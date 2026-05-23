package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "submissions")
data class LocalSubmission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serverId: Int? = null,
    val phone: String,
    val eventDate: String,
    val eventPlace: String,
    val totalCost: Double,
    val formattedTotal: String,
    val servicesDataJson: String, // Stringified chosen services JSON
    val pdfUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface LocalSubmissionDao {
    @Query("SELECT * FROM submissions ORDER BY timestamp DESC")
    fun getAllSubmissions(): Flow<List<LocalSubmission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: LocalSubmission): Long

    @Query("DELETE FROM submissions WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Database(entities = [LocalSubmission::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localSubmissionDao(): LocalSubmissionDao
}
