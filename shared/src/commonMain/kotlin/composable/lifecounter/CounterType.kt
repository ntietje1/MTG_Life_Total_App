package composable.lifecounter

import kotlinx.serialization.Serializable

/**
 * Enum class for the different types of counters
 * @param resId The resource id for the counter icon
 */
@Serializable
enum class CounterType(val resId: String) {
    Poison("poison_icon.xml"),
    Experience("experience_icon.xml"),
    Energy("energy_icon.xml"),
    CommanderTax1("two_mana_icon.xml"),
    CommanderTax2("two_mana_icon.xml"),
    Ticket("ticket_icon.xml"),
    Acorn("acorn_icon.xml"),
    WhiteMana("w_icon_alt.xml"),
    BlueMana("u_icon_alt.xml"),
    BlackMana("b_icon_alt.xml"),
    RedMana("r_icon_alt.xml"),
    GreenMana("g_icon_alt.xml"),
    ColorlessMana("c_icon_alt.xml"),
    SnowMana("snow_icon.xml"),
    Chaos("chaos_icon.xml"),
    Planeswalker("planeswalker_icon.xml"),
    D20("d20_icon.xml"),
    Coin("coin_icon_alt.xml"),
    Bolt("lightning_icon.xml"),
    Star("star_icon.xml"),
    Heart("heart_solid_icon.xml"),
    Shield("shield_icon.xml"),
    Sword("sword_icon.xml"),
}