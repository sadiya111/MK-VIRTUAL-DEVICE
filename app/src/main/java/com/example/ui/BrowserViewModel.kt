package com.example.ui

import android.app.Application
import android.webkit.CookieManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.browser.CookieVaultManager
import com.example.browser.FindInPageState
import com.example.browser.ProfilePresets
import com.example.browser.ScreenDestination
import com.example.browser.SearchEngine
import com.example.browser.SearchSuggestion
import com.example.browser.SecurityStatus
import com.example.browser.UrlUtils
import com.example.data.local.BrowserDatabase
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.ProfileEntity
import com.example.data.local.entity.ShortcutEntity
import com.example.data.local.entity.TabEntity
import com.example.data.repository.BrowserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class BrowserSettings(
    val themeMode: String = "System", // "System", "Light", "Dark"
    val defaultSearchEngine: String = "Google",
    val javascriptEnabled: Boolean = true,
    val blockPopups: Boolean = true,
    val doNotTrack: Boolean = true,
    val textScaling: Int = 100, // percentage
    val desktopSiteDefault: Boolean = false
)

data class BrowserState(
    val currentScreen: ScreenDestination = ScreenDestination.BROWSER_MAIN,
    val navigationStack: List<ScreenDestination> = listOf(ScreenDestination.BROWSER_MAIN),
    val profiles: List<ProfileEntity> = emptyList(),
    val activeProfile: ProfileEntity? = null,
    val isPrivateMode: Boolean = false,
    val tabs: List<TabEntity> = emptyList(),
    val activeTab: TabEntity? = null,
    val isLoading: Boolean = false,
    val loadingProgress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val urlInput: String = "",
    val isEditingUrl: Boolean = false,
    val searchSuggestions: List<SearchSuggestion> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val history: List<HistoryEntity> = emptyList(),
    val shortcuts: List<ShortcutEntity> = emptyList(),
    val downloads: List<DownloadEntity> = emptyList(),
    val findInPage: FindInPageState = FindInPageState(),
    val isReaderMode: Boolean = false,
    val readerContent: String = "",
    val isDesktopSite: Boolean = false,
    val isBookmarked: Boolean = false,
    val settings: BrowserSettings = BrowserSettings(),
    val showProfileSwitcher: Boolean = false,
    val showMoreMenu: Boolean = false,
    val editingProfile: ProfileEntity? = null,
    val messageSnackbar: String? = null
)

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BrowserDatabase.getInstance(application)
    private val cookieVault = CookieVaultManager(db.browserDao())
    val repository = BrowserRepository(db.browserDao(), cookieVault)

    private val _uiState = MutableStateFlow(BrowserState())
    val uiState: StateFlow<BrowserState> = _uiState.asStateFlow()

    // Navigation callbacks for the active WebView
    var onWebGoBack: (() -> Unit)? = null
    var onWebGoForward: (() -> Unit)? = null
    var onWebReload: (() -> Unit)? = null
    var onWebStop: (() -> Unit)? = null
    var onWebLoadUrl: ((String) -> Unit)? = null
    var onWebFindText: ((String) -> Unit)? = null
    var onWebFindNext: ((Boolean) -> Unit)? = null
    var onWebClearMatches: (() -> Unit)? = null
    var onWebExtractReader: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            repository.initializeDefaultProfilesIfNeeded()
            observeProfiles()
        }
    }

    private fun observeProfiles() {
        viewModelScope.launch {
            repository.allProfiles.collectLatest { profileList ->
                _uiState.update { state ->
                    val active = state.activeProfile?.let { curr ->
                        profileList.find { it.id == curr.id }
                    } ?: profileList.firstOrNull { it.isDefault } ?: profileList.firstOrNull()
                    state.copy(profiles = profileList, activeProfile = active)
                }

                val currentActive = _uiState.value.activeProfile
                if (currentActive != null && _uiState.value.tabs.isEmpty()) {
                    loadProfileData(currentActive.id, _uiState.value.isPrivateMode)
                }
            }
        }
    }

    private fun loadProfileData(profileId: String, isPrivate: Boolean) {
        viewModelScope.launch {
            // Observe tabs
            repository.getTabsForProfile(profileId, isPrivate).collectLatest { tabList ->
                _uiState.update { state ->
                    val active = if (tabList.isEmpty()) null else {
                        state.activeTab?.let { curr -> tabList.find { it.id == curr.id } } ?: tabList.firstOrNull()
                    }
                    state.copy(
                        tabs = tabList,
                        activeTab = active,
                        isDesktopSite = active?.isDesktopSite ?: false,
                        urlInput = active?.url ?: "prism://home"
                    )
                }

                // If no tabs exist, create one automatically
                if (tabList.isEmpty()) {
                    val newTab = repository.createTab(profileId, "prism://home", isPrivate)
                    _uiState.update { it.copy(activeTab = newTab) }
                }
            }
        }

        viewModelScope.launch {
            repository.getBookmarks(profileId).collectLatest { bList ->
                _uiState.update { it.copy(bookmarks = bList) }
                checkCurrentBookmarkStatus()
            }
        }

        viewModelScope.launch {
            repository.getHistory(profileId).collectLatest { hList ->
                _uiState.update { it.copy(history = hList) }
            }
        }

        viewModelScope.launch {
            repository.getShortcuts(profileId).collectLatest { sList ->
                _uiState.update { it.copy(shortcuts = sList) }
            }
        }

        viewModelScope.launch {
            repository.getDownloads(profileId).collectLatest { dList ->
                _uiState.update { it.copy(downloads = dList) }
            }
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        _uiState.update {
            val updatedStack = it.navigationStack + destination
            it.copy(currentScreen = destination, navigationStack = updatedStack, showMoreMenu = false)
        }
    }

    fun navigateBack(): Boolean {
        val stack = _uiState.value.navigationStack
        if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            val prev = newStack.last()
            _uiState.update { it.copy(currentScreen = prev, navigationStack = newStack) }
            return true
        }
        return false
    }

    fun switchProfile(targetProfileId: String) {
        val current = _uiState.value.activeProfile
        val currentUrls = _uiState.value.tabs.map { it.url }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showProfileSwitcher = false) }

            // 1. Genuine cookie and storage isolation transition
            cookieVault.switchProfileSession(
                oldProfileId = current?.id,
                newProfileId = targetProfileId,
                isPrivate = false,
                currentUrls = currentUrls
            )

            val target = repository.getProfile(targetProfileId)
            _uiState.update {
                it.copy(
                    activeProfile = target,
                    isPrivateMode = false,
                    isLoading = false,
                    messageSnackbar = "Switched to ${target?.name ?: "Profile"}"
                )
            }

            target?.let {
                loadProfileData(it.id, isPrivate = false)
            }
        }
    }

    fun togglePrivateMode() {
        val willBePrivate = !_uiState.value.isPrivateMode
        val current = _uiState.value.activeProfile
        val currentUrls = _uiState.value.tabs.map { it.url }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showMoreMenu = false) }

            if (willBePrivate) {
                // Switching INTO Private Mode: Save profile cookies and wipe active cookies
                cookieVault.saveCurrentProfileCookies(current?.id ?: "", currentUrls)
                withContext(Dispatchers.Main) {
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.removeAllCookies(null)
                    cookieManager.removeSessionCookies(null)
                    cookieManager.flush()
                }
            } else {
                // Leaving Private Mode: Wipe private cookies completely and restore profile cookies
                cookieVault.switchProfileSession(
                    oldProfileId = null,
                    newProfileId = current?.id ?: "personal",
                    isPrivate = false,
                    currentUrls = emptyList()
                )
            }

            _uiState.update {
                it.copy(
                    isPrivateMode = willBePrivate,
                    isLoading = false,
                    messageSnackbar = if (willBePrivate) "Entered Incognito Mode" else "Exited Incognito Mode"
                )
            }

            current?.let {
                loadProfileData(it.id, willBePrivate)
            }
        }
    }

    fun selectTab(tab: TabEntity) {
        viewModelScope.launch {
            val updated = tab.copy(lastAccessedAt = System.currentTimeMillis())
            repository.updateTab(updated)
            _uiState.update {
                it.copy(
                    activeTab = updated,
                    urlInput = updated.url,
                    isDesktopSite = updated.isDesktopSite,
                    currentScreen = ScreenDestination.BROWSER_MAIN
                )
            }
            if (updated.url != "prism://home") {
                onWebLoadUrl?.invoke(updated.url)
            }
            checkCurrentBookmarkStatus()
        }
    }

    fun createNewTab(url: String = "prism://home", isPrivate: Boolean = _uiState.value.isPrivateMode) {
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        viewModelScope.launch {
            val tab = repository.createTab(profileId, url, isPrivate)
            selectTab(tab)
        }
    }

    fun closeTab(tab: TabEntity) {
        viewModelScope.launch {
            repository.closeTab(tab)
            val remaining = _uiState.value.tabs.filter { it.id != tab.id }
            if (remaining.isNotEmpty()) {
                selectTab(remaining.first())
            } else {
                createNewTab(isPrivate = _uiState.value.isPrivateMode)
            }
        }
    }

    fun closeAllTabs() {
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        val isPrivate = _uiState.value.isPrivateMode
        viewModelScope.launch {
            repository.closeAllTabs(profileId, isPrivate)
            createNewTab(isPrivate = isPrivate)
            _uiState.update { it.copy(messageSnackbar = "All tabs closed") }
        }
    }

    fun duplicateTab(tab: TabEntity) {
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        viewModelScope.launch {
            val dup = repository.createTab(profileId, tab.url, tab.isPrivate)
            selectTab(dup)
        }
    }

    fun submitUrl(input: String) {
        val engine = SearchEngine.fromName(_uiState.value.activeProfile?.searchEngine ?: "Google")
        val resolved = UrlUtils.formatInputToUrl(input, engine)

        _uiState.update {
            it.copy(
                urlInput = resolved,
                isEditingUrl = false,
                currentScreen = ScreenDestination.BROWSER_MAIN
            )
        }

        val tab = _uiState.value.activeTab
        if (tab != null) {
            viewModelScope.launch {
                val updated = tab.copy(url = resolved, title = UrlUtils.getDisplayHost(resolved))
                repository.updateTab(updated)
                _uiState.update { it.copy(activeTab = updated) }
            }
        }

        cookieVault.recordDomain(resolved)
        onWebLoadUrl?.invoke(resolved)
        checkCurrentBookmarkStatus()
    }

    fun onPageStarted(url: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingProgress = 15,
                urlInput = url
            )
        }
        cookieVault.recordDomain(url)
    }

    fun onPageFinished(url: String, title: String?, canBack: Boolean, canForward: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = false,
                loadingProgress = 100,
                canGoBack = canBack,
                canGoForward = canForward,
                urlInput = url
            )
        }

        val active = _uiState.value.activeTab
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        val pageTitle = title ?: UrlUtils.getDisplayHost(url)

        if (active != null) {
            viewModelScope.launch {
                val updated = active.copy(url = url, title = pageTitle)
                repository.updateTab(updated)
                _uiState.update { it.copy(activeTab = updated) }

                // Save to history only if NOT in private mode
                if (!_uiState.value.isPrivateMode && url != "prism://home" && url != "about:blank") {
                    repository.recordHistory(profileId, pageTitle, url)
                }
            }
        }
        checkCurrentBookmarkStatus()
    }

    fun onProgressChanged(progress: Int) {
        _uiState.update { it.copy(loadingProgress = progress, isLoading = progress < 100) }
    }

    fun toggleDesktopSite() {
        val newDesktop = !_uiState.value.isDesktopSite
        _uiState.update { it.copy(isDesktopSite = newDesktop, showMoreMenu = false) }
        val tab = _uiState.value.activeTab
        if (tab != null) {
            viewModelScope.launch {
                repository.updateTab(tab.copy(isDesktopSite = newDesktop))
            }
        }
        onWebReload?.invoke()
    }

    fun toggleBookmark() {
        val tab = _uiState.value.activeTab ?: return
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        val currentBookmarked = _uiState.value.isBookmarked

        viewModelScope.launch {
            if (currentBookmarked) {
                val found = _uiState.value.bookmarks.find { it.url == tab.url }
                if (found != null) {
                    repository.removeBookmark(found)
                    _uiState.update { it.copy(isBookmarked = false, messageSnackbar = "Bookmark removed") }
                }
            } else {
                repository.addBookmark(profileId, tab.title, tab.url)
                _uiState.update { it.copy(isBookmarked = true, messageSnackbar = "Bookmark saved") }
            }
        }
    }

    private fun checkCurrentBookmarkStatus() {
        val url = _uiState.value.activeTab?.url ?: return
        val isBm = _uiState.value.bookmarks.any { it.url == url }
        _uiState.update { it.copy(isBookmarked = isBm) }
    }

    fun addBookmarkManually(profileId: String, title: String, url: String) {
        viewModelScope.launch {
            repository.addBookmark(profileId, title, url)
            checkCurrentBookmarkStatus()
            _uiState.update { it.copy(messageSnackbar = "Bookmark added") }
        }
    }

    fun clearProfileData(profileId: String) {
        viewModelScope.launch {
            repository.clearProfileData(profileId)
            _uiState.update { it.copy(messageSnackbar = "Profile data cleared") }
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
            checkCurrentBookmarkStatus()
        }
    }

    fun deleteHistoryEntry(history: HistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(history.id)
        }
    }

    fun clearHistory() {
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        viewModelScope.launch {
            repository.clearHistory(profileId)
            _uiState.update { it.copy(messageSnackbar = "History cleared") }
        }
    }

    fun clearDownloads() {
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        viewModelScope.launch {
            repository.clearDownloads(profileId)
            _uiState.update { it.copy(messageSnackbar = "Downloads cleared") }
        }
    }

    fun clearCookiesAndStorage() {
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        viewModelScope.launch {
            repository.clearProfileData(profileId)
            withContext(Dispatchers.Main) {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
            _uiState.update { it.copy(messageSnackbar = "Cookies and browsing storage cleared") }
        }
    }

    fun addCustomShortcut(title: String, url: String, emoji: String) {
        val profileId = _uiState.value.activeProfile?.id ?: "personal"
        viewModelScope.launch {
            repository.addShortcut(profileId, title, url, emoji)
            _uiState.update { it.copy(messageSnackbar = "Shortcut added to home") }
        }
    }

    fun removeShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch {
            repository.removeShortcut(shortcut)
        }
    }

    fun createProfile(name: String, colorHex: String, icon: String, searchEngine: String, customHome: String) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val newProfile = ProfileEntity(
                id = id,
                name = name.ifBlank { "Profile" },
                colorHex = colorHex,
                avatarIcon = icon,
                searchEngine = searchEngine,
                customHomeUrl = customHome
            )
            repository.saveProfile(newProfile)
            _uiState.update { it.copy(currentScreen = ScreenDestination.BROWSER_MAIN, messageSnackbar = "Created profile $name") }
            switchProfile(id)
        }
    }

    fun updateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            _uiState.update {
                it.copy(
                    activeProfile = if (it.activeProfile?.id == profile.id) profile else it.activeProfile,
                    currentScreen = ScreenDestination.PROFILE_MANAGER,
                    messageSnackbar = "Profile updated"
                )
            }
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            val remaining = _uiState.value.profiles.filter { it.id != profile.id }
            val nextProfile = remaining.firstOrNull { it.isDefault } ?: remaining.firstOrNull()
            if (nextProfile != null) {
                switchProfile(nextProfile.id)
            }
            _uiState.update { it.copy(messageSnackbar = "Profile ${profile.name} deleted") }
        }
    }

    fun setEditingProfile(profile: ProfileEntity?) {
        _uiState.update { it.copy(editingProfile = profile) }
    }

    fun updateSettings(newSettings: BrowserSettings) {
        _uiState.update { it.copy(settings = newSettings, messageSnackbar = "Settings saved") }
    }

    fun setEditingUrl(editing: Boolean) {
        _uiState.update {
            it.copy(
                isEditingUrl = editing,
                urlInput = if (editing) {
                    if (it.activeTab?.url == "prism://home") "" else it.activeTab?.url ?: ""
                } else it.urlInput
            )
        }
        if (editing) {
            updateSearchSuggestions(_uiState.value.urlInput)
        }
    }

    fun onUrlInputChanged(newText: String) {
        _uiState.update { it.copy(urlInput = newText) }
        updateSearchSuggestions(newText)
    }

    private fun updateSearchSuggestions(query: String) {
        if (query.isBlank()) {
            val historySuggestions = _uiState.value.history.take(5).map {
                SearchSuggestion(it.title, it.url, isHistory = true)
            }
            _uiState.update { it.copy(searchSuggestions = historySuggestions) }
            return
        }

        val q = query.lowercase()
        val historyMatches = _uiState.value.history
            .filter { it.title.lowercase().contains(q) || it.url.lowercase().contains(q) }
            .take(3)
            .map { SearchSuggestion(it.title, it.url, isHistory = true) }

        val bookmarkMatches = _uiState.value.bookmarks
            .filter { it.title.lowercase().contains(q) || it.url.lowercase().contains(q) }
            .take(3)
            .map { SearchSuggestion(it.title, it.url, isBookmark = true) }

        val webSearches = listOf(
            SearchSuggestion("Search \"$query\"", query, isHistory = false),
            SearchSuggestion("$query wiki", "https://en.wikipedia.org/wiki/Special:Search?search=$query"),
            SearchSuggestion("$query reddit", "https://www.reddit.com/search/?q=$query")
        )

        val combined = (historyMatches + bookmarkMatches + webSearches).distinctBy { it.url }
        _uiState.update { it.copy(searchSuggestions = combined) }
    }

    fun toggleProfileSwitcher(show: Boolean) {
        _uiState.update { it.copy(showProfileSwitcher = show) }
    }

    fun toggleMoreMenu(show: Boolean) {
        _uiState.update { it.copy(showMoreMenu = show) }
    }

    fun toggleFindInPage(active: Boolean) {
        _uiState.update {
            it.copy(
                findInPage = it.findInPage.copy(active = active, query = if (!active) "" else it.findInPage.query),
                showMoreMenu = false
            )
        }
        if (!active) {
            onWebClearMatches?.invoke()
        }
    }

    fun setFindQuery(query: String) {
        _uiState.update { it.copy(findInPage = it.findInPage.copy(query = query)) }
        onWebFindText?.invoke(query)
    }

    fun findNext(forward: Boolean) {
        onWebFindNext?.invoke(forward)
    }

    fun updateFindMatches(currentIndex: Int, totalMatches: Int) {
        _uiState.update {
            it.copy(findInPage = it.findInPage.copy(currentIndex = currentIndex, totalMatches = totalMatches))
        }
    }

    fun toggleReaderMode() {
        val willBeReader = !_uiState.value.isReaderMode
        _uiState.update { it.copy(isReaderMode = willBeReader, showMoreMenu = false) }
        if (willBeReader) {
            onWebExtractReader?.invoke()
        }
    }

    fun setReaderContent(content: String) {
        _uiState.update { it.copy(readerContent = content) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(messageSnackbar = null) }
    }
}
