package gr.livperiskar.livenotes

import gr.livperiskar.livenotes.data.NoteEntity

/**
 * Επιστρέφει (titleText, previewText) όπως εμφανίζονται στη λίστα:
 * - Αν το Note έχει title → αυτός είναι ο τίτλος.
 * - Το preview είναι το content χωρίς την πρώτη γραμμή.
 * - Αν ΔΕΝ έχει title → η 1η γραμμή του content γίνεται τίτλος,
 *   το υπόλοιπο γίνεται preview.
 * - Αν είναι όλα κενά → "Untitled" χωρίς preview.
 */
fun NoteEntity.resolveTitleAndPreview(): Pair<String, String> {
    val lines = content.lineSequence().toList()

    return if (title.isNotBlank()) {
        val preview = if (lines.size <= 1) {
            ""
        } else {
            lines.drop(1).joinToString(" ").trim()
        }
        title to preview
    } else {
        if (lines.isEmpty()) {
            "Untitled" to ""
        } else {
            val titleFromContent = lines.first().trim().ifBlank { "Untitled" }
            val preview = if (lines.size <= 1) {
                ""
            } else {
                lines.drop(1).joinToString(" ").trim()
            }
            titleFromContent to preview
        }
    }
}
