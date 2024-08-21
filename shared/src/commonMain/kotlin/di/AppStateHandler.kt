package di

object AppStateHandler {
    private var _firstLifeCounterNavigation = true
    private var _allowChangeNumPlayers = true

    var firstLifeCounterNavigation: Boolean
        get() = _firstLifeCounterNavigation
        set(value) {
            _firstLifeCounterNavigation = value
        }

    var allowChangeNumPlayers: Boolean
        get() = _allowChangeNumPlayers
        set(value) {
            _allowChangeNumPlayers = value
        }
}