package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.browser.ScreenDestination
import com.example.ui.BrowserState
import com.example.ui.BrowserViewModel
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.BrowserMoreMenu
import com.example.ui.components.BrowserTopBar
import com.example.ui.components.BrowserWebView

@Composable
fun BrowserMainScreen(
    viewModel: BrowserViewModel,
    state: BrowserState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Display snackbar messages
    LaunchedEffect(state.messageSnackbar) {
        state.messageSnackbar?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    // Android Hardware Back button handling
    BackHandler(enabled = true) {
        when {
            state.isEditingUrl -> viewModel.setEditingUrl(false)
            state.findInPage.active -> viewModel.toggleFindInPage(false)
            state.isReaderMode -> viewModel.toggleReaderMode()
            state.showMoreMenu -> viewModel.toggleMoreMenu(false)
            state.showProfileSwitcher -> viewModel.toggleProfileSwitcher(false)
            state.canGoBack -> viewModel.onWebGoBack?.invoke()
            state.activeTab?.url != "prism://home" -> viewModel.submitUrl("prism://home")
            else -> {
                // If on homepage and cannot go back, let standard back exit
                if (!viewModel.navigateBack()) {
                    (context as? android.app.Activity)?.finish()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BrowserTopBar(
                activeProfile = state.activeProfile,
                isPrivate = state.isPrivateMode,
                currentUrl = state.urlInput,
                openTabsCount = state.tabs.size,
                isLoading = state.isLoading,
                loadingProgress = state.loadingProgress,
                findInPage = state.findInPage,
                onProfileClick = { viewModel.toggleProfileSwitcher(true) },
                onUrlClick = { viewModel.setEditingUrl(true) },
                onReloadClick = { viewModel.onWebReload?.invoke() },
                onTabsClick = { viewModel.navigateTo(ScreenDestination.TAB_SWITCHER) },
                onMoreClick = { viewModel.toggleMoreMenu(true) },
                onFindQueryChange = { viewModel.setFindQuery(it) },
                onFindNext = { viewModel.findNext(it) },
                onCloseFind = { viewModel.toggleFindInPage(false) }
            )
        },
        bottomBar = {
            BrowserBottomBar(
                canGoBack = state.canGoBack,
                canGoForward = state.canGoForward,
                onBack = { viewModel.onWebGoBack?.invoke() },
                onForward = { viewModel.onWebGoForward?.invoke() },
                onHome = { viewModel.submitUrl("prism://home") },
                onTabs = { viewModel.navigateTo(ScreenDestination.TAB_SWITCHER) },
                onMenu = { viewModel.toggleMoreMenu(true) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val currentUrl = state.activeTab?.url ?: "prism://home"

            if (currentUrl == "prism://home" || currentUrl.isBlank() || currentUrl == "about:blank") {
                HomepageView(
                    activeProfile = state.activeProfile,
                    isPrivate = state.isPrivateMode,
                    shortcuts = state.shortcuts,
                    bookmarks = state.bookmarks,
                    onSearchClick = { viewModel.setEditingUrl(true) },
                    onUrlSelect = { viewModel.submitUrl(it) },
                    onAddShortcut = { title, url, emoji ->
                        viewModel.addCustomShortcut(title, url, emoji)
                    }
                )
            } else {
                BrowserWebView(
                    viewModel = viewModel,
                    targetUrl = currentUrl,
                    isDesktopSite = state.isDesktopSite,
                    browserSettings = state.settings
                )
            }

            // Reader Mode Overlay
            if (state.isReaderMode) {
                ReaderModeView(
                    title = state.activeTab?.title ?: "Article",
                    content = state.readerContent,
                    onClose = { viewModel.toggleReaderMode() }
                )
            }

            // Omnibox Fullscreen Search Overlay
            if (state.isEditingUrl) {
                OmniboxOverlay(
                    initialText = if (state.urlInput == "prism://home") "" else state.urlInput,
                    activeProfile = state.activeProfile,
                    isPrivate = state.isPrivateMode,
                    suggestions = state.searchSuggestions,
                    onTextChange = { viewModel.onUrlInputChanged(it) },
                    onSubmit = { viewModel.submitUrl(it) },
                    onDismiss = { viewModel.setEditingUrl(false) }
                )
            }

            // Profile Switcher Bottom Sheet
            if (state.showProfileSwitcher) {
                ProfileSwitchSheet(
                    profiles = state.profiles,
                    activeProfile = state.activeProfile,
                    isPrivate = state.isPrivateMode,
                    onSelectProfile = { viewModel.switchProfile(it) },
                    onTogglePrivate = { viewModel.togglePrivateMode() },
                    onAddProfile = {
                        viewModel.toggleProfileSwitcher(false)
                        viewModel.setEditingProfile(null)
                        viewModel.navigateTo(ScreenDestination.CREATE_PROFILE)
                    },
                    onManageProfiles = {
                        viewModel.toggleProfileSwitcher(false)
                        viewModel.navigateTo(ScreenDestination.PROFILE_MANAGER)
                    },
                    onDismiss = { viewModel.toggleProfileSwitcher(false) }
                )
            }

            // Three-Dot More Menu Sheet
            if (state.showMoreMenu) {
                BrowserMoreMenu(
                    isBookmarked = state.isBookmarked,
                    isDesktopSite = state.isDesktopSite,
                    isReaderMode = state.isReaderMode,
                    onNewTab = {
                        viewModel.createNewTab(isPrivate = state.isPrivateMode)
                        viewModel.toggleMoreMenu(false)
                    },
                    onNewPrivateTab = {
                        if (!state.isPrivateMode) viewModel.togglePrivateMode()
                        viewModel.createNewTab(isPrivate = true)
                        viewModel.toggleMoreMenu(false)
                    },
                    onToggleBookmark = {
                        viewModel.toggleBookmark()
                        viewModel.toggleMoreMenu(false)
                    },
                    onOpenBookmarks = { viewModel.navigateTo(ScreenDestination.BOOKMARKS) },
                    onOpenHistory = { viewModel.navigateTo(ScreenDestination.HISTORY) },
                    onOpenDownloads = { viewModel.navigateTo(ScreenDestination.DOWNLOADS) },
                    onSwitchProfile = {
                        viewModel.toggleMoreMenu(false)
                        viewModel.toggleProfileSwitcher(true)
                    },
                    onFindInPage = { viewModel.toggleFindInPage(true) },
                    onToggleReader = { viewModel.toggleReaderMode() },
                    onToggleDesktop = { viewModel.toggleDesktopSite() },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, state.activeTab?.title ?: "Web Page")
                            putExtra(Intent.EXTRA_TEXT, state.activeTab?.url ?: "")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Page"))
                        viewModel.toggleMoreMenu(false)
                    },
                    onOpenSettings = { viewModel.navigateTo(ScreenDestination.SETTINGS) },
                    onOpenAbout = { viewModel.navigateTo(ScreenDestination.ABOUT) },
                    onDismiss = { viewModel.toggleMoreMenu(false) }
                )
            }
        }
    }
}
