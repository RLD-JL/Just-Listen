package com.rld.justlisten.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.WorkManager
import com.rld.justlisten.android.ui.MainComposable
import com.rld.justlisten.android.ui.theme.ColorPallet
import com.rld.justlisten.android.ui.theme.JustListenTheme
import com.rld.justlisten.android.ui.utils.getColorPallet
import com.rld.justlisten.datalayer.datacalls.settings.getSettingsInfo

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model = (application as JustListenApp).model
        val musicServiceConnection = (application as JustListenApp).musicServiceConnection
        installSplashScreen().apply {
        }
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelUniqueWork("SleepWorker")

        val settingsInfo = mutableStateOf(model.repository.getSettingsInfo())
        setContent {
            JustListenTheme(
                darkTheme = settingsInfo.value.isDarkThemeOn,
                colorPallet = getColorPallet(settingsInfo.value.palletColor)
            ) {
                val statusBarColor =
                    if (getColorPallet(settingsInfo.value.palletColor) == ColorPallet.Dark) MaterialTheme.colors.background.toArgb()
                    else MaterialTheme.colors.primary.toArgb()
                window.statusBarColor = statusBarColor
                window.navigationBarColor =
                    if (getColorPallet(settingsInfo.value.palletColor) == ColorPallet.Dark)
                        MaterialTheme.colors.background.toArgb() else MaterialTheme.colors.primary.toArgb()
                MainComposable(
                    model, musicServiceConnection, settingsUpdated = {
                        settingsInfo.value = model.repository.getSettingsInfo()
                    },
                    hasNavigationDonationOn = settingsInfo.value.hasNavigationDonationOn,
                    updateStatusBarColor = { color, extended ->
                        if (extended)
                            window.statusBarColor = color
                        else
                            window.statusBarColor = statusBarColor
                    }
                )
            }
        }
    }
}




