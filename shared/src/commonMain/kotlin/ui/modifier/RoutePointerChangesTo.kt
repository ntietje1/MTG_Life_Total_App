package ui.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
suspend fun PointerInputScope.routePointerChangesTo(
    onDown: (PointerInputChange) -> Unit = {},
    onMove: (PointerInputChange) -> Unit = {},
    onUp: (PointerInputChange) -> Unit = {},
    onLongPress: suspend () -> Unit = {},
    countCallback: (List<PointerId>) -> Unit = {}
) {
    val activePointers = mutableMapOf<PointerId, Job>()
    awaitEachGesture {
        do {
            val event = awaitPointerEvent()
            countCallback(event.changes.map { it.id })
            event.changes.forEach { pointerInputChange ->
                when (event.type) {
                    PointerEventType.Press, PointerEventType.Enter -> {
                        if (pointerInputChange.id !in activePointers) {
                            activePointers[pointerInputChange.id] = GlobalScope.launch { onLongPress() }
                            onDown(pointerInputChange)
                        }
                    }
                    PointerEventType.Move -> onMove(pointerInputChange)
                    PointerEventType.Release, PointerEventType.Exit -> {
                        if (pointerInputChange.id in activePointers && pointerInputChange.previousPressed && !pointerInputChange.pressed) {
                            activePointers[pointerInputChange.id]?.cancel()
                            activePointers.remove(pointerInputChange.id)
                            onUp(pointerInputChange)
                        }
                    }
                }
//                pointerInputChange.consume()
            }
        } while (event.changes.any { it.pressed })
    }
}