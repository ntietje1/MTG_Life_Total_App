import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.value.Value
import composable.lifecounter.LifeCounterComponent
import composable.playerselect.PlayerSelectComponent
import kotlinx.serialization.Serializable
import data.SettingsManager

/**
 * The root component of the application
 */
class RootComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {
    private var firstPlayerSelect = true
    private val navigation = SlotNavigation<Configuration>()
    private val initialConfiguration: Configuration = if (SettingsManager.autoSkip && firstPlayerSelect) {
        Configuration.LifeCounterScreen
    } else {
        Configuration.PlayerSelectScreen
    }

    val childSlot: Value<ChildSlot<Configuration, Child>> = childSlot(
        source = navigation,
        childFactory = ::createChild,
        serializer = Configuration.serializer(),
        initialConfiguration = { initialConfiguration }
//        handleBackButton = true,
    )

    /**
     * Creates a child component based on the configuration
     */
    private fun createChild(
        config: Configuration,
        context: ComponentContext
    ): Child {
        return when (config) {
            Configuration.PlayerSelectScreen -> Child.PlayerSelectScreen(
                PlayerSelectComponent(
                    componentContext = context,
                    goToLifeCounterScreen = {
                        navigation.activate(Configuration.LifeCounterScreen)
                    },
                    setNumPlayers = {
                        if (firstPlayerSelect) {
                            SettingsManager.numPlayers = it
                            firstPlayerSelect = false
                        }
                    }
                )
            )

            Configuration.LifeCounterScreen -> Child.LifeCounterScreen(
                LifeCounterComponent(context,
                    goToPlayerSelectScreen = {
                        navigation.activate(Configuration.PlayerSelectScreen)
                    },
                    returnToLifeCounterScreen = {
                        navigation.activate(Configuration.LifeCounterScreen)
                    },
                    setNumPlayers = {
                        SettingsManager.numPlayers = it
                    }
                )
            )
        }
    }

    /**
     * Possible child components
     */
    sealed class Child {
        data class LifeCounterScreen(val component: LifeCounterComponent) : Child()
        data class PlayerSelectScreen(val component: PlayerSelectComponent) : Child()
    }

    /**
     * Possible child configurations of the root component
     */
    @Serializable
    sealed class Configuration {
        @Serializable
        data object PlayerSelectScreen : Configuration()

        @Serializable
        data object LifeCounterScreen : Configuration()
    }

}