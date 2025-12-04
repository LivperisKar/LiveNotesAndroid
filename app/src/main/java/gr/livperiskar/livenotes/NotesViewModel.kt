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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

data class InlineReminder(
    val noteId: Long,
    val lineIndex: Int,
    val triggerAtMillis: Long,
    val reminderText: String,
    val rawLine: String
)

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val currentNote: NoteEntity? = null,
    val searchQuery: String = ""
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository
    private val reminderScheduler = ReminderScheduler(application)

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    // Inline reminders που προκύπτουν από τις γραμμές @rmd στο περιεχόμενο
    private val _currentInlineReminders = MutableStateFlow<List<InlineReminder>>(emptyList())
    val currentInlineReminders: StateFlow<List<InlineReminder>> = _currentInlineReminders.asStateFlow()

    // Parser για dd/MM/yyyy HH:mm
    private val reminderDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    init {
        val db = LiveNotesDatabase.getInstance(application)
        repository = NoteRepository(db.noteDao())

        // Ζωντανή λίστα σημειώσεων
        viewModelScope.launch {
            repository.getNotesFlow().collect { notes ->
                _uiState.update { state ->
                    val current = state.currentNote
                    val newCurrent = when {
                        current == null -> notes.firstOrNull()
                        current.id == 0L -> current // draft πριν αποθηκευτεί
                        else -> notes.find { it.id == current.id } ?: notes.firstOrNull()
                    }

                    // Επαναυπολογισμός inline reminders για το τρέχον note
                    val reminders = extractInlineRemindersFromContent(
                        newCurrent,
                        newCurrent?.content.orEmpty()
                    )
                    _currentInlineReminders.value = reminders

                    // Αν υπάρχει τρέχον note με κανονικό id, κάνουμε sync τα reminders του
                    val noteId = newCurrent?.id ?: 0L
                    if (noteId > 0L) {
                        val remindersForThisNote = reminders.filter { it.noteId == noteId }
                        reminderScheduler.scheduleRemindersForNote(noteId, remindersForThisNote)
                    }

                    state.copy(
                        notes = notes,
                        currentNote = newCurrent
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

        // Όταν αλλάζουμε current note, ενημερώνουμε και τα inline reminders + scheduling
        val reminders = extractInlineRemindersFromContent(note, note.content)
        _currentInlineReminders.value = reminders

        if (note.id > 0L) {
            val remindersForThisNote = reminders.filter { it.noteId == note.id }
            reminderScheduler.scheduleRemindersForNote(note.id, remindersForThisNote)
        }

        _uiState.update { it.copy(currentNote = note) }
    }

    fun startNewNote() {
        val now = System.currentTimeMillis()
        val draft = NoteEntity(
            id = 0L,
            title = "",
            content = "",
            createdAt = now,
            updatedAt = now
        )

        _uiState.update {
            it.copy(currentNote = draft)
        }

        // Άδειο περιεχόμενο ⇒ άδειες υπενθυμίσεις
        _currentInlineReminders.value = emptyList()
    }

    /** AUTOSAVE – καλείται σε κάθε αλλαγή κειμένου από τον editor */
    fun updateCurrentContent(newContent: String) {
        val current = _uiState.value.currentNote
        val now = System.currentTimeMillis()

        if (current == null) {
            // Καινούριος draft
            val draft = NoteEntity(
                id = 0L,
                title = deriveTitle(newContent),
                content = newContent,
                createdAt = now,
                updatedAt = now
            )
            _uiState.update { it.copy(currentNote = draft) }
            saveNote(draft)

            // Inline reminders για draft (id = 0L προς το παρόν)
            _currentInlineReminders.value =
                extractInlineRemindersFromContent(draft, newContent)
            // Δεν κάνουμε schedule εδώ, γιατί δεν έχουμε ακόμα πραγματικό id
        } else {
            val updated = current.copy(
                content = newContent,
                title = deriveTitle(newContent),
                updatedAt = now
            )
            _uiState.update { it.copy(currentNote = updated) }
            saveNote(updated)

            // Inline reminders για το updated note
            val reminders = extractInlineRemindersFromContent(updated, newContent)
            _currentInlineReminders.value = reminders

            // Αν έχει κανονικό id, κάνουμε sync τα reminders του
            if (updated.id > 0L) {
                val remindersForThisNote = reminders.filter { it.noteId == updated.id }
                reminderScheduler.scheduleRemindersForNote(
                    updated.id,
                    remindersForThisNote
                )
            }
        }
    }

    private fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            val id = repository.upsert(note)
            // Αν ήταν καινούρια (id = 0), φρόντισε να ενημερωθεί το id στο state
            if (note.id == 0L && id != note.id) {
                _uiState.update { state ->
                    val current = state.currentNote
                    if (current?.id == 0L) {
                        val withRealId = current.copy(id = id)

                        // Επαναυπολογισμός inline reminders με το σωστό noteId
                        val reminders = extractInlineRemindersFromContent(
                            withRealId,
                            withRealId.content
                        )
                        _currentInlineReminders.value = reminders

                        // Πρώτο scheduling για αυτή τη νέα σημείωση
                        val remindersForThisNote = reminders.filter { it.noteId == withRealId.id }
                        reminderScheduler.scheduleRemindersForNote(
                            withRealId.id,
                            remindersForThisNote
                        )

                        state.copy(currentNote = withRealId)
                    } else {
                        state
                    }
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

    // ======================
    //  Inline reminders parser
    // ======================

    private fun extractInlineRemindersFromContent(
        note: NoteEntity?,
        content: String
    ): List<InlineReminder> {
        val id = note?.id ?: 0L
        if (content.isBlank()) return emptyList()

        val lines = content.lines()
        if (lines.isEmpty()) return emptyList()

        val result = mutableListOf<InlineReminder>()

        lines.forEachIndexed { index, line ->
            val reminder = parseReminderLine(id, index, line)
            if (reminder != null) {
                result.add(reminder)
            }
        }

        return result
    }

    private fun parseReminderLine(
        noteId: Long,
        lineIndex: Int,
        line: String
    ): InlineReminder? {
        val trimmed = line.trim()
        if (!trimmed.startsWith("@rmd")) return null

        // Βγάζουμε το "@rmd" και τα πρώτα κενά
        val afterMarker = trimmed.removePrefix("@rmd").trimStart()
        if (afterMarker.isEmpty()) return null

        // Πρώτη κάθετος: πριν από αυτήν είναι η ημερομηνία/ώρα
        val firstPipe = afterMarker.indexOf('|')
        if (firstPipe == -1) return null

        val dateTimePart = afterMarker.substring(0, firstPipe).trim()
        val remaining = afterMarker.substring(firstPipe + 1)

        // Δεύτερη κάθετος: ανάμεσα στις κάθετες είναι το κείμενο υπενθύμισης
        val secondPipe = remaining.indexOf('|')
        if (secondPipe == -1) return null

        val reminderText = remaining.substring(0, secondPipe).trim()
        if (reminderText.isEmpty()) return null

        val triggerAtMillis = try {
            reminderDateFormat.parse(dateTimePart)?.time ?: return null
        } catch (e: ParseException) {
            return null
        }

        return InlineReminder(
            noteId = noteId,
            lineIndex = lineIndex,
            triggerAtMillis = triggerAtMillis,
            reminderText = reminderText,
            rawLine = line
        )
    }
}
