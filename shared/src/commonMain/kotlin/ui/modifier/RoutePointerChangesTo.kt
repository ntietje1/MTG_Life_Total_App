package ui.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.positionChange
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@OptIn(DelicateCoroutinesApi::class)
suspend fun PointerInputScope.averagedRoutePointerChangesTo(
    onDown: (PointerInputChange) -> Unit = {  },
    onMove: (PointerInputChange, Offset) -> Unit = {  _, _ -> },
    onUp: (PointerInputChange) -> Unit = { },
    onLongPress: suspend (PointerInputChange) -> Unit = {  },
    countCallback: (List<PointerId>) -> Unit = {}
) {
    val activePointers = mutableMapOf<PointerId, Job>()
    awaitEachGesture {
        do {
            val event = awaitPointerEvent()
            val averagePosition = event.changes.map { it.position }.reduce { acc, offset -> acc + offset } / event.changes.size.toFloat()
            val averagePositionChange = event.changes.map { it.positionChange() }.reduce { acc, offset -> acc + offset } / event.changes.size.toFloat()
            countCallback(event.changes.map { it.id })
            var moveUsed = false
            event.changes.forEach { pointerInputChange ->
                val modifiedPointerInputChange = pointerInputChange.copy(
                    currentPosition = averagePosition
                )
                when (event.type) {
                    PointerEventType.Press, PointerEventType.Enter -> {
                        if (pointerInputChange.id !in activePointers) {
                            activePointers[pointerInputChange.id] = GlobalScope.launch { onLongPress(modifiedPointerInputChange) }
                            onDown(modifiedPointerInputChange)
                        }
                    }

                    PointerEventType.Move -> {
                        if (!moveUsed) {
                            onMove(modifiedPointerInputChange, averagePositionChange)
                            moveUsed = true
                        }
                    }

                    PointerEventType.Release, PointerEventType.Exit -> {
                        if (pointerInputChange.id in activePointers && pointerInputChange.previousPressed && !pointerInputChange.pressed) {
                            activePointers[pointerInputChange.id]?.cancel()
                            activePointers.remove(pointerInputChange.id)
                            onUp(modifiedPointerInputChange)
                        }
                    }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun PointerInputScope.routePointerChangesTo(
    onDown: (PointerInputChange) -> Unit = {},
    onMove: (PointerInputChange, Offset) -> Unit = { _, _ -> },
    onUp: (PointerInputChange) -> Unit = {},
    onLongPress: suspend (PointerInputChange) -> Unit = {},
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
                            activePointers[pointerInputChange.id] = GlobalScope.launch { onLongPress(pointerInputChange) }
                            onDown(pointerInputChange)
                        }
                    }

                    PointerEventType.Move -> {
                        onMove(pointerInputChange, pointerInputChange.position)
                    }

                    PointerEventType.Release, PointerEventType.Exit -> {
                        if (pointerInputChange.id in activePointers && pointerInputChange.previousPressed && !pointerInputChange.pressed) {
                            activePointers[pointerInputChange.id]?.cancel()
                            activePointers.remove(pointerInputChange.id)
                            onUp(pointerInputChange)
                        }
                    }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}