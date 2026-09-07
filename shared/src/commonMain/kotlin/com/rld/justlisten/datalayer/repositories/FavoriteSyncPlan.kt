package com.rld.justlisten.datalayer.repositories

internal data class FavoriteSyncPlan(
    val desiredFavorites: Set<String>,
    val addLocally: Set<String>,
    val removeLocally: Set<String>,
    val favoriteRemotely: Set<String>,
    val unfavoriteRemotely: Set<String>,
)

internal fun favoriteRemoteIdsNeedingMetadata(
    remoteTrackIds: Set<String>,
    cachedPublicIdsByRemoteId: Map<String, String>,
    locallyKnownPublicIds: Set<String>,
): Set<String> = remoteTrackIds.filterTo(mutableSetOf()) { remoteId ->
    cachedPublicIdsByRemoteId[remoteId]?.let(locallyKnownPublicIds::contains) != true
}

/**
 * Produces a deterministic three-way merge of local and remote favorite sets.
 * A null baseline denotes the first sync, where preserving both libraries is
 * safer than guessing which side contains a deletion. Explicit pending intent
 * always wins over both snapshots.
 */
internal fun buildFavoriteSyncPlan(
    localFavorites: Set<String>,
    remoteFavorites: Set<String>,
    baselineFavorites: Set<String>?,
    pendingFavorites: Map<String, Boolean>,
    remoteAuthoritative: Boolean = false,
): FavoriteSyncPlan {
    val allTrackIds = buildSet {
        addAll(localFavorites)
        addAll(remoteFavorites)
        baselineFavorites?.let(::addAll)
        addAll(pendingFavorites.keys)
    }

    val desired = allTrackIds.filterTo(mutableSetOf()) { trackId ->
        pendingFavorites[trackId] ?: when {
            remoteAuthoritative -> trackId in remoteFavorites
            baselineFavorites == null ->
                trackId in localFavorites || trackId in remoteFavorites
            else -> {
                val wasFavorite = trackId in baselineFavorites
                val isLocalFavorite = trackId in localFavorites
                val isRemoteFavorite = trackId in remoteFavorites
                when {
                    isLocalFavorite == isRemoteFavorite -> isLocalFavorite
                    isLocalFavorite != wasFavorite && isRemoteFavorite == wasFavorite ->
                        isLocalFavorite
                    isRemoteFavorite != wasFavorite && isLocalFavorite == wasFavorite ->
                        isRemoteFavorite
                    else -> isLocalFavorite
                }
            }
        }
    }

    return FavoriteSyncPlan(
        desiredFavorites = desired,
        addLocally = desired - localFavorites,
        removeLocally = localFavorites - desired,
        favoriteRemotely = desired - remoteFavorites,
        unfavoriteRemotely = remoteFavorites - desired,
    )
}
