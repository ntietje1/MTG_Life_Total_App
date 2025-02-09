package di

import app.cash.sqldelight.db.SqlDriver
import com.hypeapps.lifelinked.db.Database
import data.PlayerAdapter
import data.GameRepository
import data.GameWithPlayerAdapter

class DatabaseModule(sqlDriver: SqlDriver) {
    private val database = Database(sqlDriver)

    private val playerAdapter: PlayerAdapter = PlayerAdapter()
    private val gameWithPlayerAdapter = GameWithPlayerAdapter(playerAdapter)

    // Repositories
    val gameRepository = GameRepository(
        database = database,
        playerAdapter = playerAdapter,
        gameWithPlayersAdapter = gameWithPlayerAdapter
    )
}
