package gr.livperiskar.livenotes.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val dao: NoteDao
) {
    fun getNotesFlow(): Flow<List<NoteEntity>> = dao.getAllNotesFlow()

    suspend fun upsert(note: NoteEntity): Long = dao.upsert(note)

    suspend fun deleteNotes(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun searchNotes(query: String): List<NoteEntity> = dao.search(query)

    suspend fun getNote(id: Long): NoteEntity? = dao.getById(id)
}
