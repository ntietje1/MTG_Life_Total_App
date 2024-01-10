package composable.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

/**
 * A modifier that rotates a composable vertically, aware of boundaries
 * @param rotation The rotation to apply
 * @return The modifier
 */
fun Modifier.rotateVertically(rotation: VerticalRotation) = then(object : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable, constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable, width: Int
    ): Int {
        return measurable.maxIntrinsicWidth(width)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable, width: Int
    ): Int {
        return measurable.maxIntrinsicWidth(width)
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable, height: Int
    ): Int {
        return measurable.minIntrinsicHeight(height)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable, height: Int
    ): Int {
        return measurable.maxIntrinsicHeight(height)
    }
}).then(rotate(rotation.value))

/**
 * Vertical rotation values
 */
enum class VerticalRotation(val value: Float) {
    CLOCKWISE(90f), COUNTER_CLOCKWISE(270f)
}