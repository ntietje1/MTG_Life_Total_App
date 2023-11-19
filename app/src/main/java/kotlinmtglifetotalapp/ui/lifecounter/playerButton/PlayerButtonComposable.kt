package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinmtglifetotalapp.R
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.PlayerButtonStateManager.currentDealer
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.PlayerButtonStateManager.getDamageToPlayer
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.PlayerButtonStateManager.setDealer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//enum class PlayerButtonState {
//    NORMAL, COMMANDER_DEALER, COMMANDER_RECEIVER, SETTINGS
//}


object PlayerButtonStateManager {
    private val buttonStates = arrayListOf<MutableState<PlayerButtonState>>()

    fun registerButtonState(state: MutableState<PlayerButtonState>) {
        buttonStates.add(state)
    }

    fun updateAllStates(state: PlayerButtonState) {
        buttonStates.forEach { it.value = state }
    }

    var currentDealer: Player? = null

    fun setDealer(dealer: Player) {
        currentDealer = dealer
    }

    fun addDamageToPlayer(receiver: Int, damage: Int) {
        val dealer = currentDealer!!
        dealer.commanderDamage[receiver] = dealer.commanderDamage[receiver] + damage
        println("Added $damage damage to player $receiver : ${dealer.commanderDamage[receiver]}")
    }

    fun getDamageToPlayer(receiver: Int): Int {
        val dealer = currentDealer ?: return 0
        val damage = dealer.commanderDamage[receiver]
        println("Getting damage ($damage) to player $receiver")
        return damage
    }
}


@Preview
@Composable
fun ExampleScreen() {
    Column() {
        PlayerButton(
            player = Player.generatePlayer(),
            initialState = PlayerButtonState.NORMAL
        )
        PlayerButton(
            player = Player.generatePlayer(),
            initialState = PlayerButtonState.NORMAL
        )
    }
}

@Composable
fun PlayerButton(
    player: Player,
    initialState: PlayerButtonState,
    width: Dp = 400.dp,
    height: Dp = 300.dp
) {

    val state = remember { mutableStateOf(initialState) }
    val life = remember { mutableIntStateOf(player.life) }

    PlayerButtonStateManager.registerButtonState(state)
    val id = player.playerNum

    val commanderDamage = remember { mutableIntStateOf(getDamageToPlayer(id)) }

    LaunchedEffect(life.intValue) {
        if (state.value == PlayerButtonState.COMMANDER_RECEIVER) {
            commanderDamage.intValue = getDamageToPlayer(id)
        }
    }

    val visibleColor = when (state.value) {
        PlayerButtonState.NORMAL -> {
            if (life.intValue <= 0) {
                Color(player.playerColor.desaturateColor(0.9f))
            } else {
                Color(player.playerColor)
            }
        }
        PlayerButtonState.COMMANDER_RECEIVER -> Color.DarkGray
        PlayerButtonState.COMMANDER_DEALER -> Color(player.playerColor.desaturateColor().darkenColor())
        else -> Color(player.playerColor.desaturateColor(0.8f).darkenColor(0.8f))
    }

    fun commanderButtonOnClick() {
        state.value = when (state.value) {
            PlayerButtonState.NORMAL -> {
                PlayerButtonStateManager.updateAllStates(PlayerButtonState.COMMANDER_RECEIVER)
                setDealer(player)
                PlayerButtonState.COMMANDER_DEALER
            }
            PlayerButtonState.COMMANDER_DEALER -> {
                PlayerButtonStateManager.updateAllStates(PlayerButtonState.NORMAL)
                PlayerButtonState.NORMAL
            }
            else -> throw Exception("Invalid state for commanderButtonOnClick")
        }
    }

    fun settingsButtonOnClick() {
        state.value = when (state.value) {
            PlayerButtonState.NORMAL -> PlayerButtonState.SETTINGS
            PlayerButtonState.SETTINGS -> PlayerButtonState.NORMAL
            else -> throw Exception("Invalid state for settingsButtonOnClick")
        }
    }

    fun onIncrementLife() {
        when (state.value) {
            PlayerButtonState.NORMAL -> life.intValue++
            PlayerButtonState.COMMANDER_RECEIVER -> {
                PlayerButtonStateManager.addDamageToPlayer(id, 1)
                life.intValue--
            }
            else -> {}
        }
    }

    fun onDecrementLife() {
        when (state.value) {
            PlayerButtonState.NORMAL -> life.intValue--
            PlayerButtonState.COMMANDER_RECEIVER -> {
                PlayerButtonStateManager.addDamageToPlayer(id, -1)
                life.intValue++
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(Color.Transparent)
            .padding(5.dp)
            .clip(RoundedCornerShape(30.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Overlay LifeChangeButtons on top of PlayerInfo
        LifeChangeButtons(
            onIncrementLife = { onIncrementLife() },
            onDecrementLife = { onDecrementLife() },
            color = visibleColor
        )



        // Overlay PlayerInfo in the center
        when (state.value) {
            PlayerButtonState.NORMAL -> PlayerInfo(playerName = player.name, life = life.intValue, state = state.value)
            PlayerButtonState.COMMANDER_RECEIVER -> PlayerInfo(playerName = player.name, life = commanderDamage.intValue, state=state.value)
            PlayerButtonState.COMMANDER_DEALER -> Text(
                text = "Deal damage with your commander",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            PlayerButtonState.SETTINGS -> SettingsMenu(
                onColorButtonClick = { /* Handle color button click */ },
                onChangeNameButtonClick = { /* Handle change name button click */ },
                onMonarchyButtonClick = { /* Handle monarchy button click */ },
                onSavePlayerButtonClick = { /* Handle save player button click */ },
                onLoadPlayerButtonClick = { /* Handle load player button click */ },
                onImageButtonClick = { /* Handle image button click */ }
            )
        }

        // Overlay PlayerButtonStateIcon on top-right corner
        PlayerButtonStateButtons(
            state = state.value,
            commanderButtonOnClick = { commanderButtonOnClick() },
            settingsButtonOnClick = { settingsButtonOnClick() }
        )

    }
}

@Composable
fun PlayerButtonStateButtons(
    state: PlayerButtonState,
    commanderButtonOnClick: () -> Unit,
    settingsButtonOnClick: () -> Unit
) {
    val commanderButtonVisible = state != PlayerButtonState.COMMANDER_RECEIVER && state != PlayerButtonState.SETTINGS
    val settingsButtonVisible = state != PlayerButtonState.COMMANDER_DEALER && state != PlayerButtonState.COMMANDER_RECEIVER

    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

                IconButton(
                    onClick = { commanderButtonOnClick() },
                    modifier = Modifier
                        .size(50.dp)
                        .padding(bottom = 2.5.dp, start = 2.5.dp)
                        .clip(RoundedCornerShape(0.dp))
                        .background(Color.Transparent)
                        .alpha(if (commanderButtonVisible) 1f else 0f),
                    enabled = commanderButtonVisible,
                    content = {
                        Icon(
                            modifier = Modifier.fillMaxSize(0.75f),
                            imageVector = ImageVector.vectorResource(id = R.drawable.commander_solid_icon),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )

                IconButton(
                    onClick = { settingsButtonOnClick() },
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Transparent)
                        .alpha(if (settingsButtonVisible) 1f else 0f),
                    enabled = settingsButtonVisible,
                    content = {
                        Icon(
                            modifier = Modifier.fillMaxSize(0.85f),
                            imageVector = ImageVector.vectorResource(id = R.drawable.settings_solid_icon),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )

        }
    }
}


@Composable
fun PlayerInfo(playerName: String, life: Int, state: PlayerButtonState) {
    val iconID = when (state) {
        PlayerButtonState.NORMAL -> R.drawable.heart_solid_icon
        PlayerButtonState.COMMANDER_DEALER -> R.drawable.transparent
        PlayerButtonState.COMMANDER_RECEIVER -> R.drawable.commander_solid_icon
        PlayerButtonState.SETTINGS -> R.drawable.transparent
        else -> R.drawable.transparent
    }

    Box() {

        Text(
            text = life.toString(),
            color = Color.White,
            fontSize = 150.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text(text = playerName, color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(150.dp))
            Icon(
                painter = painterResource(iconID),
                contentDescription = null,
                tint = Color.White
            )
        }
    }

}

@Composable
fun SettingsMenu(
    onColorButtonClick: () -> Unit,
    onChangeNameButtonClick: () -> Unit,
    onMonarchyButtonClick: () -> Unit,
    onSavePlayerButtonClick: () -> Unit,
    onLoadPlayerButtonClick: () -> Unit,
    onImageButtonClick: () -> Unit
) {
    val size = 110.dp
    LazyHorizontalGrid(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(horizontal = 50.dp, vertical = 30.dp),
        rows = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    )
    {
        item {
            SettingsButton(
                imageResource = painterResource(R.drawable.color_picker_icon),
                text = "Set Color",
                onClick = onColorButtonClick,
                size = size,
                color = Color.Transparent
            )
        }

        item {
            SettingsButton(
                imageResource = painterResource(R.drawable.change_name_icon),
                text = "Change Name",
                onClick = onChangeNameButtonClick,
                size = size,
                color = Color.Transparent
            )
        }
        item {
            SettingsButton(
                imageResource = painterResource(R.drawable.monarchy_icon),
                text = "Monarch",
                onClick = onMonarchyButtonClick,
                size = size,
                color = Color.Transparent
            )
        }
        item {
            SettingsButton(
                imageResource = painterResource(R.drawable.upload_icon),
                text = "Save Player",
                onClick = onSavePlayerButtonClick,
                size = size,
                color = Color.Transparent
            )
        }
        item {
            SettingsButton(
                imageResource = painterResource(R.drawable.download_icon),
                text = "Load Player",
                onClick = onLoadPlayerButtonClick,
                size = size,
                color = Color.Transparent
            )
        }
        item {
            SettingsButton(
                imageResource = painterResource(R.drawable.change_background_icon),
                text = "Set Background",
                onClick = onImageButtonClick,
                size = size,
                color = Color.Transparent
            )
        }
    }
}


fun Modifier.repeatingClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    maxDelayMillis: Long = 1000,
    minDelayMillis: Long = 5,
    delayDecayFactor: Float = .20f,
    onClick: () -> Unit
): Modifier = composed {

    val currentClickListener by rememberUpdatedState(onClick)
    val isEnabled by rememberUpdatedState(enabled)

    pointerInput(interactionSource, isEnabled) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val job = launch {
                    var currentDelayMillis = maxDelayMillis
                    while (isEnabled && down.pressed) {
                        currentClickListener()
                        delay(currentDelayMillis)
                        val nextMillis =
                            currentDelayMillis - (currentDelayMillis * delayDecayFactor)
                        currentDelayMillis = nextMillis.toLong().coerceAtLeast(minDelayMillis)
                    }
                }
                waitForUpOrCancellation()
                job.cancel()
            }
        }
    }
}

@Composable
fun LifeChangeButtons(
    onIncrementLife: () -> Unit,
    onDecrementLife: () -> Unit,
    color: Color
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = { onIncrementLife() },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .background(color)
                .then(
                    Modifier.repeatingClickable(
                        interactionSource = interactionSource,
                        enabled = true,
                        onClick = { onIncrementLife() }
                    )
                )
        ) {
            // Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null)
        }
        IconButton(
            onClick = { onDecrementLife() },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(
                    Color(
                        color
                            .toArgb()
                            .darkenColor(0.97f)
                    )
                )
                .then(
                    Modifier.repeatingClickable(
                        interactionSource = interactionSource,
                        enabled = true,
                        onClick = { onDecrementLife() }
                    )
                )
        ) {
            // Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
    }
}
