package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.browser.SearchEngine
import com.example.browser.SecurityStatus
import com.example.browser.UrlUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Prism", appName)
    }

    @Test
    fun `url utils resolves plain search to engine query`() {
        val search = UrlUtils.formatInputToUrl("kotlin coroutines guide", SearchEngine.GOOGLE)
        assertTrue(search.startsWith("https://www.google.com/search?q="))
        assertTrue(search.contains("kotlin+coroutines+guide"))
    }

    @Test
    fun `url utils formats domain without scheme to https`() {
        val url = UrlUtils.formatInputToUrl("wikipedia.org", SearchEngine.GOOGLE)
        assertEquals("https://wikipedia.org", url)
    }

    @Test
    fun `url utils detects security status correctly`() {
        assertEquals(SecurityStatus.SECURE, UrlUtils.getSecurityStatus("https://github.com"))
        assertEquals(SecurityStatus.NOT_SECURE, UrlUtils.getSecurityStatus("http://example.com"))
        assertEquals(SecurityStatus.INTERNAL, UrlUtils.getSecurityStatus("prism://home"))
    }
}
