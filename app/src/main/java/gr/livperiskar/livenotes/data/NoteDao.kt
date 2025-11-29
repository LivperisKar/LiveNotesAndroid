package gr.livperiskar.livenotes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
        ORDER BY updatedAt DESC
    """)
    suspend fun search(query: String): List<NoteEntity>
}
