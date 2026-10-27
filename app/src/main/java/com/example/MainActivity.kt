package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.browser.ScreenDestination
import com.example.ui.BrowserViewModel
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.BrowserMainScreen
import com.example.ui.screens.BrowserSettingsScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileEditScreen
import com.example.ui.screens.ProfileManagementScreen
import com.example.ui.screens.TabSwitcherScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            val isDarkTheme = when (uiState.settings.themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme() || uiState.isPrivateMode
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: BrowserViewModel) {
    val state by viewModel.uiState.collectAsState()

    when (state.currentScreen) {
        ScreenDestination.BROWSER_MAIN -> {
            BrowserMainScreen(viewModel = viewModel, state = state)
        }

        ScreenDestination.TAB_SWITCHER -> {
            BackHandler { viewModel.navigateBack() }
            TabSwitcherScreen(
                activeProfile = state.activeProfile,
                isPrivate = state.isPrivateMode,
                tabs = state.tabs,
                activeTab = state.activeTab,
                onSelectTab = { tab -> viewModel.selectTab(tab) },
                onCloseTab = { tab -> viewModel.closeTab(tab) },
                onCloseAllTabs = { viewModel.closeAllTabs() },
                onNewTab = { isPriv -> viewModel.createNewTab(isPrivate = isPriv) },
                onDuplicateTab = { tab -> viewModel.duplicateTab(tab) },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenDestination.PROFILE_MANAGER -> {
            BackHandler { viewModel.navigateBack() }
            ProfileManagementScreen(
                profiles = state.profiles,
                activeProfile = state.activeProfile,
                onAddProfileClick = {
                    viewModel.setEditingProfile(null)
                    viewModel.navigateTo(ScreenDestination.CREATE_PROFILE)
                },
                onEditProfileClick = { profile ->
                    viewModel.setEditingProfile(profile)
                    viewModel.navigateTo(ScreenDestination.EDIT_PROFILE)
                },
                onSetDefaultProfile = { profile ->
                    // Set default profile
                    val updated = profile.copy(isDefault = true)
                    viewModel.updateProfile(updated)
                },
                onDeleteProfile = { profile ->
                    viewModel.deleteProfile(profile)
                },
                onClearProfileData = { profile ->
                    viewModel.clearProfileData(profile.id)
                },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenDestination.CREATE_PROFILE -> {
            BackHandler { viewModel.navigateBack() }
            ProfileEditScreen(
                profileToEdit = null,
                onSaveProfile = { name, colorHex, icon, searchEngine, customHome ->
                    viewModel.createProfile(name, colorHex, icon, searchEngine, customHome)
                },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenDestination.EDIT_PROFILE -> {
            BackHandler { viewModel.navigateBack() }
            ProfileEditScreen(
                profileToEdit = state.editingProfile,
                onSaveProfile = { name, colorHex, icon, searchEngine, customHome ->
                    state.editingProfile?.let { existing ->
                        val updated = existing.copy(
                            name = name,
                            colorHex = colorHex,
                            avatarIcon = icon,
                            searchEngine = searchEngine,
                            customHomeUrl = customHome
                        )
                        viewModel.updateProfile(updated)
                    }
                },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenDestination.BOOKMARKS -> {
            BackHandler { viewModel.navigateBack() }
            BookmarksScreen(
                activeProfile = state.activeProfile,
                bookmarks = state.bookmarks,
                onSelectBookmark = { url -> viewModel.submitUrl(url) },
                onAddBookmark = { title, url ->
                    val profileId = state.activeProfile?.id ?: "personal"
                    viewModel.addBookmarkManually(profileId, title, url)
                },
                onDeleteBookmark = { bm -> viewModel.removeBookmark(bm) },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenDestination.HISTORY -> {
            BackHandler { viewModel.navigateBack() }
            HistoryScreen(
                activeProfile = state.activeProfile,
                history = state.history,
                onSelectHistory = { url -> viewModel.submitUrl(url) },
                onDeleteHistory = { item -> viewModel.deleteHistoryEntry(item) },
                onClearAllHistory = { viewModel.clearHistory() },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenDestination.DOWNLOADS -> {
            BackHandler { viewModel.navigateBack() }
            DownloadsScreen(
                activeProfile = state.activeProfile,
                downloads = state.downloads,
                onClearDownloads = { viewModel.clearDownloads() },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenDestination.SETTINGS -> {
            BackHandler { viewModel.navigateBack() }
            BrowserSettingsScreen(
                settings = state.settings,
                onUpdateSettings = { viewModel.updateSettings(it) },
                onClearDataClick = { viewModel.clearCookiesAndStorage() },
                onAboutClick = { viewModel.navigateTo(ScreenDestination.ABOUT) },
                onBack = { viewModel.navigateBack() }
            )
        }

        ScreenDestination.ABOUT -> {
            BackHandler { viewModel.navigateBack() }
            AboutScreen(onBack = { viewModel.navigateBack() })
        }
    }
}
