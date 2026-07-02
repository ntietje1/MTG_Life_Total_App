package domain.state.game

import kotlinx.serialization.Serializable

@Serializable
enum class CounterType {
    POISON,
    EXPERIENCE,
    ENERGY,
    COMMANDER_TAX_PRIMARY,
    COMMANDER_TAX_SECONDARY,
    TICKET,
    ACORN,
    WHITE_MANA,
    BLUE_MANA,
    BLACK_MANA,
    RED_MANA,
    GREEN_MANA,
    COLORLESS_MANA,
    SNOW_MANA,
    CHAOS,
    PLANESWALKER,
    D20,
    COIN,
    BOLT,
    STAR,
    HEART,
    SHIELD,
    SWORD
}

@Serializable
enum class TableCounterType {
    WHITE_MANA,
    BLUE_MANA,
    BLACK_MANA,
    RED_MANA,
    GREEN_MANA,
    COLORLESS_MANA,
    SNOW_MANA,
    STORM
}
