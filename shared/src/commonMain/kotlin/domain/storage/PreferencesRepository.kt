package domain.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.VersionNumber

class PreferencesRepository(
    private val settings: Settings
) {
    private val _autoKo = MutableStateFlow(settings.getBoolean(Keys.AutoKo, true))
    val autoKo: StateFlow<Boolean> = _autoKo.asStateFlow()
    fun setAutoKo(value: Boolean) = putBoolean(Keys.AutoKo, value, _autoKo)

    private val _autoSkip = MutableStateFlow(settings.getBoolean(Keys.AutoSkip, false))
    val autoSkip: StateFlow<Boolean> = _autoSkip.asStateFlow()
    fun setAutoSkip(value: Boolean) = putBoolean(Keys.AutoSkip, value, _autoSkip)

    private val _keepScreenOn = MutableStateFlow(settings.getBoolean(Keys.KeepScreenOn, false))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()
    fun setKeepScreenOn(value: Boolean) = putBoolean(Keys.KeepScreenOn, value, _keepScreenOn)

    private val _cameraRollDisabled = MutableStateFlow(settings.getBoolean(Keys.CameraRollDisabled, false))
    val cameraRollDisabled: StateFlow<Boolean> = _cameraRollDisabled.asStateFlow()
    fun setCameraRollDisabled(value: Boolean) = putBoolean(Keys.CameraRollDisabled, value, _cameraRollDisabled)

    private val _fastCoinFlip = MutableStateFlow(settings.getBoolean(Keys.FastCoinFlip, false))
    val fastCoinFlip: StateFlow<Boolean> = _fastCoinFlip.asStateFlow()
    fun setFastCoinFlip(value: Boolean) = putBoolean(Keys.FastCoinFlip, value, _fastCoinFlip)

    private val _numPlayers = MutableStateFlow(settings.getInt(Keys.NumPlayers, 4))
    val numPlayers: StateFlow<Int> = _numPlayers.asStateFlow()
    fun setNumPlayers(value: Int) = putInt(Keys.NumPlayers, value, _numPlayers)

    private val _alt4PlayerLayout = MutableStateFlow(settings.getBoolean(Keys.Alt4PlayerLayout, false))
    val alt4PlayerLayout: StateFlow<Boolean> = _alt4PlayerLayout.asStateFlow()
    fun setAlt4PlayerLayout(value: Boolean) = putBoolean(Keys.Alt4PlayerLayout, value, _alt4PlayerLayout)

    private val _darkTheme = MutableStateFlow(settings.getBoolean(Keys.DarkTheme, true))
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()
    fun setDarkTheme(value: Boolean) = putBoolean(Keys.DarkTheme, value, _darkTheme)

    private val _startingLife = MutableStateFlow(settings.getInt(Keys.StartingLife, 40))
    val startingLife: StateFlow<Int> = _startingLife.asStateFlow()
    fun setStartingLife(value: Int) = putInt(Keys.StartingLife, value, _startingLife)

    private val _tutorialSkip = MutableStateFlow(settings.getBoolean(Keys.TutorialSkip, false))
    val tutorialSkip: StateFlow<Boolean> = _tutorialSkip.asStateFlow()
    fun setTutorialSkip(value: Boolean) = putBoolean(Keys.TutorialSkip, value, _tutorialSkip)

    private val _lastSplashScreenShown = MutableStateFlow(settings.getString(Keys.LastSplashScreenShown, VersionNumber.zero.value))
    val lastSplashScreenShown: StateFlow<String> = _lastSplashScreenShown.asStateFlow()
    fun setLastSplashScreenShown(value: String) = putString(Keys.LastSplashScreenShown, value, _lastSplashScreenShown)

    private val _turnTimer = MutableStateFlow(settings.getBoolean(Keys.TurnTimer, false))
    val turnTimer: StateFlow<Boolean> = _turnTimer.asStateFlow()
    fun setTurnTimer(value: Boolean) = putBoolean(Keys.TurnTimer, value, _turnTimer)

    private val _devMode = MutableStateFlow(settings.getBoolean(Keys.DevMode, false))
    val devMode: StateFlow<Boolean> = _devMode.asStateFlow()
    fun setDevMode(value: Boolean) = putBoolean(Keys.DevMode, value, _devMode)

    private fun putBoolean(key: String, value: Boolean, flow: MutableStateFlow<Boolean>) {
        settings.putBoolean(key, value)
        flow.value = value
    }

    private fun putInt(key: String, value: Int, flow: MutableStateFlow<Int>) {
        settings.putInt(key, value)
        flow.value = value
    }

    private fun putString(key: String, value: String, flow: MutableStateFlow<String>) {
        settings.putString(key, value)
        flow.value = value
    }

    private object Keys {
        const val AutoKo = "autoKo"
        const val AutoSkip = "autoSkip"
        const val KeepScreenOn = "keepScreenOn"
        const val CameraRollDisabled = "cameraRollDisabled"
        const val FastCoinFlip = "fastCoinFlip"
        const val NumPlayers = "numPlayers"
        const val Alt4PlayerLayout = "alt4PlayerLayout"
        const val DarkTheme = "darkTheme"
        const val StartingLife = "startingLife"
        const val TutorialSkip = "tutorialSkip"
        const val LastSplashScreenShown = "lastSplashScreenShown"
        const val TurnTimer = "turnTimer"
        const val DevMode = "devMode"
    }
}
