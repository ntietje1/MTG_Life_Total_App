package ui.tutorial.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import domain.game.CommanderDamageManager
import domain.game.GameStateManager
import domain.game.PlayerCustomizationManager
import domain.game.PlayerStateManager
import domain.game.timer.GameTimerState
import domain.game.timer.TimerManager
import domain.storage.IImageManager
import domain.storage.ISettingsManager
import domain.storage.SettingsManager
import domain.system.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.Player
import model.card.Card
import theme.PlayerColor2
import theme.PlayerColor5
import theme.PlayerColor6
import theme.PlayerColor7
import theme.PlayerColor8
import theme.PlayerColor9
import theme.defaultTextStyle
import theme.scaledSp
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
    patchNotes: String = SettingsManager.instance.patchNotes.value,
    private var playerStates: List<Player> = emptyList(),
    private var allPlanes: List<Card> = emptyList(),
    private var planarDeck: List<Card> = emptyList(),
    private var planarBackStack: List<Card> = emptyList(),
    private val playerPrefs: ArrayList<Player> = arrayListOf()
) : ISettingsManager {
    private val _autoKo = MutableStateFlow(autoKo)
    override val autoKo: StateFlow<Boolean> = _autoKo.asStateFlow()
    override fun setAutoKo(value: Boolean) {
        _autoKo.value = value
    }

    private val _autoSkip = MutableStateFlow(autoSkip)
    override val autoSkip: StateFlow<Boolean> = _autoSkip.asStateFlow()
    override fun setAutoSkip(value: Boolean) {
        _autoSkip.value = value
    }

    private val _keepScreenOn = MutableStateFlow(keepScreenOn)
    override val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()
    override fun setKeepScreenOn(value: Boolean) {
        _keepScreenOn.value = value
    }

    private val _cameraRollDisabled = MutableStateFlow(cameraRollDisabled)
    override val cameraRollDisabled: StateFlow<Boolean> = _cameraRollDisabled.asStateFlow()
    override fun setCameraRollDisabled(value: Boolean) {
        _cameraRollDisabled.value = value
    }

    private val _fastCoinFlip = MutableStateFlow(fastCoinFlip)
    override val fastCoinFlip: StateFlow<Boolean> = _fastCoinFlip.asStateFlow()
    override fun setFastCoinFlip(value: Boolean) {
        _fastCoinFlip.value = value
    }

    private val _numPlayers = MutableStateFlow(numPlayers)
    override val numPlayers: StateFlow<Int> = _numPlayers.asStateFlow()
    override fun setNumPlayers(value: Int) {
        _numPlayers.value = value
    }

    private val _alt4PlayerLayout = MutableStateFlow(alt4PlayerLayout)
    override val alt4PlayerLayout: StateFlow<Boolean> = _alt4PlayerLayout.asStateFlow()
    override fun setAlt4PlayerLayout(value: Boolean) {
        _alt4PlayerLayout.value = value
    }

    private val _darkTheme = MutableStateFlow(darkTheme)
    override val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()
    override fun setDarkTheme(value: Boolean) {
        _darkTheme.value = value
    }

    private val _startingLife = MutableStateFlow(startingLife)
    override val startingLife: StateFlow<Int> = _startingLife.asStateFlow()
    override fun setStartingLife(value: Int) {
        _startingLife.value = value
    }

    private val _tutorialSkip = MutableStateFlow(tutorialSkip)
    override val tutorialSkip: StateFlow<Boolean> = _tutorialSkip.asStateFlow()
    override fun setTutorialSkip(value: Boolean) {
        _tutorialSkip.value = value
    }

    private val _lastSplashScreenShown = MutableStateFlow(lastSplashScreenShown)
    override val lastSplashScreenShown: StateFlow<String> = _lastSplashScreenShown.asStateFlow()
    override fun setLastSplashScreenShown(value: String) {
        _lastSplashScreenShown.value = value
    }

    private val _turnTimer = MutableStateFlow(turnTimer)
    override val turnTimer: StateFlow<Boolean> = _turnTimer.asStateFlow()
    override fun setTurnTimer(value: Boolean) {
        _turnTimer.value = value
    }

    private val _devMode = MutableStateFlow(devMode)
    override val devMode: StateFlow<Boolean> = _devMode.asStateFlow()
    override fun setDevMode(value: Boolean) {
        _devMode.value = value
    }

    private val _patchNotes = MutableStateFlow(patchNotes)
    override val patchNotes: StateFlow<String> = _patchNotes.asStateFlow()
    override fun setPatchNotes(value: String) {
        _patchNotes.value = value
    }

    private val _savedTimerState: MutableStateFlow<GameTimerState?> = MutableStateFlow(null)
    override val savedTimerState: StateFlow<GameTimerState?> = _savedTimerState.asStateFlow()
    override fun setSavedTimerState(value: GameTimerState?) {
        _savedTimerState.value = value
    }

    override fun loadPlayerStates(): List<Player> {
        return playerStates
    }

    override fun savePlayerStates(players: List<Player>) {
        playerStates = players
    }

    override fun savePlanechaseState(allPlanes: List<Card>, planarDeck: List<Card>, planarBackStack: List<Card>) {
        this.allPlanes = allPlanes
        this.planarDeck = planarDeck
        this.planarBackStack = planarBackStack
    }

    override fun loadPlanechaseState(): Triple<List<Card>, List<Card>, List<Card>> {
        return Triple(allPlanes, planarDeck, planarBackStack)
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
    customizationManager: PlayerCustomizationManager,
    playerStateManager: PlayerStateManager,
    commanderDamageManager: CommanderDamageManager,
    gameStateManager: GameStateManager,
    timerManager: TimerManager
) : PlayerButtonViewModel(
    initialState = state,
    settingsManager = settingsManager,
    imageManager = imageManager,
    notificationManager = notificationManager,
    playerStateManager = playerStateManager,
    playerCustomizationManager = customizationManager,
    commanderManager = commanderDamageManager,
    gameStateManager = gameStateManager,
    timerManager = timerManager
)

abstract class MockLifeCounterViewModel(
    lifeCounterState: LifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
    settingsManager: ISettingsManager,
    imageManager: IImageManager,
    notificationManager: NotificationManager
) : LifeCounterViewModel(
    initialState = lifeCounterState,
    settingsManager = settingsManager,
    playerStateManager = PlayerStateManager(settingsManager, imageManager),
    commanderManager = CommanderDamageManager(notificationManager),
    imageManager = imageManager,
    notificationManager = notificationManager,
    planeChaseViewModel = PlaneChaseViewModel(settingsManager),
    playerCustomizationManager = PlayerCustomizationManager(settingsManager),
    gameStateManager = GameStateManager(settingsManager),
    timerManager = TimerManager(settingsManager)
)

@Composable
fun TutorialScreenWrapper(
    modifier: Modifier = Modifier, blur: Boolean, step: Pair<Int, Int>, instructions: String, content: @Composable BoxScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.wrapContentSize()
    ) {
        val blurRadius = remember(Unit) { maxHeight / 75f }
        val textSize = remember(Unit) { (maxHeight / 40f).value }
        Column(
            modifier.then(if (blur) Modifier.blur(blurRadius) else Modifier), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top
        ) {
            Column(
                Modifier.wrapContentSize().offset(y = -(textSize * 0.67f).dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Step ${step.first} of ${step.second}", fontSize = textSize.scaledSp, textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.9f), style = defaultTextStyle()
                )
                Spacer(modifier = Modifier.height(textSize.dp / 3))
                Text(
                    text = instructions, fontSize = (textSize * 1.25f).scaledSp, textAlign = TextAlign.Center, color = Color.White, minLines = 2, style = defaultTextStyle()
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(), content = content
            )
        }
    }
}

@Composable
fun TutorialOverlayScreen(
    onDismiss: () -> Unit, content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().pointerInput(Unit) {
            onDismiss()
        }) {
            content()
        }
    }
}

data class MockGameState(
    val playerStates: List<PlayerButtonState> = listOf(
        PlayerButtonState(
            player = Player(
                life = 40, name = "Player 1", color = PlayerColor7, playerNum = 1
            )
        ),
        PlayerButtonState(
            Player(
                life = 40, name = "Player 2", color = PlayerColor2, playerNum = 2
            )
        ),
        PlayerButtonState(
            Player(
                life = 40, name = "Player 3", color = PlayerColor5, playerNum = 3
            )
        ),
        PlayerButtonState(
            Player(
                life = 40, name = "Player 4", color = PlayerColor6, playerNum = 4
            )
        ),
        PlayerButtonState(
            Player(
                life = 40, name = "Player 5", color = PlayerColor9, playerNum = 5
            )
        ),
        PlayerButtonState(
            Player(
                life = 40, name = "Player 6", color = PlayerColor8, playerNum = 6
            )
        ),
    ), val lifeCounterState: LifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false), val mockSettingsManager: ISettingsManager = MockSettingsManager(
        autoKo = false,
        numPlayers = 4,
        alt4PlayerLayout = false,
        startingLife = 40,
        turnTimer = false,
        playerStates = playerStates.map { it.player },
        planarDeck = emptyList(),
        planarBackStack = emptyList(),
        playerPrefs = arrayListOf()
    ), val mockImageManager: IImageManager = MockImageManager()
)