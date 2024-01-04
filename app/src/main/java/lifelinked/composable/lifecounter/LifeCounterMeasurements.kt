package lifelinked.composable.lifecounter

import androidx.compose.ui.unit.Dp


class LifeCounterMeasurements(maxWidth: Dp, maxHeight: Dp, numPlayers: Int, alt4Layout: Boolean = true) {

    data class ButtonPlacement(val index: Int, val width: Dp, val height: Dp, val angle: Float)

    private val middleButtonOffset: Float = when (numPlayers) {
        1 -> 0.065f
        2 -> 0.5f
        3 -> 0.615f
        4 -> if (alt4Layout) {
            0.264f
        } else {
            0.5f
        }

        5 -> 0.364f
        6 -> 0.323f
        else -> throw IllegalArgumentException("invalid number of players: $numPlayers")
    }

    val rows: List<List<ButtonPlacement>> = when (numPlayers) {
        1 -> listOf(
            listOf(
                ButtonPlacement(
                    index = 0, width = maxHeight, height = maxWidth, angle = 90f
                )
            )
        )

        2 -> listOf(
            listOf(
                ButtonPlacement(
                    index = 0, width = maxWidth, height = maxHeight / 2, angle = 180f
                ),
            ), listOf(
                ButtonPlacement(
                    index = 1, width = maxWidth, height = maxHeight / 2, angle = 0f
                )
            )
        )

        3 -> {
            val offset3 = 0.8f
            val unit = maxWidth * offset3
            listOf(
                listOf(
                    ButtonPlacement(
                        index = 0, width = maxHeight - unit, height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 1, width = maxHeight - unit, height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 2, width = maxWidth, height = unit, angle = 0f
                    )
                )
            )
        }

        4 -> if (alt4Layout) {
            val offset4alt = 0.80f
            val unit = maxHeight / 3 * offset4alt
            listOf(
                listOf(
                    ButtonPlacement(
                        index = 0, width = maxWidth, height = unit, angle = 180f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 1, width = maxHeight - (unit * 2), height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 2, width = maxHeight - (unit * 2), height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 3, width = maxWidth, height = unit, angle = 0f
                    )
                )
            )
        } else {
            listOf(
                listOf(
                    ButtonPlacement(
                        index = 0, width = maxHeight / 2, height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 1, width = maxHeight / 2, height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 2, width = maxHeight / 2, height = maxWidth / 2, angle = 90f
                    ),
                    ButtonPlacement(
                        index = 3, width = maxHeight / 2, height = maxWidth / 2, angle = 270f
                    ),
                )
            )
        }

        5 -> {
            val offset5 = 0.265f
            val unit = maxWidth * offset5
            listOf(
                listOf(
                    ButtonPlacement(
                        index = 0, width = maxHeight / 2 - unit, height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 1, width = maxHeight / 2 - unit, height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 2, width = maxHeight / 2 - unit, height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 3, width = maxHeight / 2 - unit, height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 4, width = maxWidth, height = unit * 2, angle = 0f
                    )
                )
            )
        }

        6 -> listOf(
            listOf(
                ButtonPlacement(
                    index = 0, width = maxHeight / 3, height = maxWidth / 2, angle = 90f
                ), ButtonPlacement(
                    index = 1, width = maxHeight / 3, height = maxWidth / 2, angle = 270f
                )
            ), listOf(
                ButtonPlacement(
                    index = 2, width = maxHeight / 3, height = maxWidth / 2, angle = 90f
                ), ButtonPlacement(
                    index = 3, width = maxHeight / 3, height = maxWidth / 2, angle = 270f
                )
            ), listOf(
                ButtonPlacement(
                    index = 4, width = maxHeight / 3, height = maxWidth / 2, angle = 90f
                ), ButtonPlacement(
                    index = 5, width = maxHeight / 3, height = maxWidth / 2, angle = 270f
                )
            )
        )

        else -> throw IllegalArgumentException("invalid number of players")
    }

    fun middleOffset(): Float {
        return middleButtonOffset
    }

}