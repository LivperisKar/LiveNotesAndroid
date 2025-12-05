package gr.livperiskar.livenotes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "livenotes_reminders"

        const val KEY_NOTE_ID = "note_id"
        const val KEY_TEXT = "text"
        const val KEY_REMINDER_LINE_INDEX = "reminder_line_index"

        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_REMINDER_LINE_INDEX = "extra_reminder_line_index"
    }

    override suspend fun doWork(): Result {
        val text = inputData.getString(KEY_TEXT) ?: return Result.failure()
        val noteId = inputData.getLong(KEY_NOTE_ID, 0L)
        val lineIndex = inputData.getInt(KEY_REMINDER_LINE_INDEX, -1)

        if (noteId <= 0L) {
            return Result.failure()
        }

        createNotificationChannel()
        showNotification(noteId, text, lineIndex)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "LiveNotes reminders"
            val descriptionText = "Reminders created inside notes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager? =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        noteId: Long,
        text: String,
        reminderLineIndex: Int
    ) {
        // Κάνουμε το id deterministic αλλά μοναδικό ανά (noteId + text + lineIndex)
        val notificationId =
            (noteId.hashCode() xor text.hashCode() xor reminderLineIndex.hashCode())

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTE_ID, noteId)
            putExtra(EXTRA_REMINDER_LINE_INDEX, reminderLineIndex)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            flags
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // προς το παρόν το app icon
            .setContentTitle("LiveNotes reminder")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(notificationId, builder.build())
        }
    }
}
