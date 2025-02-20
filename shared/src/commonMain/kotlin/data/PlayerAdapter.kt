package data

import androidx.compose.ui.graphics.Color
import com.hypeapps.lifelinked.db.GetCommanderDamages
import com.hypeapps.lifelinked.db.GetCounters
import domain.common.NumberWithRecentChange
import model.Player
import ui.lifecounter.CounterType

class PlayerAdapter {
    fun toPlayer(
        gameId: Long,
        name: String,
        imageString: String?,
        color: Int,
        textColor: Int,
        playerNum: Int,
        lifeTotal: Int,
        monarch: Boolean,
        setDead: Boolean,
        partnerMode: Boolean,
        commanderDamages: List<GetCommanderDamages>,
        counters: List<GetCounters>
    ): Player {
        return Player(
            gameId = gameId,
            name = name,
            imageString = imageString,
            color = Color(color),
            textColor = Color(textColor),
            playerNum = playerNum,
            lifeTotal = NumberWithRecentChange(lifeTotal, 0),
            monarch = monarch,
            commanderDamage = commanderDamages.map { NumberWithRecentChange(it.damage.toInt(), 0) },
            counters = counters.associate {  CounterType.valueOf(it.counter_type) to NumberWithRecentChange(it.counter_value.toInt(), 0) },
            setDead = setDead,
            partnerMode = partnerMode
        )
    }
}
