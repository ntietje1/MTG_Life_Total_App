package domain.state.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val stateModule = module {
    single { MonarchyState(CoroutineScope(Dispatchers.Main)) }
    single { PlayerLifeRecentChangeState(CoroutineScope(Dispatchers.Main)) }
}