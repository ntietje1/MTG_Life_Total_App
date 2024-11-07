package ui.tutorial.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import data.IImageManager
import data.ISettingsManager
import data.Player
import data.SettingsManager
import data.serializable.Card
import di.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ui.dialog.planechase.PlaneChaseViewModel
import ui.lifecounter.LifeCounterState
import ui.lifecounter.LifeCounterViewModel
import ui.lifecounter.playerbutton.PlayerButtonState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

class MockSettingsManager(
    autoKo: Boolean = SettingsManager.instance.autoKo.value,
    autoSkip: Boolean = SettingsManager.instance.autoSkip.value,
    keepScreenOn: Boolean = SettingsManager.instance.keepScreenOn.value,
    cameraRollDisabled: Boolean = SettingsManager.instance.cameraRollDisabled.value,
    fastCoinFlip: Boolean = SettingsManager.instance.fastCoinFlip.value,
    numPlayers: Int = SettingsManager.instance.numPlayers.value,
    alt4PlayerLayout: Boolean = SettingsManager.instance.alt4PlayerLayout.value,
    darkTheme: Boolean = SettingsManager.instance.darkTheme.value,
    startingLife: Int = SettingsManager.instance.startingLife.value,
    tutorialSkip: Boolean = SettingsManager.instance.tutorialSkip.value,
    lastSplashScreenShown: String = SettingsManager.instance.lastSplashScreenShown.value,
    turnTimer: Boolean = SettingsManager.instance.turnTimer.value,
    devMode: Boolean = SettingsManager.instance.devMode.value,
    private var playerStates: List<Player> = emptyList(),
    private var planarDeck: List<Card> = emptyList(),
    private var planarBackStack: List<Card> = emptyList(),
    private val playerPrefs: ArrayList<Player> = arrayListOf()
) : ISettingsManager {
    override val autoKo: StateFlow<Boolean> = MutableStateFlow(autoKo)
    override fun setAutoKo(value: Boolean) {}

    override val autoSkip: StateFlow<Boolean> = MutableStateFlow(autoSkip)
    override fun setAutoSkip(value: Boolean) {}

    override val keepScreenOn: StateFlow<Boolean> = MutableStateFlow(keepScreenOn)
    override fun setKeepScreenOn(value: Boolean) {}

    override val cameraRollDisabled: StateFlow<Boolean> = MutableStateFlow(cameraRollDisabled)
    override fun setCameraRollDisabled(value: Boolean) {}

    override val fastCoinFlip: StateFlow<Boolean> = MutableStateFlow(fastCoinFlip)
    override fun setFastCoinFlip(value: Boolean) {}

    override val numPlayers: StateFlow<Int> = MutableStateFlow(numPlayers)
    override fun setNumPlayers(value: Int) {}

    override val alt4PlayerLayout: StateFlow<Boolean> = MutableStateFlow(alt4PlayerLayout)
    override fun setAlt4PlayerLayout(value: Boolean) {}

    override val darkTheme: StateFlow<Boolean> = MutableStateFlow(darkTheme)
    override fun setDarkTheme(value: Boolean) {}

    override val startingLife: StateFlow<Int> = MutableStateFlow(startingLife)
    override fun setStartingLife(value: Int) {}

    override val tutorialSkip: StateFlow<Boolean> = MutableStateFlow(tutorialSkip)
    override fun setTutorialSkip(value: Boolean) {}

    override val lastSplashScreenShown: StateFlow<String> = MutableStateFlow(lastSplashScreenShown)
    override fun setLastSplashScreenShown(value: String) {}

    override val turnTimer: StateFlow<Boolean> = MutableStateFlow(turnTimer)
    override fun setTurnTimer(value: Boolean) {}

    override val devMode: StateFlow<Boolean> = MutableStateFlow(devMode)
    override fun setDevMode(value: Boolean) {}

    override fun loadPlayerStates(): List<Player> {
        return playerStates
    }

    override fun savePlayerStates(players: List<Player>) {
        playerStates = players
    }

    override fun savePlanechaseState(planarDeck: List<Card>, planarBackStack: List<Card>) {
        this.planarDeck = planarDeck
        this.planarBackStack = planarBackStack
    }

    override fun loadPlanechaseState(): Pair<List<Card>, List<Card>> {
        return Pair(planarDeck, planarBackStack)
    }

    override fun savePlayerPref(player: Player) {
        playerPrefs.add(player)
    }

    override fun deletePlayerPref(player: Player) {
        playerPrefs.remove(player)
    }

    override fun loadPlayerPrefs(): ArrayList<Player> {
        return playerPrefs
    }
}

class MockImageManager : IImageManager {
    override suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String {
        return fileName
    }

    override fun getImagePath(fileName: String): String? {
        return null
    }
}

open class MockPlayerButtonViewModel(
    state: PlayerButtonState,
    settingsManager: ISettingsManager,
    imageManager: IImageManager,
    notificationManager: NotificationManager,
    onCommanderButtonClickedCallback: (PlayerButtonViewModel) -> Unit,
    setAllMonarchy: (Boolean) -> Unit,
    getCurrentDealer: () -> PlayerButtonViewModel?,
    updateCurrentDealerMode: (Boolean) -> Unit,
    triggerSave: () -> Unit,
    resetPlayerColor: (Player) -> Player,
    moveTimerCallback: () -> Unit
) : PlayerButtonViewModel(
    initialState = state,
    settingsManager = settingsManager,
    imageManager = imageManager,
    notificationManager = notificationManager,
    onCommanderButtonClickedCallback = onCommanderButtonClickedCallback,
    setAllMonarchy = setAllMonarchy,
    getCurrentDealer = getCurrentDealer,
    updateCurrentDealerMode = updateCurrentDealerMode,
    triggerSave = triggerSave,
    resetPlayerColor = resetPlayerColor,
    moveTimerCallback = moveTimerCallback
)

abstract class MockLifeCounterViewModel(
    lifeCounterState: LifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
    settingsManager: ISettingsManager,
    imageManager: IImageManager,
    notificationManager: NotificationManager
) : LifeCounterViewModel(
    initialState = lifeCounterState,
    settingsManager = settingsManager,
    imageManager = imageManager,
    notificationManager = notificationManager,
    planeChaseViewModel = PlaneChaseViewModel(settingsManager),
) {

}

@Composable
fun TutorialOverlayScreen(
    onDismiss: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxSize().pointerInput(Unit) {
                onDismiss()
            }
        ) {
            content()
        }
    }
}