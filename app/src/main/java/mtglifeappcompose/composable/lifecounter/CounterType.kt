package mtglifeappcompose.composable.lifecounter

import com.example.mtglifeappcompose.R

enum class CounterType(val idx: Int, val resId: Int = R.drawable.placeholder_icon) {
    Poison(0, R.drawable.poison_icon),
    Experience(1, R.drawable.experience_icon),
    Energy(2, R.drawable.energy_icon),
    CommanderTax1(3, R.drawable.two_mana_icon),
    CommanderTax2(4, R.drawable.two_mana_icon),
    Ticket(5, R.drawable.ticket_icon),
    Acorn(6, R.drawable.acorn_icon),
    WhiteMana(7, R.drawable.w_icon_alt),
    BlueMana(8, R.drawable.u_icon_alt),
    BlackMana(9, R.drawable.b_icon_alt),
    RedMana(10, R.drawable.r_icon_alt),
    GreenMana(11, R.drawable.g_icon_alt),
    ColorlessMana(12, R.drawable.c_icon_alt),
    SnowMana(13, R.drawable.snow_icon),
    Misc1(14, R.drawable.d20_icon),
    Misc2(15, R.drawable.coin_icon_alt),
    Misc3(16, R.drawable.lightning_icon),
    Misc4(17, R.drawable.star_icon),
    Misc5(18, R.drawable.heart_solid_icon),
    Misc6(19, R.drawable.shield_icon),
    Misc7(20, R.drawable.sword_icon),

}