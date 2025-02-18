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
import data.GameRepository
import domain.common.NumberWithRecentChange
import domain.game.timer.GameTimerState
import domain.storage.IImageStore
import domain.storage.ISettingsStore
import domain.storage.LocalSettingsStore
import domain.usecase.player.state.ManagePlayerStateUseCase
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
import ui.lifecounter.LifeCounterState
import ui.lifecounter.playerbutton.PlayerButtonState

class MockSettingsStore(
    autoKo: Boolean = LocalSettingsStore.instance.autoKo.value,
    autoSkip: Boolean = LocalSettingsStore.instance.autoSkip.value,
    keepScreenOn: Boolean = LocalSettingsStore.instance.keepScreenOn.value,
    cameraRollDisabled: Boolean = LocalSettingsStore.instance.cameraRollDisabled.value,
    fastCoinFlip: Boolean = LocalSettingsStore.instance.fastCoinFlip.value,
    numPlayers: Int = LocalSettingsStore.instance.defaultNumPlayers.value,
    alt4PlayerLayout: Boolean = LocalSettingsStore.instance.defaultAltPlayerLayout.value,
    darkTheme: Boolean = LocalSettingsStore.instance.darkTheme.value,
    startingLife: Int = LocalSettingsStore.instance.defaultStartingLife.value,
    tutorialSkip: Boolean = LocalSettingsStore.instance.tutorialSkip.value,
    lastSplashScreenShown: String = LocalSettingsStore.instance.lastSplashScreenShown.value,
    turnTimer: Boolean = LocalSettingsStore.instance.turnTimer.value,
    devMode: Boolean = LocalSettingsStore.instance.devMode.value,
    currentGameId: Long? = LocalSettingsStore.instance.currentGameId.value,
    patchNotes: String = LocalSettingsStore.instance.patchNotes.value,
    private var playerStates: List<Player> = emptyList(),
    private var allPlanes: List<Card> = emptyList(),
    private var planarDeck: List<Card> = emptyList(),
    private var planarBackStack: List<Card> = emptyList(),
    private val playerPrefs: ArrayList<Player> = arrayListOf()
) : ISettingsStore {
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
    override val defaultNumPlayers: StateFlow<Int> = _numPlayers.asStateFlow()
    override fun setDefaultNumPlayers(value: Int) {
        _numPlayers.value = value
    }

    private val _alt4PlayerLayout = MutableStateFlow(alt4PlayerLayout)
    override val defaultAltPlayerLayout: StateFlow<Boolean> = _alt4PlayerLayout.asStateFlow()
    override fun setDefaultAltPlayerLayout(value: Boolean) {
        _alt4PlayerLayout.value = value
    }

    private val _darkTheme = MutableStateFlow(darkTheme)
    override val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()
    override fun setDarkTheme(value: Boolean) {
        _darkTheme.value = value
    }

    private val _startingLife = MutableStateFlow(startingLife)
    override val defaultStartingLife: StateFlow<Int> = _startingLife.asStateFlow()
    override fun setDefaultStartingLife(value: Int) {
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

    private val _currentGameId = MutableStateFlow(currentGameId)
    override val currentGameId: StateFlow<Long?> = _currentGameId.asStateFlow()
    override fun setCurrentGameId(value: Long) {
        _currentGameId.value = value
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

class MockImageStore : IImageStore {
    override suspend fun copyImageToLocalStorage(bytes: ByteArray, fileName: String): String {
        return fileName
    }

    override fun getImagePath(fileName: String): String? {
        return null
    }
}

private val mockRepository = Any() as GameRepository
private val mockManagePlayerStateUseCase = Any() as ManagePlayerStateUseCase

//open class MockPlayerButtonViewModel(
//    state: PlayerButtonState,
//    settingsManager: ISettingsStore,
//    imageManager: IImageStore,
//    notificationManager: NotificationManager,
//    customizationManager: PlayerCustomizationManager,
//    commanderDamageManager: CommanderDamageManager,
//    timerManager: TimerManager
//) : PlayerButtonViewModel(
//    initialState = state,
//    settingsManager = settingsManager,
//    imageManager = imageManager,
//    notificationManager = notificationManager,
//    playerCustomizationManager = customizationManager,
//    commanderManager = commanderDamageManager,
//    timerManager = timerManager,
//    managePlayerStateUseCase = mockManagePlayerStateUseCase,
//    savePlayerStateUseCase = Any() as SavePlayerStateUseCase,
//    playerLifeRecentChangeState = Any() as PlayerLifeRecentChangeState,
//    managePlayerCustomizationUseCase = Any() as ManagePlayerCustomizationUseCase
//)

//abstract class MockLifeCounterViewModel(
//    lifeCounterState: LifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
//    settingsManager: ISettingsStore,
//    notificationManager: NotificationManager
//) : LifeCounterViewModel(
//    initialState = lifeCounterState,
//    settingsManager = settingsManager,
//    commanderManager = CommanderDamageManager(notificationManager),
//    notificationManager = notificationManager,
//    planeChaseViewModel = PlaneChaseViewModel(settingsManager),
//    newGameUseCase = NewGameUseCase(settingsManager, mockRepository),
//    saveGameUseCase = SaveGameUseCase(mockRepository),
//    loadGameStateUseCase = LoadGameStateUseCase(mockRepository),
//    timerManager = TimerManager(settingsManager),
//    managePlayerStateUseCase = mockManagePlayerStateUseCase,
//    newPlayerUseCase = Any() as NewPlayerUseCase,
//    monarchy
//)

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
                lifeTotal = NumberWithRecentChange(40, 0), name = "Player 1", color = PlayerColor7, playerNum = 1
            )
        ),
        PlayerButtonState(
            Player(
                lifeTotal = NumberWithRecentChange(40, 0), name = "Player 2", color = PlayerColor2, playerNum = 2
            )
        ),
        PlayerButtonState(
            Player(
                lifeTotal = NumberWithRecentChange(40, 0), name = "Player 3", color = PlayerColor5, playerNum = 3
            )
        ),
        PlayerButtonState(
            Player(
                lifeTotal = NumberWithRecentChange(40, 0), name = "Player 4", color = PlayerColor6, playerNum = 4
            )
        ),
        PlayerButtonState(
            Player(
                lifeTotal = NumberWithRecentChange(40, 0), name = "Player 5", color = PlayerColor9, playerNum = 5
            )
        ),
        PlayerButtonState(
            Player(
                lifeTotal = NumberWithRecentChange(40, 0), name = "Player 6", color = PlayerColor8, playerNum = 6
            )
        ),
    ), val lifeCounterState: LifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false), val mockSettingsManager: ISettingsStore = MockSettingsStore(
        autoKo = false,
        numPlayers = 4,
        alt4PlayerLayout = false,
        startingLife = 40,
        turnTimer = false,
        playerStates = playerStates.map { it.player },
        planarDeck = emptyList(),
        planarBackStack = emptyList(),
        playerPrefs = arrayListOf()
    ), val mockImageManager: IImageStore = MockImageStore()
)