package ui.lifecounter

import androidx.compose.ui.unit.Dp

class LifeCounterMeasurements(
    private val maxWidth: Dp,
    private val maxHeight: Dp,
    private val numPlayers: Int,
    private val alt4Layout: Boolean = true
) {

    data class ButtonPlacement(val index: Int, val width: Dp, val height: Dp, val angle: Float)

    private val offset3 = 0.37f
    private val unit3 = maxHeight * offset3

    private val offset4alt = 0.80f
    private val unit4alt = maxHeight / 3 * offset4alt

    private val offset5 = 0.14f
    private val unit5 = maxHeight * offset5

    fun middleButtonOffset(middleButtonSize: Dp): Pair<Dp, Dp> {
        val offset = when (numPlayers) {
            1 -> maxWidth/2 to maxHeight/4
            2 -> maxWidth/2  to maxHeight/2
            3 -> maxWidth / 2  to (maxHeight - unit3) - (middleButtonSize / 5)
            4 -> if (!alt4Layout) {
                maxWidth/2  to maxHeight/2
            } else {
                maxWidth/2  to (unit4alt) + (middleButtonSize / 5)
            }
            5 -> maxWidth/2  to (maxHeight / 2 - unit5)
            6 -> maxWidth/2  to maxHeight/3
            else -> throw IllegalArgumentException("invalid number of players: $numPlayers")
        }
        return offset.copy(first = offset.first - middleButtonSize/2, second = offset.second - middleButtonSize/2)
    }

    fun buttonPlacements(): List<List<ButtonPlacement>> = rows

    private val rows: List<List<ButtonPlacement>> = when (numPlayers) {
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
            listOf(
                listOf(
                    ButtonPlacement(
                        index = 0, width = maxHeight - unit3, height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 1, width = maxHeight - unit3, height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 2, width = maxWidth, height = unit3, angle = 0f
                    )
                )
            )
        }

        4 -> if (alt4Layout) {
            listOf(
                listOf(
                    ButtonPlacement(
                        index = 0, width = maxWidth, height = unit4alt, angle = 180f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 1, width = maxHeight - (unit4alt * 2), height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 2, width = maxHeight - (unit4alt * 2), height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 3, width = maxWidth, height = unit4alt, angle = 0f
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
            listOf(
                listOf(
                    ButtonPlacement(
                        index = 0, width = maxHeight / 2 - unit5, height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 1, width = maxHeight / 2 - unit5, height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 2, width = maxHeight / 2 - unit5, height = maxWidth / 2, angle = 90f
                    ), ButtonPlacement(
                        index = 3, width = maxHeight / 2 - unit5, height = maxWidth / 2, angle = 270f
                    )
                ), listOf(
                    ButtonPlacement(
                        index = 4, width = maxWidth, height = unit5 * 2, angle = 0f
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
}