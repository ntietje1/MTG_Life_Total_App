package lifelinked.composable.lifecounter

import com.hypeapps.lifelinked.R
import kotlinx.serialization.Serializable

@Serializable
enum class CounterType(val resId: Int = R.drawable.placeholder_icon) {
    Poison(R.drawable.poison_icon),
    Experience(R.drawable.experience_icon),
    Energy(R.drawable.energy_icon),
    CommanderTax1(R.drawable.two_mana_icon),
    CommanderTax2(R.drawable.two_mana_icon),
    Ticket(R.drawable.ticket_icon),
    Acorn(R.drawable.acorn_icon),
    WhiteMana(R.drawable.w_icon_alt),
    BlueMana(R.drawable.u_icon_alt),
    BlackMana(R.drawable.b_icon_alt),
    RedMana(R.drawable.r_icon_alt),
    GreenMana(R.drawable.g_icon_alt),
    ColorlessMana(R.drawable.c_icon_alt),
    SnowMana(R.drawable.snow_icon),
    Chaos(R.drawable.chaos_icon),
    Planeswalker(R.drawable.planeswalker_icon),
    D20(R.drawable.d20_icon),
    Coin(R.drawable.coin_icon_alt),
    Bolt(R.drawable.lightning_icon),
    Star(R.drawable.star_icon),
    Heart(R.drawable.heart_solid_icon),
    Shield(R.drawable.shield_icon),
    Sword(R.drawable.sword_icon),
}