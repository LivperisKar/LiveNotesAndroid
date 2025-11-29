package gr.livperiskar.livenotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LiveNotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: LiveNotesDatabase? = null

        fun getInstance(context: Context): LiveNotesDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    LiveNotesDatabase::class.java,
                    "live_notes.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
