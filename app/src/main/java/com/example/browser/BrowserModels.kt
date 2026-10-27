package com.example.browser

import androidx.compose.ui.graphics.Color

enum class SearchEngine(val displayName: String, val searchUrl: String, val suggestUrl: String) {
    GOOGLE("Google", "https://www.google.com/search?q=", "https://suggestqueries.google.com/complete/search?client=chrome&q="),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com/ac/?q="),
    BING("Bing", "https://www.bing.com/search?q=", "https://api.bing.com/osjson.aspx?query="),
    YAHOO("Yahoo", "https://search.yahoo.com/search?p=", "");

    companion object {
        fun fromName(name: String): SearchEngine {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: GOOGLE
        }
    }
}

enum class SecurityStatus {
    SECURE,
    NOT_SECURE,
    INTERNAL
}

data class ProfileColorPreset(
    val hex: String,
    val name: String,
    val color: Color
)

object ProfilePresets {
    val COLORS = listOf(
        ProfileColorPreset("#3B82F6", "Sapphire Blue", Color(0xFF3B82F6)),
        ProfileColorPreset("#10B981", "Emerald Green", Color(0xFF10B981)),
        ProfileColorPreset("#8B5CF6", "Amethyst Purple", Color(0xFF8B5CF6)),
        ProfileColorPreset("#F43F5E", "Ruby Rose", Color(0xFFF43F5E)),
        ProfileColorPreset("#F59E0B", "Sunset Amber", Color(0xFFF59E0B)),
        ProfileColorPreset("#06B6D4", "Cyan Wave", Color(0xFF06B6D4)),
        ProfileColorPreset("#64748B", "Slate Stealth", Color(0xFF64748B))
    )

    val ICONS = listOf(
        "person" to "Personal",
        "work" to "Work",
        "school" to "Study",
        "shield" to "Private / Secure",
        "rocket" to "Fast / Tech",
        "palette" to "Creative",
        "code" to "Development",
        "star" to "VIP"
    )
}

data class SearchSuggestion(
    val title: String,
    val url: String,
    val isHistory: Boolean = false,
    val isBookmark: Boolean = false
)

data class FindInPageState(
    val active: Boolean = false,
    val query: String = "",
    val currentIndex: Int = 0,
    val totalMatches: Int = 0
)

enum class ScreenDestination {
    BROWSER_MAIN,
    TAB_SWITCHER,
    PROFILE_MANAGER,
    CREATE_PROFILE,
    EDIT_PROFILE,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    SETTINGS,
    ABOUT
}
