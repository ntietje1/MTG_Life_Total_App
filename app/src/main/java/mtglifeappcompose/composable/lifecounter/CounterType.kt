package mtglifeappcompose.composable.lifecounter

import com.example.mtglifeappcompose.R

enum class CounterType(val idx: Int, val resId: Int = R.drawable.placeholder_icon) {
    Poison(0),
    Experience(1),
    Energy(2),
    CommanderTax1(3),
    CommanderTax2(4),
    Ticket(5),
    Acorn(6),
    WhiteMana(7, R.drawable.w_icon),
    BlueMana(8, R.drawable.u_icon),
    BlackMana(9, R.drawable.b_icon),
    RedMana(10, R.drawable.r_icon),
    GreenMana(11, R.drawable.g_icon),
    ColorlessMana(12, R.drawable.c_icon),
    Storm(13, R.drawable.storm_icon),
    Misc1(14),
    Misc2(15),
    Misc3(16),
    Misc4(17),
    Misc5(18),
}