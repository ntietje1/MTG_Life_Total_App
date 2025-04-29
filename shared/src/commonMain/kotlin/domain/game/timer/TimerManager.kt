package domain.game.timer

import data.GameRepository
import domain.game.AttachableFlowManager
import domain.storage.ISettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import kotlin.coroutines.coroutineContext

/**
 * Contains all LifeCounterViewModel timer related logic
 */
class TimerManager(
    private val settingsManager: ISettingsManager,
    private val repository: GameRepository,
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {

    private var observerJob: Job? = null
    private val observerRegistered: Boolean
        get() = observerJob != null

    private val gameTimer: GameTimer = GameTimer(loadCurrentTimerState())
    private var timerJob: Job? = null

    override fun detach() {
        super.detach()
        stopTimerLoop()
        stopObserver()
    }

    private fun loadCurrentTimerState(): GameTimerState {
        val currentGameId = requireNotNull(settingsManager.currentGameId.value)
        val savedTimerState = settingsManager.savedTimerState.value
        println("SAVED TIMER STATE PREREQ: CurrentGameID: $currentGameId, SavedTimerState: ${savedTimerState?.gameId}")
        return if (savedTimerState == null || savedTimerState.gameId != currentGameId) {
            println("CREATING NEW TIMER STATE")
            GameTimerState(gameId = currentGameId)
        } else {
            println("USING PREVIOUS SAVED TIMER STATE: first player: ${savedTimerState.firstPlayer}")
            savedTimerState
        }
    }

    fun moveTimer() {
        val previousGameTimerLength = requireNotNull(gameTimer.timerState.value.turnTimer).seconds
        val previousPlayerIndex = requireNotNull(gameTimer.timerState.value.activePlayerIndex)
        val previousTurnNumber = requireNotNull(gameTimer.timerState.value.turnTimer).turn
        val currentGameId = requireNotNull(settingsManager.currentGameId.value)
        val previousPlayerId = requireAttached().value[previousPlayerIndex].state.value.player.id
        gameTimer.moveTimer()
        updateViewModelsWithTimer()
        saveTimerState()
        val now = Clock.System.now().toEpochMilliseconds()
        repository.insertTurn(
            gameId = currentGameId,
            playerId = previousPlayerId,
            turnNumber = previousTurnNumber,
            startTimeStamp = now - previousGameTimerLength,
            endTimeStamp = now,
        )
    }

    fun handleFirstPlayerSelection(firstPlayerId: Long) {
        val playerIndex = requireAttached().value.indexOfFirst { it.state.value.player.id == firstPlayerId }
        clearFirstPlayerSelectionState()
        gameTimer.setFirstPlayer(playerIndex)
        gameTimer.setTimerEnabled(true)
        updateViewModelsWithTimer()
        saveTimerState()
        repository.insertTimer(
            gameId = settingsManager.currentGameId.value!!,
            firstPlayerId = firstPlayerId
        )
    }

    private fun clearFirstPlayerSelectionState() {
        val playerButtonViewModels = requireAttached().value
        playerButtonViewModels.forEach { viewModel ->
            if (viewModel.state.value.buttonState == PBState.SELECT_FIRST_PLAYER) {
                viewModel.popBackStack()
            }
        }
    }

    suspend fun onTimerEnabledChange(timerEnabled: Boolean) {
        //TODO: save here
        gameTimer.setTimerEnabled(timerEnabled)
        if (timerEnabled) {
            initializeGameTimer()
            if (gameTimer.timerState.value.firstPlayer == null) {
                promptForFirstPlayer()
            }
            startTimerLoop()
        } else {
            stopTimerLoop()
//            reset()
        }
        updateViewModelsWithTimer()
    }

    fun reset() { //TODO: this may not be necessary any more
        clearFirstPlayerSelectionState()
        gameTimer.reset()
//        initializeGameTimer()
        if (settingsManager.turnTimer.value) {
            promptForFirstPlayer()
        }
    }

    private fun promptForFirstPlayer() {
        if (gameTimer.timerState.value.firstPlayer != null) {
            println("WARNING: First player already selected")
            return
        }
        val playerButtonViewModels = requireAttached().value
        playerButtonViewModels.forEach { it.onFirstPlayerPrompt() }
        if (settingsManager.defaultNumPlayers.value == 1) {
            handleFirstPlayerSelection(0)
        }
//        initializeGameTimer()
    }

    private fun initializeGameTimer() {
        val playerButtonViewModels = requireAttached().value
        gameTimer.initialize(
            playerCount = settingsManager.defaultNumPlayers.value,
            deadCheck = { index -> playerButtonViewModels[index].isDead.value },
            initialState = loadCurrentTimerState()
        )
    }

    private fun updateViewModelsWithTimer() {
        val playerButtonViewModels = requireAttached().value
        val timerState = gameTimer.timerState.value
        playerButtonViewModels.forEachIndexed { index, viewModel ->
            val shouldShowTimer = (index == timerState.activePlayerIndex)
            viewModel.setTimer(if (shouldShowTimer) timerState.turnTimer else null)
        }
    }

    private suspend fun startTimerLoop() {
        stopTimerLoop()
        timerJob = CoroutineScope(coroutineContext).launch {
            while (true) {
                gameTimer.tick()
                updateViewModelsWithTimer()
                delay(1000L)
                saveTimerState()
            }
        }
    }

    suspend fun registerTimerEnabledObserver() {
        // Observe timer enabled changes
        requireAttached()
//        initializeGameTimer()
        observerJob = CoroutineScope(coroutineContext).launch {
            settingsManager.turnTimer.collect { turnTimerEnabled ->
                onTimerEnabledChange(turnTimerEnabled)
            }
        }
    }

    private fun stopObserver() {
        observerJob?.cancel()
        observerJob = null
    }

    private fun stopTimerLoop() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveTimerState() {
        settingsManager.setSavedTimerState(gameTimer.timerState.value)
    }
} 