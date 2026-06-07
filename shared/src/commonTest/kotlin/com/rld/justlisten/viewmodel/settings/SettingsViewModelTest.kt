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
        val state = viewModel.settingsState.value
        assertEquals("Blue", state.palletColor)
        assertTrue(state.isDarkThemeOn)
        assertTrue(state.hasDonationNavigationOn)
    }

    @Test
    fun testDarkModeToggled() = runTest(testDispatcher) {
        viewModel.onDarkModeToggled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.settingsState.value
        assertEquals(false, state.isDarkThemeOn)
        assertEquals(false, fakeSettingsRepo.getSettingsInfo().isDarkThemeOn)
    }

    @Test
    fun testDonationToggled() = runTest(testDispatcher) {
        viewModel.onDonationToggled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.settingsState.value
        assertEquals(false, state.hasDonationNavigationOn)
        assertEquals(false, fakeSettingsRepo.getSettingsInfo().hasNavigationDonationOn)
    }

    @Test
    fun testPaletteSelected() = runTest(testDispatcher) {
        viewModel.onPaletteSelected("Green")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.settingsState.value
        assertEquals("Green", state.palletColor)
        assertEquals("Green", fakeSettingsRepo.getSettingsInfo().palletColor)
    }
}

class FakeSettingsRepository : SettingsRepository {
    private var info = SettingsInfo(
        id = 1L,
        hasNavigationDonationOn = true,
        isDarkThemeOn = true,
        palletColor = "Blue",
        customPrimary = null,
        customSecondary = null,
        customBackground = null,
        customSurface = null,
        isFirstLaunch = true
    )

    override fun saveSettingsInfo(
        hasNavigationDonationOn: Boolean,
        isDarkThemeOn: Boolean,
        palletColor: String,
        customPrimary: String?,
        customSecondary: String?,
        customBackground: String?,
        customSurface: String?,
        isFirstLaunch: Boolean
    ) {
        info = SettingsInfo(
            id = 1L,
            hasNavigationDonationOn = hasNavigationDonationOn,
            isDarkThemeOn = isDarkThemeOn,
            palletColor = palletColor,
            customPrimary = customPrimary,
            customSecondary = customSecondary,
            customBackground = customBackground,
            customSurface = customSurface,
            isFirstLaunch = isFirstLaunch
        )
    }

    override fun getSettingsInfo(): SettingsInfo = info
}

class FakeAuthRepository : com.rld.justlisten.datalayer.repositories.AuthRepository {
    override val sessionState = kotlinx.coroutines.flow.MutableStateFlow<com.rld.justlisten.datalayer.repositories.SessionState>(com.rld.justlisten.datalayer.repositories.SessionState.Guest)
    override fun getAuthUrl(redirectUri: String): String = ""
    override suspend fun loginWithCode(code: String, redirectUri: String): Boolean = false
    override fun logout() {}
    override suspend fun refreshSession(): Boolean = false
}

class FakeSyncRepository : com.rld.justlisten.datalayer.repositories.SyncRepository {
    override val syncState = kotlinx.coroutines.flow.MutableStateFlow<com.rld.justlisten.datalayer.repositories.SyncState>(com.rld.justlisten.datalayer.repositories.SyncState.Synced)
    override fun enqueueFavoriteTask(trackId: String, isFavorite: Boolean) {}
    override fun enqueuePlaylistCreateTask(name: String, description: String?, isPrivate: Boolean) {}
    override fun enqueuePlaylistUpdateTask(playlistId: String, songs: List<String>) {}
    override fun enqueuePlaylistDeleteTask(playlistId: String) {}
    override fun enqueuePlaylistDetailsUpdateTask(playlistId: String, name: String, description: String?) {}
    override fun triggerSync() {}
    override fun clearQueue() {}
    override suspend fun performInboundSync(userId: String) {}
}

class FakeSecureStorage : SecureStorage {
    override fun saveToken(key: String, value: String) {}
    override fun getToken(key: String): String? = null
    override fun clear() {}
}
