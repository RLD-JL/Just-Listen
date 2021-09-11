package com.example.audius.android.ui.test

import com.example.audius.android.R


object AlbumsDataProvider {

    val listOfSpotifyHomeLanes = listOf(
        "Continue listening",
        "Popular Playlists",
        "Top Charts",
        "Recommended for today",
        "Bollywood",
        "Acoustic only"
    )

    val album = Album(
        id = 0,
        artist = "Adele",
        song = "Someone like you",
        descriptions = "Album by Adele-2016",
        imageId = R.drawable.camelia
    )

    val albums = mutableListOf(
        Album(
            id = 1,
            artist = "Ed Sheeran",
            song = "Perfect",
            descriptions = "Album by Ed Sheeran-2016",
            imageId = R.drawable.edsheeran,
            genre = "Pop"
        ),
        Album(
            id = 2,
            artist = "Camelia Cabello",
            song = "Havana",
            descriptions = "Album by Camelia Cabello-2016",
            imageId = R.drawable.camelia,
            genre = "R&B"
        ),
        Album(
            id = 3,
            artist = "BlackPink",
            song = "Kill this love",
            descriptions = "Album by BlackPink-2016",
            imageId = R.drawable.bp,
            genre = "K-pop"
        ),
        Album(
            id = 4,
            artist = "Ed Sheeran",
            song = "Photograph",
            descriptions = "Album by Ed Sheeran-2016",
            imageId = R.drawable.ed2,
            genre = "Acoustic"
        ),
        Album(
            id = 13,
            artist = "Eminem",
            song = "The Eminem Show",
            descriptions = "Album by Eminem-2019",
            imageId = R.drawable.eminem,
            genre = "Rap"
        ),
        Album(
            id = 14,
            artist = "Eminem",
            song = "The Eminem Show",
            descriptions = "Album by Eminem-2019",
            imageId = R.drawable.eminem,
            genre = "Rap"
        ),
        Album(
            id = 15,
            artist = "Eminem",
            song = "The Eminem Show",
            descriptions = "Album by Eminem-2019",
            imageId = R.drawable.eminem,
            genre = "Rap"
        ),
        Album(
            id = 16,
            artist = "Eminem",
            song = "The Eminem Show",
            descriptions = "Album by Eminem-2019",
            imageId = R.drawable.eminem,
            genre = "Rap"
        ),
    )
}