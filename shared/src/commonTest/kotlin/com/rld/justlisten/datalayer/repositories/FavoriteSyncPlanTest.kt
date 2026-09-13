package com.rld.justlisten.datalayer.repositories

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteSyncPlanTest {

    @Test
    fun firstSyncMergesDifferentSetsEvenWhenCountsMatch() {
        val plan = buildFavoriteSyncPlan(
            localFavorites = setOf("local-track"),
            remoteFavorites = setOf("remote-track"),
            baselineFavorites = null,
            pendingFavorites = emptyMap(),
        )

        assertEquals(setOf("local-track", "remote-track"), plan.desiredFavorites)
        assertEquals(setOf("remote-track"), plan.addLocally)
        assertEquals(setOf("local-track"), plan.favoriteRemotely)
    }

    @Test
    fun remoteDeletionPropagatesLocallyAfterBaselineExists() {
        val plan = buildFavoriteSyncPlan(
            localFavorites = setOf("track"),
            remoteFavorites = emptySet(),
            baselineFavorites = setOf("track"),
            pendingFavorites = emptyMap(),
        )

        assertTrue(plan.desiredFavorites.isEmpty())
        assertEquals(setOf("track"), plan.removeLocally)
        assertTrue(plan.favoriteRemotely.isEmpty())
    }

    @Test
    fun localDeletionPropagatesRemotelyAfterBaselineExists() {
        val plan = buildFavoriteSyncPlan(
            localFavorites = emptySet(),
            remoteFavorites = setOf("track"),
            baselineFavorites = setOf("track"),
            pendingFavorites = emptyMap(),
        )

        assertTrue(plan.desiredFavorites.isEmpty())
        assertEquals(setOf("track"), plan.unfavoriteRemotely)
        assertTrue(plan.addLocally.isEmpty())
    }

    @Test
    fun pendingUnfavoriteOverridesStaleRemoteFavorite() {
        val plan = buildFavoriteSyncPlan(
            localFavorites = emptySet(),
            remoteFavorites = setOf("track"),
            baselineFavorites = setOf("track"),
            pendingFavorites = mapOf("track" to false),
        )

        assertTrue(plan.desiredFavorites.isEmpty())
        assertEquals(setOf("track"), plan.unfavoriteRemotely)
        assertTrue(plan.addLocally.isEmpty())
    }

    @Test
    fun pendingFavoriteOverridesStaleRemoteDeletion() {
        val plan = buildFavoriteSyncPlan(
            localFavorites = setOf("track"),
            remoteFavorites = emptySet(),
            baselineFavorites = setOf("track"),
            pendingFavorites = mapOf("track" to true),
        )

        assertEquals(setOf("track"), plan.desiredFavorites)
        assertEquals(setOf("track"), plan.favoriteRemotely)
    }

    @Test
    fun accountSwitchUsesNewAccountsRemoteLibrary() {
        val plan = buildFavoriteSyncPlan(
            localFavorites = setOf("previous-account-track"),
            remoteFavorites = setOf("new-account-track"),
            baselineFavorites = null,
            pendingFavorites = emptyMap(),
            remoteAuthoritative = true,
        )

        assertEquals(setOf("new-account-track"), plan.desiredFavorites)
        assertEquals(setOf("previous-account-track"), plan.removeLocally)
        assertEquals(setOf("new-account-track"), plan.addLocally)
        assertTrue(plan.favoriteRemotely.isEmpty())
    }

    @Test
    fun cachedFavoriteIdentitiesAvoidRepeatedMetadataRequests() {
        val missing = favoriteRemoteIdsNeedingMetadata(
            remoteTrackIds = setOf("numeric-1", "numeric-2"),
            cachedPublicIdsByRemoteId = mapOf(
                "numeric-1" to "public-1",
                "numeric-2" to "public-2",
            ),
            locallyKnownPublicIds = setOf("public-1", "public-2"),
        )

        assertTrue(missing.isEmpty())
    }

    @Test
    fun missingLocalMetadataIsRefetchedEvenWhenIdentityIsCached() {
        val missing = favoriteRemoteIdsNeedingMetadata(
            remoteTrackIds = setOf("numeric-1", "numeric-2", "numeric-3"),
            cachedPublicIdsByRemoteId = mapOf(
                "numeric-1" to "public-1",
                "numeric-2" to "public-2",
            ),
            locallyKnownPublicIds = setOf("public-1"),
        )

        assertEquals(setOf("numeric-2", "numeric-3"), missing)
    }
}
