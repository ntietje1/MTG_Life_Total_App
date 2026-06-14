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
import domain.common.NumberWithRecentChange
import domain.api.ScryfallApi
import domain.game.PlayerCustomizationManager
import domain.game.timer.TimerManager
import domain.game.timer.TimerStateRepository
import domain.state.game.SavedGameRepository
import domain.state.game.GameSessionStore
import domain.state.profile.PlayerProfileRepository
import domain.state.planechase.PlanechaseRepository
import domain.storage.IFileImageStore
import domain.storage.PreferencesRepository
import domain.system.NotificationManager
import com.russhwolf.settings.Settings
import model.Player
import theme.PlayerColor2
import theme.PlayerColor5
import theme.PlayerColor6
import theme.PlayerColor7
import theme.PlayerColor8
import theme.PlayerColor9
import theme.defaultTextStyle
import theme.scaledSp
import ui.dialog.planechase.PlaneChaseViewModel
import ui.lifecounter.GameSessionUiMapper
import ui.lifecounter.LifeCounterState
import ui.lifecounter.LifeCounterViewModel
import ui.lifecounter.PlayerSeatUiState

class MockFileImageStore : IFileImageStore {
    override suspend fun saveImage(bytes: ByteArray): String {
        return "tutorial-image"
    }

    override fun localImageUri(imageId: String): String? {
        return null
    }

    override fun deleteImage(imageId: String) = Unit
}

fun mockPreferencesRepository(
    autoKo: Boolean = true,
    autoSkip: Boolean = false,
    keepScreenOn: Boolean = false,
    cameraRollDisabled: Boolean = false,
    fastCoinFlip: Boolean = false,
    numPlayers: Int = 4,
    alt4PlayerLayout: Boolean = false,
    darkTheme: Boolean = true,
    startingLife: Int = 40,
    tutorialSkip: Boolean = false,
    lastSplashScreenShown: String = "0.0.0",
    turnTimer: Boolean = false,
    devMode: Boolean = false
): PreferencesRepository {
    return PreferencesRepository(InMemorySettings()).also { preferences ->
        preferences.setAutoKo(autoKo)
        preferences.setAutoSkip(autoSkip)
        preferences.setKeepScreenOn(keepScreenOn)
        preferences.setCameraRollDisabled(cameraRollDisabled)
        preferences.setFastCoinFlip(fastCoinFlip)
        preferences.setNumPlayers(numPlayers)
        preferences.setAlt4PlayerLayout(alt4PlayerLayout)
        preferences.setDarkTheme(darkTheme)
        preferences.setStartingLife(startingLife)
        preferences.setTutorialSkip(tutorialSkip)
        preferences.setLastSplashScreenShown(lastSplashScreenShown)
        preferences.setTurnTimer(turnTimer)
        preferences.setDevMode(devMode)
    }
}

private class InMemorySettings : Settings {
    private val values = mutableMapOf<String, Any>()

    override val keys: Set<String>
        get() = values.keys

    override val size: Int
        get() = values.size

    override fun clear() {
        values.clear()
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    override fun hasKey(key: String): Boolean {
        return values.containsKey(key)
    }

    override fun putInt(key: String, value: Int) {
        values[key] = value
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getIntOrNull(key) ?: defaultValue
    }

    override fun getIntOrNull(key: String): Int? {
        return values[key] as? Int
    }

    override fun putLong(key: String, value: Long) {
        values[key] = value
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getLongOrNull(key) ?: defaultValue
    }

    override fun getLongOrNull(key: String): Long? {
        return values[key] as? Long
    }

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun getString(key: String, defaultValue: String): String {
        return getStringOrNull(key) ?: defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return values[key] as? String
    }

    override fun putFloat(key: String, value: Float) {
        values[key] = value
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getFloatOrNull(key) ?: defaultValue
    }

    override fun getFloatOrNull(key: String): Float? {
        return values[key] as? Float
    }

    override fun putDouble(key: String, value: Double) {
        values[key] = value
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getDoubleOrNull(key) ?: defaultValue
    }

    override fun getDoubleOrNull(key: String): Double? {
        return values[key] as? Double
    }

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getBooleanOrNull(key) ?: defaultValue
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return values[key] as? Boolean
    }
}

abstract class MockLifeCounterViewModel(
    lifeCounterState: LifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
    preferencesRepository: PreferencesRepository,
    profileRepository: PlayerProfileRepository,
    fileImageStore: IFileImageStore,
    notificationManager: NotificationManager
) : LifeCounterViewModel(
    initialState = lifeCounterState,
    preferencesRepository = preferencesRepository,
    profileRepository = profileRepository,
    fileImageStore = fileImageStore,
    notificationManager = notificationManager,
    planeChaseViewModel = PlaneChaseViewModel(
        planechaseRepository = PlanechaseRepository(InMemorySettings()),
        scryfallApi = ScryfallApi(requestDelayMillis = 0),
        initialPlaneSearchEnabled = false
    ),
    playerCustomizationManager = PlayerCustomizationManager(profileRepository),
    gameSessionStore = GameSessionStore(SavedGameRepository(InMemorySettings(), preferencesRepository)),
    timerManager = TimerManager(TimerStateRepository(InMemorySettings()), preferencesRepository)
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
    val players: List<Player> = listOf(
        Player(
            lifeTotal = NumberWithRecentChange(40, 0), name = "Player 1", color = PlayerColor7, playerNum = 1
        ),
        Player(
            lifeTotal = NumberWithRecentChange(40, 0), name = "Player 2", color = PlayerColor2, playerNum = 2
        ),
        Player(
            lifeTotal = NumberWithRecentChange(40, 0), name = "Player 3", color = PlayerColor5, playerNum = 3
        ),
        Player(
            lifeTotal = NumberWithRecentChange(40, 0), name = "Player 4", color = PlayerColor6, playerNum = 4
        ),
        Player(
            lifeTotal = NumberWithRecentChange(40, 0), name = "Player 5", color = PlayerColor9, playerNum = 5
        ),
        Player(
            lifeTotal = NumberWithRecentChange(40, 0), name = "Player 6", color = PlayerColor8, playerNum = 6
        ),
    ),
    val lifeCounterState: LifeCounterState = LifeCounterState(
        showButtons = true,
        showLoadingScreen = false,
        players = players.map { player ->
            PlayerSeatUiState(
                seatId = GameSessionUiMapper.seatIdForPlayerNumber(player.playerNum),
                player = player
            )
        }
    ),
    val mockPreferencesRepository: PreferencesRepository = mockPreferencesRepository(
        autoKo = false,
        numPlayers = 4,
        alt4PlayerLayout = false,
        startingLife = 40,
        turnTimer = false
    ),
    val mockProfileRepository: PlayerProfileRepository = PlayerProfileRepository(
        InMemorySettings()
    ),
    val mockFileImageStore: IFileImageStore = MockFileImageStore()
)
