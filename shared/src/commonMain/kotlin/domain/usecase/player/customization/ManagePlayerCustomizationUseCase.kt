package domain.usecase.player.customization

import androidx.compose.ui.graphics.Color
import domain.storage.IImageStore
import model.Player

class ManagePlayerCustomizationUseCase(
    private val imageManager: IImageStore
) {

    fun copy(target: Player, source: Player): Player {
        return target.copy(
            imageString = source.imageString,
            color = source.color,
            textColor = source.textColor,
            name = source.name
        )
    }

    suspend fun updatePlayerImageBytes(player: Player, imageBytes: ByteArray): Player {
        val copiedUri = imageManager.copyImageToLocalStorage(imageBytes, player.name)
        return player.copy(imageString = copiedUri)
    }

    fun updatePlayerImageUri(player: Player, imageUri: String?): Player {
        return player.copy(imageString = imageUri)
    }

    fun updatePlayerBackgroundColor(player: Player, backgroundColor: Color): Player {
        return player.copy(
            color = backgroundColor ?: player.color,
        )
    }

    fun updatePlayerAccentColor(player: Player, accentColor: Color): Player {
        return player.copy(
            textColor = accentColor,
        )
    }

    fun updatePlayerName(player: Player, name: String): Player {
        return player.copy(name = name)
    }
}