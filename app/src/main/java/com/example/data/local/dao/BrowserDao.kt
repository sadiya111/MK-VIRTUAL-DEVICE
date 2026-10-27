package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ProfileCookieEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ShortcutEntity
import com.example.data.local.entity.TabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {

    // --- Profiles ---
    @Query("SELECT * FROM profiles ORDER BY createdAt ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    // --- Tabs ---
    @Query("SELECT * FROM tabs WHERE profileId = :profileId AND isPrivate = :isPrivate ORDER BY lastAccessedAt DESC")
    fun getTabsForProfile(profileId: String, isPrivate: Boolean): Flow<List<TabEntity>>

    @Query("SELECT * FROM tabs WHERE profileId = :profileId AND isPrivate = :isPrivate ORDER BY lastAccessedAt DESC")
    suspend fun getTabsForProfileSync(profileId: String, isPrivate: Boolean): List<TabEntity>

    @Query("SELECT * FROM tabs WHERE id = :tabId LIMIT 1")
    suspend fun getTabById(tabId: String): TabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity)

    @Update
    suspend fun updateTab(tab: TabEntity)

    @Delete
    suspend fun deleteTab(tab: TabEntity)

    @Query("DELETE FROM tabs WHERE profileId = :profileId AND isPrivate = :isPrivate")
    suspend fun deleteAllTabsForProfile(profileId: String, isPrivate: Boolean)

    // --- Bookmarks ---
    @Query("SELECT * FROM bookmarks WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getBookmarksForProfile(profileId: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE profileId = :profileId AND url = :url LIMIT 1")
    suspend fun getBookmarkByUrl(profileId: String, url: String): BookmarkEntity?

    // --- History ---
    @Query("SELECT * FROM history WHERE profileId = :profileId ORDER BY visitedAt DESC")
    fun getHistoryForProfile(profileId: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history WHERE profileId = :profileId")
    suspend fun clearHistoryForProfile(profileId: String)

    @Query("DELETE FROM history WHERE profileId = :profileId AND visitedAt >= :fromTimestamp")
    suspend fun clearHistorySince(profileId: String, fromTimestamp: Long)

    // --- Profile Cookies ---
    @Query("SELECT * FROM profile_cookies WHERE profileId = :profileId")
    suspend fun getCookiesForProfile(profileId: String): List<ProfileCookieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCookie(cookie: ProfileCookieEntity)

    @Query("DELETE FROM profile_cookies WHERE profileId = :profileId")
    suspend fun clearCookiesForProfile(profileId: String)

    // --- Downloads ---
    @Query("SELECT * FROM downloads WHERE profileId = :profileId ORDER BY downloadedAt DESC")
    fun getDownloadsForProfile(profileId: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Delete
    suspend fun deleteDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE profileId = :profileId")
    suspend fun clearDownloadsForProfile(profileId: String)

    // --- Shortcuts ---
    @Query("SELECT * FROM shortcuts WHERE profileId = :profileId ORDER BY orderIndex ASC")
    fun getShortcutsForProfile(profileId: String): Flow<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortcut(shortcut: ShortcutEntity)

    @Delete
    suspend fun deleteShortcut(shortcut: ShortcutEntity)

    // --- Full Profile Cleanup ---
    @Query("DELETE FROM tabs WHERE profileId = :profileId")
    suspend fun purgeProfileTabs(profileId: String)

    @Query("DELETE FROM bookmarks WHERE profileId = :profileId")
    suspend fun purgeProfileBookmarks(profileId: String)

    @Query("DELETE FROM history WHERE profileId = :profileId")
    suspend fun purgeProfileHistory(profileId: String)

    @Query("DELETE FROM profile_cookies WHERE profileId = :profileId")
    suspend fun purgeProfileCookies(profileId: String)

    @Query("DELETE FROM downloads WHERE profileId = :profileId")
    suspend fun purgeProfileDownloads(profileId: String)

    @Query("DELETE FROM shortcuts WHERE profileId = :profileId")
    suspend fun purgeProfileShortcuts(profileId: String)
}
