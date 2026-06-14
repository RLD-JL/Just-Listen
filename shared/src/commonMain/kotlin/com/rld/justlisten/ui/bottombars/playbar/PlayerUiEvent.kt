package com.rld.justlisten.ui.bottombars.playbar

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp

data class PlayerLayoutInfo(
    val bottomPadding: Dp,
    val currentFractionProvider: () -> Float,
    val isExtended: Boolean
)

sealed interface PlayerUiEvent {
    data object Collapse : PlayerUiEvent
    data object Expand : PlayerUiEvent
    data object OpenAddPlaylist : PlayerUiEvent
    data class OpenComments(val trackId: String) : PlayerUiEvent
    data object CloseSheet : PlayerUiEvent
    data class PainterLoaded(val painter: Painter) : PlayerUiEvent
    data class DominantColorExtracted(val colorInt: Int) : PlayerUiEvent
    data class NavigateToArtist(val artistId: String, val artistName: String) : PlayerUiEvent
}
