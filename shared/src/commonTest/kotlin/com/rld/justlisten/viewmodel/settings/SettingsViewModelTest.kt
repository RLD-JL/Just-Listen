package com.rld.justlisten.viewmodel.settings

import com.rld.justlisten.database.settingsscreen.SettingsInfo
import com.rld.justlisten.datalayer.repositories.SettingsRepository
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.util.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.delay
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeSettingsRepo = FakeSettingsRepository()
    private val fakeAuthRepo = FakeAuthRepository()
    private val fakeSyncRepo = FakeSyncRepository()
    private lateinit var viewModel: SettingsViewModel
    private val fakeSecureStorage = FakeSecureStorage()
    private val apiClient = ApiClient(apiKey = "", secureStorage = fakeSecureStorage)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(fakeSettingsRepo, fakeAuthRepo, fakeSyncRepo, apiClient)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_LoadsFromRepository() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        var retries = 100
        while (viewModel.settingsState.value.palletColor == "Pink" && retries > 0) {
            delay(10)
            testDispatcher.scheduler.runCurrent()
            retries--
        }

        val state = viewModel.settingsState.value
        assertEquals("Blue", state.palletColor)
        assertTrue(state.isDarkThemeOn)
        assertTrue(state.hasSupportNavigationOn)
    }

    @Test
    fun testDarkModeToggled() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        var retries = 100
        while (viewModel.settingsState.value.palletColor == "Pink" && retries > 0) {
            delay(10)
            testDispatcher.scheduler.runCurrent()
            retries--
        }

        viewModel.onDarkModeToggled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        retries = 100
        while (fakeSettingsRepo.getSettingsInfo().isDarkThemeOn && retries > 0) {
            delay(10)
            retries--
        }

        val state = viewModel.settingsState.value
        assertEquals(false, state.isDarkThemeOn)
        assertEquals(false, fakeSettingsRepo.getSettingsInfo().isDarkThemeOn)
    }

    @Test
    fun testSupportToggled() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        var retries = 100
        while (viewModel.settingsState.value.palletColor == "Pink" && retries > 0) {
            delay(10)
            testDispatcher.scheduler.runCurrent()
            retries--
        }

        viewModel.onSupportToggled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        retries = 100
        while (fakeSettingsRepo.getSettingsInfo().hasNavigationSupportOn && retries > 0) {
            delay(10)
            retries--
        }

        val state = viewModel.settingsState.value
        assertEquals(false, state.hasSupportNavigationOn)
        assertEquals(false, fakeSettingsRepo.getSettingsInfo().hasNavigationSupportOn)
    }

    @Test
    fun testPaletteSelected() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        var retries = 100
        while (viewModel.settingsState.value.palletColor == "Pink" && retries > 0) {
            delay(10)
            testDispatcher.scheduler.runCurrent()
            retries--
        }

        viewModel.onPaletteSelected("Green")
        testDispatcher.scheduler.advanceUntilIdle()

        retries = 100
        while (fakeSettingsRepo.getSettingsInfo().palletColor != "Green" && retries > 0) {
            delay(10)
            retries--
        }

        val state = viewModel.settingsState.value
        assertEquals("Green", state.palletColor)
        assertEquals("Green", fakeSettingsRepo.getSettingsInfo().palletColor)
    }

    @Test
    fun testEqualizerPreviewCanCancelOrSave() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        val rockBands = listOf(4f, 2f, -2f, 2f, 5f)

        viewModel.previewEqualizerSettings(true, "Rock", rockBands)
        assertTrue(viewModel.settingsState.value.isEqEnabled)
        assertEquals(rockBands, viewModel.settingsState.value.eqBands)
        assertEquals(false, fakeSettingsRepo.getSettingsInfo().isEqEnabled)

        viewModel.cancelEqualizerPreview()
        assertEquals(false, viewModel.settingsState.value.isEqEnabled)
        assertEquals("Flat", viewModel.settingsState.value.eqPreset)

        viewModel.previewEqualizerSettings(true, "Rock", rockBands)
        viewModel.saveEqualizerPreview(true, "Rock", rockBands)
        testDispatcher.scheduler.advanceUntilIdle()

        var retries = 100
        while (!fakeSettingsRepo.getSettingsInfo().isEqEnabled && retries > 0) {
            delay(10)
            retries--
        }

        assertTrue(fakeSettingsRepo.getSettingsInfo().isEqEnabled)
        assertEquals("Rock", fakeSettingsRepo.getSettingsInfo().eqPreset)
        assertEquals(rockBands.joinToString(","), fakeSettingsRepo.getSettingsInfo().eqBands)
    }

    @Test
    fun testSessionRestorationRetriesUntilAuthenticated() = runTest(testDispatcher) {
        val restoringAuthRepository = FakeAuthRepository().apply {
            sessionState.value = com.rld.justlisten.datalayer.repositories.SessionState.Restoring
            refreshSessionHandler = {
                if (refreshSessionCalls >= 2) {
                    sessionState.value = com.rld.justlisten.datalayer.repositories.SessionState.Authenticated(
                        com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse(
                            userId = "user-id",
                            name = "Listener",
                            handle = "listener",
                        )
                    )
                    true
                } else {
                    false
                }
            }
        }
        val restoringViewModel = SettingsViewModel(
            fakeSettingsRepo,
            restoringAuthRepository,
            fakeSyncRepo,
            apiClient,
        )

        runCurrent()
        assertEquals(0, restoringAuthRepository.refreshSessionCalls)
        assertTrue(
            restoringViewModel.settingsState.value.sessionState is
                com.rld.justlisten.datalayer.repositories.SessionState.Restoring
        )

        advanceTimeBy(1_001L)
        runCurrent()

        assertEquals(1, restoringAuthRepository.refreshSessionCalls)
        advanceTimeBy(2_001L)
        runCurrent()

        assertEquals(2, restoringAuthRepository.refreshSessionCalls)
        assertTrue(
            restoringViewModel.settingsState.value.sessionState is
                com.rld.justlisten.datalayer.repositories.SessionState.Authenticated
        )
    }

    @Test
    fun testSessionRestorationStopsAfterFiveAttempts() = runTest(testDispatcher) {
        val restoringAuthRepository = FakeAuthRepository().apply {
            sessionState.value = com.rld.justlisten.datalayer.repositories.SessionState.Restoring
        }
        val restoringViewModel = SettingsViewModel(
            fakeSettingsRepo,
            restoringAuthRepository,
            fakeSyncRepo,
            apiClient,
        )

        runCurrent()
        advanceTimeBy(31_001L)
        runCurrent()

        assertEquals(5, restoringAuthRepository.refreshSessionCalls)
        assertTrue(restoringViewModel.settingsState.value.isSessionRecoveryExhausted)
    }

    @Test
    fun testOAuthProfileFailureStartsSessionRestoration() = runTest(testDispatcher) {
        fakeAuthRepo.loginWithCodeHandler = {
            sessionState.value = com.rld.justlisten.datalayer.repositories.SessionState.Restoring
            false
        }
        fakeAuthRepo.refreshSessionHandler = {
            sessionState.value = com.rld.justlisten.datalayer.repositories.SessionState.Authenticated(
                com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse(
                    userId = "user-id",
                    name = "Listener",
                    handle = "listener",
                )
            )
            true
        }

        viewModel.loginWithCode("code", "justlisten://oauth/callback")
        runCurrent()
        advanceTimeBy(1_001L)
        runCurrent()

        assertEquals(1, fakeAuthRepo.loginWithCodeCalls)
        assertEquals(1, fakeAuthRepo.refreshSessionCalls)
        assertTrue(
            viewModel.settingsState.value.sessionState is
                com.rld.justlisten.datalayer.repositories.SessionState.Authenticated
        )
    }
}

class FakeSettingsRepository : SettingsRepository {
    private var info = SettingsInfo(
        id = 1L,
        hasNavigationSupportOn = true,
        isDarkThemeOn = true,
        palletColor = "Blue",
        customPrimary = null,
        customSecondary = null,
        customBackground = null,
        customSurface = null,
        isFirstLaunch = true,
        isOngoingStreamEnabled = true,
        isEqEnabled = false,
        eqPreset = "Flat",
        eqBands = ""
    )

    override fun saveSettingsInfo(
        hasNavigationSupportOn: Boolean,
        isDarkThemeOn: Boolean,
        palletColor: String,
        customPrimary: String?,
        customSecondary: String?,
        customBackground: String?,
        customSurface: String?,
        isFirstLaunch: Boolean,
        isOngoingStreamEnabled: Boolean,
        isEqEnabled: Boolean,
        eqPreset: String,
        eqBands: String
    ) {
        info = SettingsInfo(
            id = 1L,
            hasNavigationSupportOn = hasNavigationSupportOn,
            isDarkThemeOn = isDarkThemeOn,
            palletColor = palletColor,
            customPrimary = customPrimary,
            customSecondary = customSecondary,
            customBackground = customBackground,
            customSurface = customSurface,
            isFirstLaunch = isFirstLaunch,
            isOngoingStreamEnabled = isOngoingStreamEnabled,
            isEqEnabled = isEqEnabled,
            eqPreset = eqPreset,
            eqBands = eqBands
        )
    }

    override var isCrossfadeEnabled: Boolean = false
    override var crossfadeDurationSeconds: Double = 5.0
    override var crossfadeStyle: String = "Radio Segue"
    override var isVolumeNormalizationEnabled: Boolean = false
    override fun getSettingsInfo(): SettingsInfo = info

    override fun blockUser(userId: String, username: String) {}
    override fun unblockUser(userId: String) {}
    override fun getBlockedUsers(): List<com.rld.justlisten.database.settingsscreen.BlockedUser> = emptyList()
    override fun hideComment(commentId: String) {}
    override fun unhideComment(commentId: String) {}
    override fun getHiddenComments(): List<String> = emptyList()
}

class FakeAuthRepository : com.rld.justlisten.datalayer.repositories.AuthRepository {
    override val sessionState = kotlinx.coroutines.flow.MutableStateFlow<com.rld.justlisten.datalayer.repositories.SessionState>(com.rld.justlisten.datalayer.repositories.SessionState.Guest)
    var refreshSessionCalls = 0
    var refreshSessionHandler: suspend FakeAuthRepository.() -> Boolean = { false }
    var loginWithCodeCalls = 0
    var loginWithCodeHandler: suspend FakeAuthRepository.() -> Boolean = { false }
    override fun getAuthUrl(redirectUri: String): String = ""
    override suspend fun loginWithCode(code: String, redirectUri: String): Boolean {
        loginWithCodeCalls += 1
        return loginWithCodeHandler()
    }
    override fun logout() {}
    override suspend fun refreshSession(): Boolean {
        refreshSessionCalls += 1
        return refreshSessionHandler()
    }
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
    override fun updateUserProfile(userId: String, name: String, bio: String?, profilePicUrl: String?, coverPhotoUrl: String?, location: String?, xHandle: String?, instagramHandle: String?, tiktokHandle: String?, website: String?, fanClubFlair: String?) {}
}

class FakeSyncRepository : com.rld.justlisten.datalayer.repositories.SyncRepository {
    override val syncState = kotlinx.coroutines.flow.MutableStateFlow<com.rld.justlisten.datalayer.repositories.SyncState>(com.rld.justlisten.datalayer.repositories.SyncState.Synced)
    override fun enqueueFavoriteTask(userId: String, trackId: String, isFavorite: Boolean) {}
    override fun enqueuePlaylistCreateTask(name: String, description: String?, isPrivate: Boolean) {}
    override fun enqueuePlaylistUpdateTask(playlistId: String, songs: List<String>) {}
    override fun enqueuePlaylistDeleteTask(playlistId: String) {}
    override fun enqueuePlaylistDetailsUpdateTask(playlistId: String, name: String, description: String?) {}
    override fun triggerSync() {}
    override fun clearQueue() {}
    override suspend fun runPendingSync(): Boolean = true
    override suspend fun performInboundSync(userId: String) {}
}

class FakeSecureStorage : SecureStorage {
    override fun saveToken(key: String, value: String) {}
    override fun getToken(key: String): String? = null
    override fun clear() {}
}
