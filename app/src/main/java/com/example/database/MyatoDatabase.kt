package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        DownloadEntity::class,
        PlayHistoryEntity::class,
        PlaybackQueueEntity::class,
        SongBookmarkEntity::class,
        DeviceEqualizerEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MyatoDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun downloadDao(): DownloadDao
    abstract fun historyDao(): HistoryDao
    abstract fun queueDao(): QueueDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun deviceEqualizerDao(): DeviceEqualizerDao

    companion object {
        @Volatile
        private var INSTANCE: MyatoDatabase? = null

        fun getInstance(context: Context): MyatoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyatoDatabase::class.java,
                    "myato_player_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
