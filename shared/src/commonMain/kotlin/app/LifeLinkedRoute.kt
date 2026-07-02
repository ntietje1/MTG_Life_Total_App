package app

sealed interface LifeLinkedRoute {
    val route: String

    data object PlayerSelect : LifeLinkedRoute {
        override val route: String = "player_select"
    }

    data object LifeCounter : LifeLinkedRoute {
        override val route: String = "life_counter"
    }

    data object Tutorial : LifeLinkedRoute {
        override val route: String = "tutorial"
    }

    data object Splash : LifeLinkedRoute {
        override val route: String = "splash"
    }
}
