package ui.lifecounter

enum class LifeCounterModal {
    Default,
    CoinFlip,
    CoinFlipTutorial,
    PlayerNumber,
    FourPlayerLayout,
    StartingLife,
    DiceRoll,
    Counter,
    Settings,
    Scryfall,
    PatchNotes,
    AboutMe,
    PlaneChase,
    PlanarDeck,
    PlanarTutorial
}

data class LifeCounterModalStack(
    val entries: List<LifeCounterModal> = emptyList()
) {
    val current: LifeCounterModal? get() = entries.lastOrNull()
    val canGoBack: Boolean get() = entries.size > 1

    fun open(modal: LifeCounterModal): LifeCounterModalStack {
        return copy(entries = entries + modal)
    }

    fun replace(modal: LifeCounterModal): LifeCounterModalStack {
        return copy(entries = if (entries.isEmpty()) listOf(modal) else entries.dropLast(1) + modal)
    }

    fun goBack(): LifeCounterModalStack {
        return copy(entries = entries.dropLast(1))
    }

    companion object {
        val Empty = LifeCounterModalStack()
    }
}
