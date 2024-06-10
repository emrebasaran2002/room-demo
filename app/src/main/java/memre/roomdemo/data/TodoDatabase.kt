package memre.roomdemo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Database(entities = [Todo::class], version = 2, exportSchema = true)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun getDao(): TodoDao

    companion object {

        private var realInstance: TodoDatabase? = null
        private var testInstance: TodoDatabase? = null

        private val databaseLock = ReentrantLock()

        /**
         * Obtain a real instance of [TodoDatabase].
         */
        fun getInstance(context: Context): TodoDatabase = databaseLock.withLock {
            realInstance ?: run {
                val database = Room.databaseBuilder(context.applicationContext,
                    TodoDatabase::class.java, "todo_database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                realInstance = database
                database
            }
        }

        /**
         * Obtain a fake instance of [TodoDatabase] for testing. This database will
         * reside in main memory and be destroyed when the testing is complete.
         */
        fun getTestInstance(context: Context): TodoDatabase = databaseLock.withLock {
            testInstance ?: run {
                val database = Room.inMemoryDatabaseBuilder(context.applicationContext,
                    TodoDatabase::class.java)
                    .allowMainThreadQueries()
                    .build()
                testInstance = database
                database
            }
        }
    }
}