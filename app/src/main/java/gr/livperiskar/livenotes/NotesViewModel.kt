package gr.livperiskar.livenotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gr.livperiskar.livenotes.data.LiveNotesDatabase
import gr.livperiskar.livenotes.data.NoteEntity
import gr.livperiskar.livenotes.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val currentNote: NoteEntity? = null,
    val searchQuery: String = ""
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        val db = LiveNotesDatabase.getInstance(application)
        repository = NoteRepository(db.noteDao())

        // Ζωντανή λίστα σημειώσεων
        viewModelScope.launch {
            repository.getNotesFlow().collect { notes ->
                _uiState.update { state ->
                    val current = state.currentNote
                    state.copy(
                        notes = notes,
                        currentNote = when {
                            current == null -> notes.firstOrNull()
                            current.id == 0L -> current // draft πριν αποθηκευτεί
                            else -> notes.find { it.id == current.id } ?: notes.firstOrNull()
                        }
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectNote(id: Long) {
        val note = _uiState.value.notes.find { it.id == id } ?: return
        _uiState.update { it.copy(currentNote = note) }
    }

    fun startNewNote() {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                currentNote = NoteEntity(
                    id = 0L,
                    title = "",
                    content = "",
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    /** AUTOSAVE – καλείται σε κάθε αλλαγή κειμένου από τον editor */
    fun updateCurrentContent(newContent: String) {
        val current = _uiState.value.currentNote
        val now = System.currentTimeMillis()

        if (current == null) {
            val draft = NoteEntity(
                id = 0L,
                title = deriveTitle(newContent),
                content = newContent,
                createdAt = now,
                updatedAt = now
            )
            _uiState.update { it.copy(currentNote = draft) }
            saveNote(draft)
        } else {
            val updated = current.copy(
                content = newContent,
                title = deriveTitle(newContent),
                updatedAt = now
            )
            _uiState.update { it.copy(currentNote = updated) }
            saveNote(updated)
        }
    }

    private fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            val id = repository.upsert(note)
            // Αν ήταν καινούρια (id=0), φρόντισε να ενημερωθεί το id στο state
            if (note.id == 0L && id != note.id) {
                _uiState.update { state ->
                    if (state.currentNote?.id == 0L) {
                        state.copy(currentNote = state.currentNote?.copy(id = id))
                    } else state
                }
            }
        }
    }

    /** ΔΙΑΓΡΑΦΗ σημειώσεων από τη λίστα */
    fun deleteNotes(ids: Set<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.deleteNotes(ids.toList())
        }
    }

    private fun deriveTitle(content: String): String {
        val firstLine = content.lineSequence().firstOrNull()?.trim() ?: ""
        if (firstLine.isEmpty()) return ""
        return if (firstLine.length <= 60) firstLine else firstLine.substring(0, 57) + "…"
    }
}
