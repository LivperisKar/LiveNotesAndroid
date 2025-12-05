package gr.livperiskar.livenotes

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Συγχρονίζει τα reminders μιας συγκεκριμένης σημείωσης με το WorkManager.
     *
     * - Πρώτα ακυρώνει ό,τι παλιά jobs υπάρχουν για αυτό το noteId.
     * - Μετά, για κάθε InlineReminder, προγραμματίζει ένα OneTimeWorkRequest.
     */
    fun scheduleRemindersForNote(
        noteId: Long,
        reminders: List<InlineReminder>
    ) {
        val tag = "note-reminders-$noteId"

        // Ακύρωση όλων των παλιών για αυτό το note
        workManager.cancelAllWorkByTag(tag)

        if (reminders.isEmpty()) return

        val now = System.currentTimeMillis()

        reminders.forEach { reminder ->
            val delayMillis = reminder.triggerAtMillis - now
            // Αν είναι ήδη στο παρελθόν ή ακριβώς τώρα, δεν έχει νόημα να το προγραμματίσουμε
            if (delayMillis <= 0L) {
                return@forEach
            }

            val data = workDataOf(
                ReminderWorker.KEY_NOTE_ID to noteId,
                ReminderWorker.KEY_TEXT to reminder.reminderText,
                ReminderWorker.KEY_REMINDER_LINE_INDEX to reminder.lineIndex
            )

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(data)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .build()

            workManager.enqueue(request)
        }
    }
}
