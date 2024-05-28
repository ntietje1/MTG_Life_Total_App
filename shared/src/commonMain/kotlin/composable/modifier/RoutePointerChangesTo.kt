package composable.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope

suspend fun PointerInputScope.routePointerChangesTo(
    onDown: (PointerInputChange) -> Unit = {}, onMove: (PointerInputChange) -> Unit = {}, onUp: (PointerInputChange) -> Unit = {}, countCallback: (List<PointerId>) -> Unit = {}
) {
    val activePointers = mutableSetOf<PointerId>()
    awaitEachGesture {
        do {
            val event = awaitPointerEvent()
            countCallback(event.changes.map { it.id })
            event.changes.forEach { pointerInputChange ->
                when (event.type) {
                    PointerEventType.Press, PointerEventType.Enter -> {
                        if (pointerInputChange.id !in activePointers) {
                            activePointers.add(pointerInputChange.id)
                            onDown(pointerInputChange)
                        }
                    }

                    PointerEventType.Move -> onMove(pointerInputChange)
                    PointerEventType.Release, PointerEventType.Exit -> {
                        if (pointerInputChange.id in activePointers && pointerInputChange.previousPressed && !pointerInputChange.pressed) {
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