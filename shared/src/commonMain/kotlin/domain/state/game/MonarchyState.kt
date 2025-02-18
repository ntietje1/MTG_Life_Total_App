package domain.state.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import model.Player

class MonarchyState(
    scope: CoroutineScope
) {
    private val _currentMonarchyState = MutableStateFlow<Int?>(null)
    val currentMonarchyState = _currentMonarchyState.asStateFlow()

    private val monarchyCallbacks = mutableMapOf<Int, (Boolean) -> Unit>()
    private var onSaveCallback: (() -> Unit)? = null
    private var observerJob: Job = scope.launch {
        currentMonarchyState.collectLatest { newMonarchyPlayerNum ->
//            if (newMonarchyPlayerNum == null) return@collectLatest
            monarchyCallbacks.forEach { (playerNum, onUpdate) ->
                if (newMonarchyPlayerNum != null && newMonarchyPlayerNum != playerNum) {
                    onUpdate(false)
                }
            }
            onSaveCallback?.invoke()
        }
    }

    fun clear() {
        observerJob.cancel()
    }

    fun attachMonarchyTracker(
        player: Player,
        onUpdate: (Boolean) -> Unit
    ) {
        monarchyCallbacks[player.playerNum] = onUpdate

        // If this player is currently the monarch, update the state
        if (player.monarch) {
            _currentMonarchyState.value = player.playerNum
        }
    }

    fun registerSaveCallback(onSave: () -> Unit) {
        onSaveCallback = onSave
    }

    fun setMonarchState(playerNum: Int?) {
        _currentMonarchyState.value = playerNum
        println("MonarchyState: New monarch is player $playerNum")
    }
}