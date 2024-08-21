package ui.lifecounter

import kotlinx.serialization.Serializable
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.acorn_icon
import lifelinked.shared.generated.resources.b_icon_alt
import lifelinked.shared.generated.resources.c_icon_alt
import lifelinked.shared.generated.resources.chaos_icon
import lifelinked.shared.generated.resources.coin_icon_alt
import lifelinked.shared.generated.resources.d20_icon
import lifelinked.shared.generated.resources.energy_icon
import lifelinked.shared.generated.resources.experience_icon
import lifelinked.shared.generated.resources.g_icon_alt
import lifelinked.shared.generated.resources.heart_solid_icon
import lifelinked.shared.generated.resources.lightning_icon
import lifelinked.shared.generated.resources.planeswalker_icon
import lifelinked.shared.generated.resources.poison_icon
import lifelinked.shared.generated.resources.r_icon_alt
import lifelinked.shared.generated.resources.shield_icon
import lifelinked.shared.generated.resources.snow_icon
import lifelinked.shared.generated.resources.star_icon
import lifelinked.shared.generated.resources.sword_icon
import lifelinked.shared.generated.resources.ticket_icon
import lifelinked.shared.generated.resources.two_mana_icon
import lifelinked.shared.generated.resources.u_icon_alt
import lifelinked.shared.generated.resources.w_icon_alt
import org.jetbrains.compose.resources.DrawableResource

@Serializable
enum class CounterType(val resource: DrawableResource) {
    Poison(Res.drawable.poison_icon),
    Experience(Res.drawable.experience_icon),
    Energy(Res.drawable.energy_icon),
    CommanderTax1(Res.drawable.two_mana_icon),
    CommanderTax2(Res.drawable.two_mana_icon),
    Ticket(Res.drawable.ticket_icon),
    Acorn(Res.drawable.acorn_icon),
    WhiteMana(Res.drawable.w_icon_alt),
    BlueMana(Res.drawable.u_icon_alt),
    BlackMana(Res.drawable.b_icon_alt),
    RedMana(Res.drawable.r_icon_alt),
    GreenMana(Res.drawable.g_icon_alt),
    ColorlessMana(Res.drawable.c_icon_alt),
    SnowMana(Res.drawable.snow_icon),
    Chaos(Res.drawable.chaos_icon),
    Planeswalker(Res.drawable.planeswalker_icon),
    D20(Res.drawable.d20_icon),
    Coin(Res.drawable.coin_icon_alt),
    Bolt(Res.drawable.lightning_icon),
    Star(Res.drawable.star_icon),
    Heart(Res.drawable.heart_solid_icon),
    Shield(Res.drawable.shield_icon),
    Sword(Res.drawable.sword_icon),
}