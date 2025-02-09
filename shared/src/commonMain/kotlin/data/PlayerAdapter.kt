package data

import androidx.compose.ui.graphics.Color
import data.utils.SqliteListConverter
import domain.common.NumberWithRecentChange
import model.Player
import ui.lifecounter.CounterType

class PlayerAdapter {
    fun toPlayer(
        id: Long,
        name: String,
        imageString: String?,
        color: Int,
        textColor: Int,
        playerNum: Int,
        lifeTotal: Int,
        lifeTotalRecentChange: Int,
        monarch: Boolean,
        setDead: Boolean,
        partnerMode: Boolean,
        commanderDamages: String?,
        counters: String?,
        activeCounters: String?
    ): Player {
        return Player(
            id = id,
            name = name,
            imageString = imageString,
            color = Color(color),
            textColor = Color(textColor),
            playerNum = playerNum,
            lifeTotal = NumberWithRecentChange(lifeTotal, lifeTotalRecentChange),
            monarch = monarch,
            commanderDamage = parseCommanderDamages(commanderDamages),
            counters = parseCounters(counters),
            activeCounters = CounterType.entries, //TODO: to be refactored out
            setDead = setDead,
            partnerMode = partnerMode
        )
    }

    private fun parseCommanderDamages(damagesStr: String?): List<NumberWithRecentChange> {
        return SqliteListConverter.fromPairedString(
            str = damagesStr,
            defaultSize = Player.MAX_PLAYERS * 2,
            defaultValue = NumberWithRecentChange(0, 0)
        ) { pair -> NumberWithRecentChange(pair[0].toInt(), pair[1].toInt()) }
    }

    private fun parseCounters(countersStr: String?): List<Int> {
        return SqliteListConverter.fromString(
            str = countersStr,
            defaultSize = CounterType.entries.size * 2,
            defaultValue = 0
        )
    }
}
