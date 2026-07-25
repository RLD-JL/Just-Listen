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
    fun parsesUniversalCommentLink() {
        assertEquals(
            JustListenDeepLink(
                "comments/share",
                mapOf("trackId" to "XgRaaJy", "commentId" to "abc123"),
            ),
            parseJustListenDeepLink("https://justlisten.cloud/comments/XgRaaJy/abc123"),
        )
    }

    @Test
    fun parsesUniversalTrackCommentsLinkWithoutSpecificComment() {
        assertEquals(
            JustListenDeepLink("comments/share", mapOf("trackId" to "XgRaaJy")),
            parseJustListenDeepLink("https://justlisten.cloud/comments/XgRaaJy"),
        )
    }

    @Test
    fun keepsLegacyCustomCommentLinksWorking() {
        assertEquals(
            JustListenDeepLink(
                "comments/share",
                mapOf("trackId" to "XgRaaJy", "commentId" to "abc123"),
            ),
            parseJustListenDeepLink(
                "justlisten://comments/share?trackId=XgRaaJy&commentId=abc123",
            ),
        )
    }

    @Test
    fun createsPublicCommentShareLinks() {
        assertEquals(
            "https://justlisten.cloud/comments/XgRaaJy/abc123",
            commentsShareUrl("XgRaaJy", "abc123"),
        )
        assertEquals(
            "https://justlisten.cloud/comments/XgRaaJy",
            commentsShareUrl("XgRaaJy"),
        )
    }

    @Test
    fun rejectsMalformedUniversalCommentLink() {
        assertNull(
            parseJustListenDeepLink("https://justlisten.cloud/comments/track/comment/extra"),
        )
    }

    @Test
    fun rejectsOtherWebDomains() {
        assertNull(parseJustListenDeepLink("https://example.com/track/XgRaaJy"))
    }
}
