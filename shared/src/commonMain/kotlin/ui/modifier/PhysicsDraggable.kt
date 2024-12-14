import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ui.modifier.averagedRoutePointerChangesTo
import kotlin.math.roundToInt

@Composable
fun PhysicsDraggable(
    modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    val springSpec = SpringSpec<Float>(
        dampingRatio = 0.75f, stiffness = 250f
    )
    val liftSpec = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium
    )

    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    var numPointers by remember { mutableStateOf(0) }

    Box(modifier = modifier.offset {
        IntOffset(
            offsetX.value.roundToInt(), offsetY.value.roundToInt()
        )
    }.graphicsLayer(
        rotationZ = rotation.value, scaleX = scale.value, scaleY = scale.value
    ).pointerInput(Unit) {
        averagedRoutePointerChangesTo(countCallback = {
            numPointers = it.size
        }, onDown = {
            scope.launch {
                coroutineScope {
                    launch { offsetX.stop() }
                    launch { offsetY.stop() }
                    launch { rotation.stop() }
                    launch {
                        scale.animateTo(
                            targetValue = 1f + 0.05f, animationSpec = liftSpec
                        )
                    }
                }
            }
        }, onMove = { _, positionChange ->
            scope.launch {
                coroutineScope {
                    launch {
                        offsetX.snapTo(offsetX.value + positionChange.x)
                    }
                    launch {
                        offsetY.snapTo(offsetY.value + positionChange.y)
                    }
                }
            }
        }, onUp = {
            if (numPointers == 1) {
                scope.launch {
                    coroutineScope {
                        launch {
                            offsetX.animateTo(
                                targetValue = 0f, animationSpec = springSpec
                            )
                        }
                        launch {
                            offsetY.animateTo(
                                targetValue = 0f, animationSpec = springSpec
                            )
                        }
                        launch {
                            rotation.animateTo(
                                targetValue = 0f, animationSpec = springSpec
                            )
                        }
                        launch {
                            scale.animateTo(
                                targetValue = 1f, animationSpec = springSpec
                            )
                        }
                    }
                }
            }
        })
    }) {
        content()
    }
}