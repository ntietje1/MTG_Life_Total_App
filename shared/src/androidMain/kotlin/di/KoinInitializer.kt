package di

import android.content.Context
import domain.state.game.stateModule
import domain.usecase.game.gameModule
import domain.usecase.player.playerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

actual class KoinInitializer(
    private val context: Context
) {
    actual fun init() {
        startKoin {
            androidContext(context)
            androidLogger()
            modules(sharedModule, platformModule, gameModule, stateModule, playerModule)
        }
    }
}