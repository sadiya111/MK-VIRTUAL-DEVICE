package com.example.browser

import android.webkit.CookieManager
import android.webkit.WebStorage
import com.example.data.local.dao.BrowserDao
import com.example.data.local.entity.ProfileCookieEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

class CookieVaultManager(private val browserDao: BrowserDao) {

    private val activeDomains = mutableSetOf<String>()

    fun recordDomain(url: String) {
        try {
            val uri = URI(url)
            val host = uri.host
            if (!host.isNullOrBlank()) {
                activeDomains.add(host)
            }
        } catch (_: Exception) {
            // Ignore malformed URL
        }
    }

    suspend fun saveCurrentProfileCookies(profileId: String, currentUrls: List<String>) {
        if (profileId.isEmpty()) return
        withContext(Dispatchers.IO) {
            val cookieManager = CookieManager.getInstance()
            val domainsToInspect = HashSet(activeDomains)
            currentUrls.forEach { url ->
                try {
                    val host = URI(url).host
                    if (!host.isNullOrBlank()) domainsToInspect.add(host)
                } catch (_: Exception) {}
            }

            for (domain in domainsToInspect) {
                try {
                    val httpsUrl = "https://$domain"
                    val cookieStr = cookieManager.getCookie(httpsUrl)
                    if (!cookieStr.isNullOrBlank()) {
                        browserDao.saveCookie(
                            ProfileCookieEntity(
                                id = "${profileId}_$domain",
                                profileId = profileId,
                                domain = domain,
                                cookieString = cookieStr
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun switchProfileSession(oldProfileId: String?, newProfileId: String, isPrivate: Boolean, currentUrls: List<String>) {
        withContext(Dispatchers.IO) {
            val cookieManager = CookieManager.getInstance()

            // 1. Save old profile cookies if not private
            if (!oldProfileId.isNullOrEmpty()) {
                saveCurrentProfileCookies(oldProfileId, currentUrls)
            }

            // 2. Wipe current active cookie jar and local web storage
            withContext(Dispatchers.Main) {
                cookieManager.removeAllCookies(null)
                cookieManager.removeSessionCookies(null)
                cookieManager.flush()
                WebStorage.getInstance().deleteAllData()
            }

            activeDomains.clear()

            // 3. If new profile is NOT private, restore its isolated cookies
            if (!isPrivate) {
                val savedCookies = browserDao.getCookiesForProfile(newProfileId)
                withContext(Dispatchers.Main) {
                    for (cookie in savedCookies) {
                        try {
                            val domainUrl = "https://${cookie.domain}"
                            // Cookies might contain multiple key=value pairs separated by semicolon
                            val parts = cookie.cookieString.split(";")
                            for (part in parts) {
                                val trimmed = part.trim()
                                if (trimmed.isNotEmpty()) {
                                    cookieManager.setCookie(domainUrl, trimmed)
                                }
                            }
                            activeDomains.add(cookie.domain)
                        } catch (_: Exception) {}
                    }
                    cookieManager.flush()
                }
            }
        }
    }

    suspend fun clearProfileCookies(profileId: String) {
        withContext(Dispatchers.IO) {
            browserDao.clearCookiesForProfile(profileId)
        }
    }
}
