package com.rld.justlisten.media

object PlaybackQueueNavigator {

    fun getNextIndex(
        currentIndex: Int,
        playlistSize: Int,
        shuffledIndices: List<Int>,
        isShuffleEnabled: Boolean,
        repeatMode: RepeatMode
    ): Int {
        if (playlistSize <= 0) return -1
        
        return if (isShuffleEnabled) {
            val currentShuffledPos = shuffledIndices.indexOf(currentIndex)
            if (currentShuffledPos != -1 && currentShuffledPos < shuffledIndices.size - 1) {
                shuffledIndices[currentShuffledPos + 1]
            } else if (repeatMode == RepeatMode.ALL) {
                shuffledIndices.firstOrNull() ?: -1
            } else {
                -1
            }
        } else {
            if (currentIndex < playlistSize - 1) {
                currentIndex + 1
            } else if (repeatMode == RepeatMode.ALL) {
                0
            } else {
                -1
            }
        }
    }

    fun getPreviousIndex(
        currentIndex: Int,
        playlistSize: Int,
        shuffledIndices: List<Int>,
        isShuffleEnabled: Boolean,
        repeatMode: RepeatMode
    ): Int {
        if (playlistSize <= 0) return -1
        
        return if (isShuffleEnabled) {
            val currentShuffledPos = shuffledIndices.indexOf(currentIndex)
            if (currentShuffledPos > 0) {
                shuffledIndices[currentShuffledPos - 1]
            } else if (repeatMode == RepeatMode.ALL) {
                shuffledIndices.lastOrNull() ?: -1
            } else {
                -1
            }
        } else {
            if (currentIndex > 0) {
                currentIndex - 1
            } else if (repeatMode == RepeatMode.ALL) {
                playlistSize - 1
            } else {
                -1
            }
        }
    }
}
