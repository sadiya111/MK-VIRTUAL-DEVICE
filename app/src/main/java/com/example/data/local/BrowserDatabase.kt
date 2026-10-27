package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BrowserDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ProfileCookieEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ShortcutEntity
import com.example.data.local.entity.TabEntity

@Database(
    entities = [
        ProfileEntity::class,
        TabEntity::class,
        BookmarkEntity::class,
        HistoryEntity::class,
        ProfileCookieEntity::class,
        DownloadEntity::class,
        ShortcutEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getInstance(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "prism_browser.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
