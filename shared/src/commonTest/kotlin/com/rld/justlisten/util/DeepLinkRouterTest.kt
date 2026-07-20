package com.rld.justlisten.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkRouterTest {
    @Test
    fun parsesUniversalTrackLink() {
        assertEquals(
            JustListenDeepLink("track/share", mapOf("id" to "XgRaaJy")),
            parseJustListenDeepLink("https://justlisten.cloud/track/XgRaaJy"),
        )
    }

    @Test
    fun keepsExistingCustomTrackLinksWorking() {
        assertEquals(
            JustListenDeepLink("track/share", mapOf("id" to "XgRaaJy")),
            parseJustListenDeepLink("justlisten://track/share?id=XgRaaJy"),
        )
    }

    @Test
    fun rejectsOtherWebDomains() {
        assertNull(parseJustListenDeepLink("https://example.com/track/XgRaaJy"))
    }
}
