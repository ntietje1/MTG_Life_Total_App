package ui.lifecounter.playerbutton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.common.Backstack
import domain.common.NumberWithRecentChange
import domain.game.timer.TimerManager
import domain.game.timer.TurnTimer
import domain.state.game.CommanderDamageState
import domain.state.game.CommanderDealerState
import domain.state.game.MonarchyState
import domain.state.game.PlayerLifeRecentChangeState
import domain.storage.ISettingsStore
import domain.system.NotificationManager
import domain.usecase.player.customization.ManagePlayerCustomizationUseCase
import domain.usecase.player.customization.SavePlayerCustomizationUseCase
import domain.usecase.player.state.ManagePlayerCounterUseCase
import domain.usecase.player.state.ManagePlayerStateUseCase
import domain.usecase.player.state.SavePlayerStateUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import model.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.CounterType

open class PlayerButtonViewModel(
    private val initialState: PlayerButtonState,
    private val settingsManager: ISettingsStore,
    private val commanderDamageState: CommanderDamageState,
    protected val notificationManager: NotificationManager,
    private val managePlayerStateUseCase: ManagePlayerStateUseCase,
    private val managePlayerCounterUseCase: ManagePlayerCounterUseCase,
    private val savePlayerStateUseCase: SavePlayerStateUseCase,
    private val savePlayerCustomizationUseCase: SavePlayerCustomizationUseCase,
    private val playerLifeRecentChangeState: PlayerLifeRecentChangeState,
    private val managePlayerCustomizationUseCase: ManagePlayerCustomizationUseCase,
    private val timerManager: TimerManager,
    private val monarchyState: MonarchyState,
) : ViewModel(), KoinComponent {
    private var _state = MutableStateFlow(initialState)
    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()

    val isDead: StateFlow<Boolean> = combine(
        settingsManager.autoKo, state
    ) { autoKo, playerState ->
        managePlayerStateUseCase.isPlayerDead(playerState.player, autoKo)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val commanderDealerState: StateFlow<CommanderDealerState> = commanderDamageState.commanderState

    private val backstack = Backstack()

    val showBackButton: StateFlow<Boolean> = combine(
        backstack.isEmpty, state
    ) { isEmpty, state ->
        state.buttonState !in listOf(
            PBState.SELECT_FIRST_PLAYER, PBState.COMMANDER_RECEIVER, PBState.COMMANDER_DEALER
        ) && !isEmpty
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var _customizationViewmodel: CustomizationViewModel? = null
    open val customizationViewmodel: CustomizationViewModel?
        get() = _customizationViewmodel

    init {
        attachObservers()
    }

    private fun attachObservers() {
        viewModelScope.launch {
            playerLifeRecentChangeState.attachLifeTracker(
                getCurrentPlayer = { state.value.player },
                onUpdate = {
                    setPlayer(state.value.player.copy(lifeTotal = it.lifeTotal))
                }
            )

            commanderDamageState.attachCommanderTrackers(
                getCurrentPlayer = { state.value.player },
                onUpdate = {
                    setPlayer(state.value.player.copy(commanderDamage = it.commanderDamage))
                }
            )

            monarchyState.attachMonarchyTracker(
                player = state.value.player,
                onUpdate = ::setMonarchy
            )
        }
    }

    private fun removeObservers() {
        playerLifeRecentChangeState.clear()
        commanderDamageState.onClear()
        monarchyState.clear()
    }

    override fun onCleared() {
        super.onCleared()
        playerLifeRecentChangeState.clear()
        commanderDamageState.onClear()
        monarchyState.clear()
    }

    fun resetPlayer(player: Player) {
        setPlayer(player)
        removeObservers()
        attachObservers()
    }

    private fun setPlayer(player: Player) {
        _state.value = state.value.copy(player = player)
        resetCustomizationMenuViewModel()
    }

    open fun incrementLife(value: Int) {
        managePlayerStateUseCase.incrementLife(state.value.player, value)
        savePlayerStateUseCase(state.value.player)
    }

    fun setTimer(timer: TurnTimer?) {
        _state.value = state.value.copy(timer = timer)
    }

    fun setFirstPlayer() {
        timerManager.handleFirstPlayerSelection(index = state.value.player.playerNum - 1)
    }

    fun setPlayerButtonState(buttonState: PBState) {
        if (buttonState == PBState.COMMANDER_RECEIVER) {
            backstack.clear()
        }

        _state.value = state.value.copy(buttonState = buttonState)
    }

    fun onMoveTimer() {
        timerManager.moveTimer()
    }

    private fun setMonarchy(value: Boolean) {
        if (value == state.value.player.monarch) return
        setPlayer(managePlayerStateUseCase.setMonarchy(state.value.player, value))
        println("Monarchy set to $value for player: ${state.value.player.playerNum}")
    }

    open fun onMonarchyButtonClicked(value: Boolean) {
        setMonarchy(value)
    }

    open fun onCommanderButtonClicked() {
        when (state.value.buttonState) {
            PBState.NORMAL -> {
                commanderDamageState.setCurrentDealer(state.value.player)
            }

            PBState.COMMANDER_DEALER -> {
                commanderDamageState.setCurrentDealer(null)
            }

            else -> {} // do nothing
        }
    }

    open fun onSettingsButtonClicked() {
        if (state.value.buttonState == PBState.NORMAL) {
            setPlayerButtonState(PBState.SETTINGS)
            backstack.push { setPlayerButtonState(PBState.NORMAL) }
        } else {
            setPlayerButtonState(PBState.NORMAL)
            backstack.clear()
        }
    }

    open fun onKOButtonClicked() {
        setPlayer(managePlayerStateUseCase.toggleSetDead(state.value.player))
        setPlayerButtonState(PBState.NORMAL)
        backstack.clear()
        savePlayerStateUseCase(state.value.player)
    }

    open fun popBackStack() {
        if (backstack.isEmpty.value) return
        backstack.pop().invoke()
    }

    open fun copyPrefs(other: Player) {
        setPlayer(managePlayerCustomizationUseCase.copy(state.value.player, other))
        resetCustomizationMenuViewModel()
    }

    private fun initCustomizationMenuViewModel() {
        _customizationViewmodel = get<CustomizationViewModel> {
            parametersOf(state.value.player)
        }
    }

    private fun resetCustomizationMenuViewModel() {
        _customizationViewmodel = null
    }

    private fun onCustomizationApply() {
        val customizationViewmodel = requireNotNull(customizationViewmodel)
        val customizedPlayer = customizationViewmodel.state.value.player
        viewModelScope.launch {
            copyPrefs(customizedPlayer.copy(imageString = null))
            delay(50)
            copyPrefs(customizedPlayer)
            initCustomizationMenuViewModel()
            savePlayerCustomizationUseCase(state.value.player)
            if (customizedPlayer.name != state.value.player.name) {
                savePlayerStateUseCase(state.value.player) // Ensure that the correct name is saved to game state
            }
        }
    }

    open fun onShowCustomizeMenu(value: Boolean) {
        if (value && customizationViewmodel == null) {
            initCustomizationMenuViewModel()
        }
        if (!value) {
            onCustomizationApply()
            setPlayerButtonState(PBState.NORMAL)
            backstack.clear()
        }
        _state.value = state.value.copy(showCustomizeMenu = value)
    }


    fun onFirstPlayerPrompt() {
        backstack.push { setPlayerButtonState(PBState.NORMAL) }
        setPlayerButtonState(PBState.SELECT_FIRST_PLAYER)
    }

    open fun onCountersButtonClicked() {
        setPlayerButtonState(PBState.COUNTERS_VIEW)
        backstack.push { setPlayerButtonState(PBState.SETTINGS) }
    }

    open fun onAddCounterButtonClicked() {
        setPlayerButtonState(PBState.COUNTERS_SELECT)
        backstack.push { setPlayerButtonState(PBState.COUNTERS_VIEW) }
    }

    fun togglePartnerMode(value: Boolean) {
        setPlayer(commanderDamageState.togglePartnerMode(state.value.player, value))
        savePlayerStateUseCase(state.value.player)
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        setPlayer(managePlayerCounterUseCase.incrementCounter(state.value.player, counterType, value))
        savePlayerStateUseCase.saveCounter(state.value.player, counterType)
    }

    fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean {
        setPlayer(managePlayerCounterUseCase.setActiveCounter(state.value.player, counterType, active))
        savePlayerStateUseCase(state.value.player)
        return state.value.player.counters.contains(counterType)
    }

    fun getCommanderDamage(partner: Boolean): NumberWithRecentChange {
        return commanderDamageState.getCommanderDamage(state.value.player, partner)
    }

    open fun incrementCommanderDamage(value: Int, partner: Boolean) {
        commanderDamageState.incrementCommanderDamage(state.value.player, value, partner)
        savePlayerStateUseCase(state.value.player)
    }
}