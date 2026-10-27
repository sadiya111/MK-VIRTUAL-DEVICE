package com.example.data.repository

import com.example.browser.CookieVaultManager
import com.example.data.local.dao.BrowserDao
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ShortcutEntity
import com.example.data.local.entity.TabEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BrowserRepository(
    private val dao: BrowserDao,
    val cookieVault: CookieVaultManager
) {

    val allProfiles: Flow<List<ProfileEntity>> = dao.getAllProfiles()

    suspend fun initializeDefaultProfilesIfNeeded() {
        val existing = dao.getProfileById("personal")
        if (existing == null) {
            val personalProfile = ProfileEntity(
                id = "personal",
                name = "Personal",
                colorHex = "#3B82F6",
                avatarIcon = "person",
                searchEngine = "Google",
                customHomeUrl = "",
                isDefault = true
            )
            val workProfile = ProfileEntity(
                id = "work",
                name = "Work",
                colorHex = "#10B981",
                avatarIcon = "work",
                searchEngine = "DuckDuckGo",
                customHomeUrl = "",
                isDefault = false
            )
            val studyProfile = ProfileEntity(
                id = "study",
                name = "Study",
                colorHex = "#8B5CF6",
                avatarIcon = "school",
                searchEngine = "Google",
                customHomeUrl = "",
                isDefault = false
            )
            dao.insertProfile(personalProfile)
            dao.insertProfile(workProfile)
            dao.insertProfile(studyProfile)

            // Seed initial tab for Personal
            dao.insertTab(
                TabEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = "personal",
                    title = "New Tab",
                    url = "prism://home",
                    isPrivate = false
                )
            )

            // Seed initial speed dials for Personal
            seedShortcutsForProfile("personal")
            seedShortcutsForProfile("work")
            seedShortcutsForProfile("study")

            // Seed sample bookmarks
            dao.insertBookmark(
                BookmarkEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = "personal",
                    title = "Wikipedia",
                    url = "https://www.wikipedia.org"
                )
            )
            dao.insertBookmark(
                BookmarkEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = "personal",
                    title = "DuckDuckGo Privacy",
                    url = "https://duckduckgo.com"
                )
            )
            dao.insertBookmark(
                BookmarkEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = "work",
                    title = "GitHub",
                    url = "https://github.com"
                )
            )
            dao.insertBookmark(
                BookmarkEntity(
                    id = UUID.randomUUID().toString(),
                    profileId = "study",
                    title = "Khan Academy",
                    url = "https://www.khanacademy.org"
                )
            )
        }
    }

    private suspend fun seedShortcutsForProfile(profileId: String) {
        val shortcuts = listOf(
            ShortcutEntity(UUID.randomUUID().toString(), profileId, "Google", "https://www.google.com", "🔍", 0),
            ShortcutEntity(UUID.randomUUID().toString(), profileId, "Wikipedia", "https://www.wikipedia.org", "📚", 1),
            ShortcutEntity(UUID.randomUUID().toString(), profileId, "GitHub", "https://github.com", "🐙", 2),
            ShortcutEntity(UUID.randomUUID().toString(), profileId, "Reddit", "https://www.reddit.com", "💬", 3),
            ShortcutEntity(UUID.randomUUID().toString(), profileId, "Hacker News", "https://news.ycombinator.com", "⚡", 4),
            ShortcutEntity(UUID.randomUUID().toString(), profileId, "BBC News", "https://www.bbc.com/news", "📰", 5)
        )
        shortcuts.forEach { dao.insertShortcut(it) }
    }

    suspend fun getProfile(profileId: String): ProfileEntity? = dao.getProfileById(profileId)

    suspend fun saveProfile(profile: ProfileEntity) = dao.insertProfile(profile)

    suspend fun updateProfile(profile: ProfileEntity) = dao.updateProfile(profile)

    suspend fun deleteProfile(profile: ProfileEntity) {
        // Cascade purge all data belonging to this profile
        dao.purgeProfileTabs(profile.id)
        dao.purgeProfileBookmarks(profile.id)
        dao.purgeProfileHistory(profile.id)
        dao.purgeProfileCookies(profile.id)
        dao.purgeProfileDownloads(profile.id)
        dao.purgeProfileShortcuts(profile.id)
        dao.deleteProfile(profile)
    }

    suspend fun clearProfileData(profileId: String) {
        dao.purgeProfileHistory(profileId)
        dao.purgeProfileCookies(profileId)
        dao.purgeProfileDownloads(profileId)
        cookieVault.clearProfileCookies(profileId)
    }

    fun getTabsForProfile(profileId: String, isPrivate: Boolean): Flow<List<TabEntity>> {
        return dao.getTabsForProfile(profileId, isPrivate)
    }

    suspend fun getTabsForProfileSync(profileId: String, isPrivate: Boolean): List<TabEntity> {
        return dao.getTabsForProfileSync(profileId, isPrivate)
    }

    suspend fun createTab(profileId: String, url: String = "prism://home", isPrivate: Boolean = false): TabEntity {
        val tab = TabEntity(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            title = if (url == "prism://home") "New Tab" else url,
            url = url,
            isPrivate = isPrivate
        )
        dao.insertTab(tab)
        return tab
    }

    suspend fun updateTab(tab: TabEntity) = dao.updateTab(tab)

    suspend fun closeTab(tab: TabEntity) = dao.deleteTab(tab)

    suspend fun closeAllTabs(profileId: String, isPrivate: Boolean) = dao.deleteAllTabsForProfile(profileId, isPrivate)

    fun getBookmarks(profileId: String): Flow<List<BookmarkEntity>> = dao.getBookmarksForProfile(profileId)

    suspend fun addBookmark(profileId: String, title: String, url: String, folder: String = "Mobile Bookmarks") {
        dao.insertBookmark(
            BookmarkEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                title = title.ifBlank { url },
                url = url,
                folder = folder
            )
        )
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) = dao.deleteBookmark(bookmark)

    suspend fun isBookmarked(profileId: String, url: String): Boolean {
        return dao.getBookmarkByUrl(profileId, url) != null
    }

    fun getHistory(profileId: String): Flow<List<HistoryEntity>> = dao.getHistoryForProfile(profileId)

    suspend fun recordHistory(profileId: String, title: String, url: String) {
        if (url == "prism://home" || url == "about:blank" || url.isBlank()) return
        dao.insertHistory(
            HistoryEntity(
                profileId = profileId,
                title = title.ifBlank { url },
                url = url,
                visitedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistory(id: Long) = dao.deleteHistoryById(id)

    suspend fun clearHistory(profileId: String) = dao.clearHistoryForProfile(profileId)

    fun getShortcuts(profileId: String): Flow<List<ShortcutEntity>> = dao.getShortcutsForProfile(profileId)

    suspend fun addShortcut(profileId: String, title: String, url: String, emoji: String) {
        dao.insertShortcut(
            ShortcutEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                title = title,
                url = url,
                iconEmoji = emoji
            )
        )
    }

    suspend fun removeShortcut(shortcut: ShortcutEntity) = dao.deleteShortcut(shortcut)

    fun getDownloads(profileId: String): Flow<List<DownloadEntity>> = dao.getDownloadsForProfile(profileId)

    suspend fun addDownload(profileId: String, fileName: String, url: String, sizeBytes: Long, mimeType: String) {
        dao.insertDownload(
            DownloadEntity(
                id = UUID.randomUUID().toString(),
                profileId = profileId,
                fileName = fileName,
                url = url,
                fileSizeBytes = sizeBytes,
                mimeType = mimeType,
                status = "Completed"
            )
        )
    }

    suspend fun clearDownloads(profileId: String) = dao.clearDownloadsForProfile(profileId)
}
