package ui.lifecounter.playerbutton

fun PBState.showsBackButton(backStackIsEmpty: Boolean): Boolean {
    return this !in setOf(
        PBState.SELECT_FIRST_PLAYER,
        PBState.COMMANDER_RECEIVER,
        PBState.COMMANDER_DEALER
    ) && !backStackIsEmpty
}

fun PBState.showsCommanderButton(): Boolean {
    return this == PBState.NORMAL || this == PBState.COMMANDER_DEALER
}

fun PBState.showsSettingsButton(): Boolean {
    return this != PBState.COMMANDER_DEALER &&
        this != PBState.COMMANDER_RECEIVER &&
        this != PBState.SELECT_FIRST_PLAYER
}

enum class PBState {
    NORMAL,
    COMMANDER_DEALER,
    COMMANDER_RECEIVER,
    SETTINGS,
    COUNTERS_VIEW,
    COUNTERS_SELECT,
    SELECT_FIRST_PLAYER
}
