package com.example.browser

import android.util.Patterns
import java.net.URI

object UrlUtils {
    fun formatInputToUrl(input: String, searchEngine: SearchEngine): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return "prism://home"

        if (trimmed.equals("prism://home", ignoreCase = true) ||
            trimmed.equals("about:blank", ignoreCase = true)) {
            return trimmed
        }

        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("file://", ignoreCase = true)) {
            return trimmed
        }

        // Check if looks like a domain name (has dot, no spaces)
        val hasSpaces = trimmed.contains(" ")
        val hasDot = trimmed.contains(".")
        val isWebUrl = Patterns.WEB_URL.matcher(trimmed).matches()

        if (!hasSpaces && (hasDot || isWebUrl || trimmed.contains(":"))) {
            return "https://$trimmed"
        }

        // Fallback to Search Engine query
        val encodedQuery = java.net.URLEncoder.encode(trimmed, "UTF-8")
        return searchEngine.searchUrl + encodedQuery
    }

    fun getDisplayHost(url: String): String {
        if (url.startsWith("prism://home") || url.isBlank() || url == "about:blank") {
            return "Search or type URL"
        }
        return try {
            val uri = URI(url)
            val host = uri.host ?: url
            host.removePrefix("www.")
        } catch (_: Exception) {
            url
        }
    }

    fun getSecurityStatus(url: String): SecurityStatus {
        return when {
            url.startsWith("https://", ignoreCase = true) -> SecurityStatus.SECURE
            url.startsWith("http://", ignoreCase = true) -> SecurityStatus.NOT_SECURE
            else -> SecurityStatus.INTERNAL
        }
    }
}
