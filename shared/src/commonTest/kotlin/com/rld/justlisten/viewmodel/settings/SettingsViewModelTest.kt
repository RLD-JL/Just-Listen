package com.rld.justlisten.viewmodel.settings

import com.rld.justlisten.database.settingsscreen.SettingsInfo
import com.rld.justlisten.datalayer.repositories.SettingsRepository
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
    private lateinit var viewModel: SettingsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(fakeSettingsRepo)
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
        palletColor = "Blue"
    )

    override fun saveSettingsInfo(hasNavigationDonationOn: Boolean, isDarkThemeOn: Boolean, palletColor: String) {
        info = SettingsInfo(1L, hasNavigationDonationOn, isDarkThemeOn, palletColor)
    }

    override fun getSettingsInfo(): SettingsInfo = info
}
