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
import domain.state.game.SeatId
import domain.state.profile.PlayerProfileRepository
import domain.storage.IFileImageStore
import domain.storage.PreferencesRepository
import domain.system.NotificationManager
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.Player
import theme.PlayerColor2
import theme.PlayerColor5
import theme.PlayerColor6
import theme.PlayerColor7
import theme.PlayerColor8
import theme.PlayerColor9
import theme.defaultTextStyle
import theme.scaledSp
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.DayNightState
import ui.lifecounter.GameSessionUiMapper
import ui.lifecounter.LifeCounterModal
import ui.lifecounter.LifeCounterModalStack
import ui.lifecounter.LifeCounterScreenController
import ui.lifecounter.LifeCounterState
import ui.lifecounter.MiddleButtonState
import ui.lifecounter.PlayerSeatUiState
import ui.lifecounter.closePlayerCustomization
import ui.lifecounter.openPlayerCounterSelection
import ui.lifecounter.openPlayerCounters
import ui.lifecounter.openPlayerCustomization
import ui.lifecounter.openPlayerSettings
import ui.lifecounter.popPlayerButtonBackStack
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonAction
import ui.lifecounter.setAllPlayerButtonStates

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
    private val preferencesRepository: PreferencesRepository,
    private val profileRepository: PlayerProfileRepository,
    private val fileImageStore: IFileImageStore,
    private val notificationManager: NotificationManager?
) : LifeCounterScreenController {
    private val _state = MutableStateFlow(lifeCounterState)
    override val state = _state.asStateFlow()
    override val numPlayers = preferencesRepository.numPlayers
    override val alt4PlayerLayout = preferencesRepository.alt4PlayerLayout
    override val turnTimerEnabled = preferencesRepository.turnTimer

    private val customizationViewModels = mutableMapOf<SeatId, CustomizationViewModel>()

    override fun onNavigate(firstNavigation: Boolean) {
        _state.value = _state.value.copy(showButtons = true, showLoadingScreen = false)
    }

    override fun openModal(value: LifeCounterModal) {
        _state.value = _state.value.copy(modalStack = _state.value.modalStack.open(value))
    }

    override fun closeModal() {
        _state.value = _state.value.copy(modalStack = LifeCounterModalStack.Empty)
    }

    override fun goBackInModal() {
        _state.value = _state.value.copy(modalStack = _state.value.modalStack.goBack())
    }

    override fun setBlurBackground(value: Boolean) {
        _state.value = _state.value.copy(blurBackground = value)
    }

    open override fun toggleDarkTheme(value: Boolean?) {
        preferencesRepository.setDarkTheme(value ?: !preferencesRepository.darkTheme.value)
    }

    override fun toggleKeepScreenOn(value: Boolean?) {
        preferencesRepository.setKeepScreenOn(value ?: !preferencesRepository.keepScreenOn.value)
    }

    override fun setShowButtons(value: Boolean) {
        _state.value = _state.value.copy(showButtons = value)
    }

    override fun setAlt4PlayerLayout(value: Boolean) {
        preferencesRepository.setAlt4PlayerLayout(value)
    }

    open override fun setNumPlayers(value: Int) {
        preferencesRepository.setNumPlayers(value)
    }

    override fun setTimerEnabled(value: Boolean) {
        preferencesRepository.setTurnTimer(value)
    }

    override fun onCommanderDealerButtonClicked() {
        resetCommanderState()
    }

    open override fun onPlayerButtonAction(seatId: SeatId, action: PlayerButtonAction) {
        when (action) {
            PlayerButtonAction.IncrementLife -> updatePlayer(seatId) { player ->
                player.copy(lifeTotal = player.lifeTotal.changeBy(1))
            }
            PlayerButtonAction.DecrementLife -> updatePlayer(seatId) { player ->
                player.copy(lifeTotal = player.lifeTotal.changeBy(-1))
            }
            is PlayerButtonAction.IncrementCommanderDamage -> changeCommanderDamage(seatId, action.partner, 1)
            is PlayerButtonAction.DecrementCommanderDamage -> changeCommanderDamage(seatId, action.partner, -1)
            is PlayerButtonAction.SetMonarch -> setMonarch(if (action.monarch) seatId else null)
            is PlayerButtonAction.SetManualDeath -> {
                updateSeat(seatId) { seat ->
                    seat.copy(player = seat.player.copy(setDead = action.dead), isDead = action.dead)
                }
                _state.value = _state.value.closePlayerCustomization(seatId)
            }
            PlayerButtonAction.ToggleCommanderPartnerMode -> updatePlayer(seatId) { player ->
                player.copy(partnerMode = !player.partnerMode)
            }
            is PlayerButtonAction.ChangeCounter -> updatePlayer(seatId) { player ->
                val counterIndex = action.counter.ordinal
                player.copy(counters = player.counters.changeIntAt(counterIndex, action.delta))
            }
            is PlayerButtonAction.SetCounterActive -> updatePlayer(seatId) { player ->
                player.copy(
                    activeCounters = if (action.active) {
                        (player.activeCounters + action.counter).distinct()
                    } else {
                        player.activeCounters - action.counter
                    }
                )
            }
            PlayerButtonAction.ToggleCommanderDealer -> toggleCommanderDealer(seatId)
            PlayerButtonAction.ToggleSettings -> {
                _state.value = _state.value.openPlayerSettings(seatId)
            }
            PlayerButtonAction.PopBackStack -> {
                _state.value = _state.value.popPlayerButtonBackStack(seatId)
            }
            PlayerButtonAction.OpenCounters -> {
                _state.value = _state.value.openPlayerCounters(seatId)
            }
            PlayerButtonAction.OpenCounterSelection -> {
                _state.value = _state.value.openPlayerCounterSelection(seatId)
            }
            PlayerButtonAction.OpenCustomization -> {
                ensureCustomizationViewModel(seatId)
                _state.value = _state.value.openPlayerCustomization(seatId)
            }
            PlayerButtonAction.CloseCustomization -> {
                _state.value = _state.value.closePlayerCustomization(seatId)
            }
            PlayerButtonAction.SelectFirstPlayer,
            PlayerButtonAction.MoveTimer -> Unit
        }
    }

    override fun customizationViewModelFor(seatId: SeatId): CustomizationViewModel? {
        return customizationViewModels[seatId]
    }

    override fun savePlayerPrefs() = Unit

    override fun resetAllPrefs() = Unit

    override fun resetGameState(startingLife: Int?) {
        _state.value = _state.value
            .setAllPlayerButtonStates(PBState.NORMAL)
            .copy(middleButtonState = MiddleButtonState.DEFAULT)
    }

    override fun incrementCounter(index: Int, value: Int) {
        _state.value = _state.value.copy(counters = _state.value.counters.changeIntAt(index, value))
    }

    override fun resetCounters() {
        _state.value = _state.value.copy(counters = List(_state.value.counters.size) { 0 })
    }

    override fun toggleDayNight() {
        _state.value = _state.value.copy(
            dayNight = when (_state.value.dayNight) {
                DayNightState.NONE -> DayNightState.DAY
                DayNightState.DAY -> DayNightState.NIGHT
                DayNightState.NIGHT -> DayNightState.DAY
            }
        )
    }

    override fun setDayNight(value: DayNightState) {
        _state.value = _state.value.copy(dayNight = value)
    }

    protected open fun createCustomizationViewModel(
        seatId: SeatId,
        player: Player
    ): CustomizationViewModel {
        return CustomizationViewModel(
            initialPlayer = player,
            fileImageStore = fileImageStore,
            profileRepository = profileRepository,
            preferencesRepository = preferencesRepository,
        )
    }

    protected fun showNotification(message: String, duration: Long = 2000L) {
        notificationManager?.showNotification(message, duration)
    }

    private fun ensureCustomizationViewModel(seatId: SeatId): CustomizationViewModel {
        return customizationViewModels.getOrPut(seatId) {
            createCustomizationViewModel(seatId, requirePlayer(seatId))
        }
    }

    private fun toggleCommanderDealer(seatId: SeatId) {
        if (_state.value.players.firstOrNull { it.seatId == seatId }?.buttonState == PBState.COMMANDER_DEALER) {
            resetCommanderState()
            return
        }
        _state.value = _state.value.copy(
            players = _state.value.players.map { seat ->
                seat.copy(
                    buttonState = if (seat.seatId == seatId) PBState.COMMANDER_DEALER else PBState.COMMANDER_RECEIVER,
                    buttonBackStack = emptyList(),
                    backButtonVisible = false,
                )
            },
            middleButtonState = MiddleButtonState.COMMANDER_EXIT,
        )
    }

    private fun resetCommanderState() {
        _state.value = _state.value
            .setAllPlayerButtonStates(PBState.NORMAL)
            .copy(middleButtonState = MiddleButtonState.DEFAULT)
    }

    private fun changeCommanderDamage(receiverSeatId: SeatId, partner: Boolean, delta: Int) {
        val dealer = _state.value.players.firstOrNull { it.buttonState == PBState.COMMANDER_DEALER } ?: return
        val damageIndex = dealer.player.playerNum - 1 + if (partner) Player.MAX_PLAYERS else 0
        updatePlayer(receiverSeatId) { player ->
                player.copy(
                    lifeTotal = player.lifeTotal.changeBy(-delta),
                    commanderDamage = player.commanderDamage.changeNumberAt(damageIndex, delta)
                )
            }
        }

    private fun setMonarch(seatId: SeatId?) {
        _state.value = _state.value.copy(
            players = _state.value.players.map { seat ->
                seat.copy(player = seat.player.copy(monarch = seat.seatId == seatId))
            }
        )
    }

    private fun updatePlayer(seatId: SeatId, update: (Player) -> Player) {
        updateSeat(seatId) { seat -> seat.copy(player = update(seat.player)) }
    }

    private fun updateSeat(seatId: SeatId, update: (PlayerSeatUiState) -> PlayerSeatUiState) {
        _state.value = _state.value.copy(
            players = _state.value.players.map { seat -> if (seat.seatId == seatId) update(seat) else seat }
        )
    }

    private fun requirePlayer(seatId: SeatId): Player {
        return requireNotNull(_state.value.players.firstOrNull { it.seatId == seatId }) {
            "Missing player for ${seatId.value}"
        }.player
    }

    private fun NumberWithRecentChange.changeBy(delta: Int): NumberWithRecentChange {
        return NumberWithRecentChange(number = number + delta, recentChange = recentChange + delta)
    }

    private fun List<NumberWithRecentChange>.changeNumberAt(index: Int, delta: Int): List<NumberWithRecentChange> {
        return mapIndexed { currentIndex, value ->
            if (currentIndex == index) value.changeBy(delta) else value
        }
    }

    private fun List<Int>.changeIntAt(index: Int, delta: Int): List<Int> {
        return mapIndexed { currentIndex, value ->
            if (currentIndex == index) value + delta else value
        }
    }
}

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
