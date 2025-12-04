package gr.livperiskar.livenotes

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class ReminderScheduler(context: Context) {

    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    fun scheduleRemindersForNote(
        noteId: Long,
        reminders: List<InlineReminder>
    ) {
        if (noteId <= 0L) return

        val tag = "note-reminders-$noteId"

        // 1. Καθαρίζουμε παλιά work για αυτό το note (tag based)
        workManager.cancelAllWorkByTag(tag)

        if (reminders.isEmpty()) {
            return
        }

        val now = System.currentTimeMillis()

        reminders.forEach { reminder ->
            val delayMillis = reminder.triggerAtMillis - now
            if (delayMillis <= 0L) {
                // Στο παρελθόν ή τώρα – το αγνοούμε προς το παρόν
                return@forEach
            }

            val uniqueWorkName = "reminder_${reminder.noteId}_${reminder.lineIndex}"

            val inputData = workDataOf(
                ReminderWorker.KEY_NOTE_ID to reminder.noteId,
                ReminderWorker.KEY_TEXT to reminder.reminderText
            )

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .addTag(tag)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
