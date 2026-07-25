package com.rld.justlisten.viewmodel.comments

import com.rld.justlisten.datalayer.models.Comment
import com.rld.justlisten.datalayer.models.RelatedData
import com.rld.justlisten.datalayer.models.TrackCommentsResponse
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.CommentsRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse
import com.rld.justlisten.datalayer.webservices.apis.authcalls.UserProfileModel
import com.rld.justlisten.datalayer.webservices.apis.writecalls.FavoriteResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class CommentsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeCommentsRepository
    private lateinit var authRepository: FakeCommentsAuthRepository
    private lateinit var viewModel: CommentsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeCommentsRepository()
        authRepository = FakeCommentsAuthRepository()
        viewModel = CommentsViewModel(repository, authRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sharedTargetLoadsAdditionalPagesUntilFound() = runTest(dispatcher) {
        repository.pages["track" to 0] = TrackCommentsResponse(
            data = List(50) { comment("comment-$it") },
            related = RelatedData(),
        )
        repository.pages["track" to 50] = TrackCommentsResponse(
            data = listOf(comment("target")),
            related = RelatedData(),
        )

        viewModel.load(trackId = "track", targetCommentId = "target")
        advanceUntilIdle()

        assertEquals(51, viewModel.state.value.comments.size)
        assertEquals(listOf(0, 50), repository.requestedOffsets)
        assertNull(viewModel.state.value.targetCommentMessage)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun failedPostRollsBackOptimisticCommentAndRestoresDraft() = runTest(dispatcher) {
        authenticate()
        repository.pages["track" to 0] = TrackCommentsResponse()
        repository.postResponse = FavoriteResponse(error = "Posting failed")
        viewModel.load(trackId = "track", targetCommentId = null)
        advanceUntilIdle()

        viewModel.updateCommentText("Hello")
        viewModel.submitComment()

        assertEquals("Hello", viewModel.state.value.comments.single().message)
        assertEquals("", viewModel.state.value.commentText)

        advanceUntilIdle()

        assertEquals(emptyList(), viewModel.state.value.comments)
        assertEquals("Hello", viewModel.state.value.commentText)
        assertEquals("Posting failed", viewModel.state.value.commentErrorMessage)
        assertFalse(viewModel.state.value.isPosting)
    }

    @Test
    fun responseFromPreviousTrackCannotOverwriteCurrentTrack() = runTest(dispatcher) {
        authenticate()
        repository.pages["first" to 0] = TrackCommentsResponse()
        repository.pages["second" to 0] = TrackCommentsResponse(
            data = listOf(comment("second-comment", trackId = "second")),
        )
        val postGate = CompletableDeferred<Unit>()
        repository.postGate = postGate

        viewModel.load(trackId = "first", targetCommentId = null)
        advanceUntilIdle()
        viewModel.updateCommentText("Pending on first")
        viewModel.submitComment()
        dispatcher.scheduler.runCurrent()

        viewModel.load(trackId = "second", targetCommentId = null)
        postGate.complete(Unit)
        advanceUntilIdle()

        assertEquals("second", viewModel.state.value.trackId)
        assertEquals(listOf("second-comment"), viewModel.state.value.comments.map(Comment::id))
        assertNull(viewModel.state.value.commentErrorMessage)
        assertFalse(viewModel.state.value.isPosting)
    }

    @Test
    fun rapidLikeThenUnlikeSendsLatestIntentInOrder() = runTest(dispatcher) {
        authenticate()
        repository.pages["track" to 0] = TrackCommentsResponse(
            data = listOf(comment("comment")),
        )
        val firstReactionGate = CompletableDeferred<Unit>()
        repository.firstReactionGate = firstReactionGate
        viewModel.load(trackId = "track", targetCommentId = null)
        advanceUntilIdle()

        viewModel.toggleReaction(viewModel.state.value.comments.single())
        dispatcher.scheduler.runCurrent()
        viewModel.toggleReaction(viewModel.state.value.comments.single())

        assertFalse(viewModel.state.value.comments.single().isCurrentUserReacted)
        assertEquals(0, viewModel.state.value.comments.single().reactCount)

        firstReactionGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(listOf(true, false), repository.reactionRequests)
        assertFalse(viewModel.state.value.comments.single().isCurrentUserReacted)
        assertEquals(0, viewModel.state.value.comments.single().reactCount)
    }

    private suspend fun authenticate() {
        authRepository.session.value = SessionState.Authenticated(
            MeResponse(userId = "viewer", name = "Viewer", handle = "viewer"),
        )
        dispatcher.scheduler.runCurrent()
    }

    private fun comment(id: String, trackId: String = "track") = Comment(
        id = id,
        entityId = trackId,
        userId = "author",
        message = "Message $id",
        createdAt = "2026-07-24T12:00:00Z",
    )
}

private class FakeCommentsRepository : CommentsRepository {
    val pages = mutableMapOf<Pair<String, Int>, TrackCommentsResponse?>()
    val requestedOffsets = mutableListOf<Int>()
    var postResponse: FavoriteResponse? = FavoriteResponse(status = "success")
    var postGate: CompletableDeferred<Unit>? = null
    var firstReactionGate: CompletableDeferred<Unit>? = null
    val reactionRequests = mutableListOf<Boolean>()

    override suspend fun getTrackComments(
        trackId: String,
        limit: Int,
        offset: Int,
    ): TrackCommentsResponse? {
        requestedOffsets += offset
        return pages[trackId to offset]
    }

    override suspend fun getUserProfile(userId: String): UserProfileModel? = null

    override suspend fun postComment(
        userId: String,
        trackId: String,
        message: String,
        parentId: String?,
    ): FavoriteResponse? {
        postGate?.await()
        return postResponse
    }

    override suspend fun setCommentReaction(
        userId: String,
        commentId: String,
        trackId: String,
        reacted: Boolean,
    ): FavoriteResponse {
        reactionRequests += reacted
        if (reactionRequests.size == 1) firstReactionGate?.await()
        return FavoriteResponse(status = "success")
    }

    override suspend fun deleteComment(
        userId: String,
        commentId: String,
    ): FavoriteResponse = FavoriteResponse(status = "success")
}

private class FakeCommentsAuthRepository : AuthRepository {
    val session = MutableStateFlow<SessionState>(SessionState.Guest)
    override val sessionState: StateFlow<SessionState> = session

    override fun getAuthUrl(redirectUri: String): String = ""
    override suspend fun loginWithCode(code: String, redirectUri: String): Boolean = true
    override suspend fun refreshSession(): Boolean = true
    override fun logout() = Unit
    override fun getCustomName(userId: String): String? = null
    override fun getCustomBio(userId: String): String? = null
    override fun getCustomProfilePic(userId: String): String? = null
    override fun getCustomCoverPhoto(userId: String): String? = null
    override fun getCustomLocation(userId: String): String? = null
    override fun getCustomXHandle(userId: String): String? = null
    override fun getCustomInstagramHandle(userId: String): String? = null
    override fun getCustomTikTokHandle(userId: String): String? = null
    override fun getCustomWebsite(userId: String): String? = null
    override fun getCustomFanClubFlair(userId: String): String? = null

    override fun updateUserProfile(
        userId: String,
        name: String,
        bio: String?,
        profilePicUrl: String?,
        coverPhotoUrl: String?,
        location: String?,
        xHandle: String?,
        instagramHandle: String?,
        tiktokHandle: String?,
        website: String?,
        fanClubFlair: String?,
    ) = Unit
}
