package ui.dialog.customization

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import data.Player
import di.BackHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.camera_icon
import lifelinked.shared.generated.resources.change_background_icon
import lifelinked.shared.generated.resources.download_icon
import lifelinked.shared.generated.resources.enter_icon
import lifelinked.shared.generated.resources.gif_icon
import lifelinked.shared.generated.resources.question_icon
import lifelinked.shared.generated.resources.reset_icon
import lifelinked.shared.generated.resources.search_icon
import lifelinked.shared.generated.resources.text_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import ui.SettingsButton
import ui.dialog.AnimatedGridDialog
import ui.dialog.WarningDialog
import ui.dialog.color.ColorPickerDialogContent
import ui.dialog.gif.GifDialogContent
import ui.dialog.scryfall.ScryfallDialogContent
import ui.dialog.startinglife.TextFieldWithButton
import ui.lifecounter.playerbutton.CustomizationMenuState
import ui.lifecounter.playerbutton.LifeNumber
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonBackground
import ui.lifecounter.playerbutton.PlayerButtonViewModel

@Composable
fun PlayerCustomizationDialog(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, backHandler: BackHandler = koinInject(), viewModel: PlayerButtonViewModel
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    /**
     * FEATURES:
     * 1. Change name
     * 2. Load player profile
     * 3. Reset customization
     * 3. Change accent color
     * 4. Change background
     * 4a. Change background color
     * 4b. Change background image (upload)
     * 4c. Change background image (search scryfall)
     * 4d. Change background gif
     * 4e. Change background image (draw)
     * 5. Change border
     * //TODO: add "stacked" player cards that you can toggle through once you choose that profile
     */

    val singleImagePicker = rememberImagePickerLauncher(selectionMode = SelectionMode.Single, scope = rememberCoroutineScope(), onResult = { byteArrays ->
        byteArrays.firstOrNull()?.let {
            viewModel.onFileSelected(it)
        }
    })

    if (state.showCameraWarning) {
        if (viewModel.settingsManager.cameraRollDisabled) {
            WarningDialog(title = "Info", message = "Camera roll access is disabled. Enable in settings.", optionOneEnabled = false, optionTwoEnabled = true, onDismiss = {
                viewModel.showCameraWarning(false)
            })
        } else {
            WarningDialog(title = "Warning",
                message = "This will open the camera roll. Proceed?",
                optionOneEnabled = true,
                optionTwoEnabled = true,
                optionOneMessage = "Proceed",
                optionTwoMessage = "Cancel",
                onOptionOne = {
                    viewModel.showCameraWarning(false)
                    singleImagePicker.launch()
                },
                onDismiss = {
                    viewModel.showCameraWarning(false)
                })
        }
    }

    if (state.showResetPrefsDialog) {
        WarningDialog(title = "Reset Preferences",
            message = "Are you sure you want to reset your customizations?",
            optionOneEnabled = true,
            optionTwoEnabled = true,
            optionOneMessage = "Reset",
            optionTwoMessage = "Cancel",
            onOptionOne = {
                viewModel.resetPlayerPref()
                viewModel.showResetPrefsDialog(false)
            },
            onDismiss = {
                viewModel.showResetPrefsDialog(false)
            })
    }


    AnimatedGridDialog(
        modifier = modifier, onDismiss = onDismiss, backHandler = backHandler,
        pages = listOf(
            Pair(state.customizationMenuState == CustomizationMenuState.DEFAULT) {
                // title
                // change name field
                // load player profile button
                // image background
                // preview
                BoxWithConstraints(modifier = modifier) {
                    val padding = remember(Unit) { maxHeight / 40f }
                    val titleSize = remember(Unit) { (maxWidth / 40f + maxHeight / 60f).value }
                    val textFieldHeight = remember(Unit) { maxWidth / 9f + 30.dp }
                    val focusManager = LocalFocusManager.current

                    val buttonModifier = remember(Unit) {
                        Modifier.then(
                            if (maxWidth / 3f < maxHeight / 4f) {
                                Modifier
                                    .fillMaxHeight(0.75f)
                                    .padding(maxWidth / 50f)
                            } else {
                                Modifier
                                    .fillMaxWidth(0.75f)
                                    .padding(maxHeight / 50f)
                            }
                        )
                    }

                    @Composable
                    fun FormattedSettingsButton(imageResource: DrawableResource, text: String, onPress: () -> Unit) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            SettingsButton(
                                modifier = buttonModifier.height(textFieldHeight * 1.25f)
                                    .padding(horizontal = padding / 4f).padding(bottom = padding / 2f),
                                imageVector = vectorResource(imageResource),
                                text = text,
                                shadowEnabled = false,
                                onPress = onPress
                            )
                        }
                    }

                    Column(
                        Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(padding / 2f),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextFieldWithButton(modifier = Modifier.fillMaxWidth(0.75f).height(textFieldHeight).clip(RoundedCornerShape(8))
                                .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(8)), value = state.changeNameTextField, onValueChange = {
                                viewModel.setChangeNameField(it)
                            }, label = "Name", keyboardType = KeyboardType.Text, onDone = {
                                focusManager.clearFocus()
                                viewModel.setName(state.changeNameTextField.text)
                            }, button = {
                                SettingsButton(modifier = Modifier.fillMaxSize(), imageVector = vectorResource(Res.drawable.enter_icon), //TODO: switch to a pencil icon
                                    shadowEnabled = false, onPress = {
                                        focusManager.clearFocus()
                                        viewModel.setName(state.changeNameTextField.text)
                                    })
                            })
                            Spacer(modifier = Modifier.width(padding / 4f))
                            Box(
                                modifier = Modifier.fillMaxWidth().height(textFieldHeight).clip(RoundedCornerShape(8))
                                    .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(8)).pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                viewModel.setCustomizeMenuState(CustomizationMenuState.LOAD_PLAYER)
                                                backHandler.push { viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT) }
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                SettingsButton(
                                    modifier = Modifier.height(textFieldHeight).padding(horizontal = padding / 4f).padding(bottom = padding / 2f),
                                    imageVector = vectorResource(Res.drawable.download_icon),
                                    text = "Load Profile",
                                    shadowEnabled = false,
                                    enabled = false
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(0.01f))
                        PlayerButtonPreview(
                            modifier = Modifier.fillMaxWidth(0.9f).aspectRatio(1.75f),
                            name = state.changeNameTextField.text,
                            lifeNumber = state.player.life,
                            state = state.buttonState,
                            isDead = false,
                            imageUri = viewModel.locateImage(state.player),
                            backgroundColor = state.player.color,
                            accentColor = state.player.textColor,
                        )
                        Spacer(modifier = Modifier.weight(0.1f))
                        LazyVerticalGrid(
                            modifier = Modifier.padding(horizontal = padding / 2f).wrapContentSize(),
                            columns = GridCells.Fixed(3),
                            userScrollEnabled = false,
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = Arrangement.Center,
                            content = {
                                item {
                                    FormattedSettingsButton(
                                        imageResource = Res.drawable.change_background_icon, text = "Background Color"
                                    ) {
                                        viewModel.setCustomizeMenuState(CustomizationMenuState.BACKGROUND_COLOR_PICKER)
                                        backHandler.push { viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT) }
                                    }
                                }
                                item {
                                    FormattedSettingsButton(
                                        imageResource = Res.drawable.search_icon, text = "Search Image"
                                    ) {
                                        viewModel.setCustomizeMenuState(CustomizationMenuState.SCRYFALL_SEARCH)
                                        backHandler.push { viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT) }
//                                        viewModel.showScryfallSearch(!state.showScryfallSearch)
                                    }
                                }
                                item {
                                    FormattedSettingsButton(
                                        imageResource = Res.drawable.camera_icon, text = "Upload Image"
                                    ) {
                                        viewModel.showCameraWarning(true)
                                    }
                                }
                                item {
                                    FormattedSettingsButton(
                                        imageResource = Res.drawable.text_icon, text = "Text Color"
                                    ) {
                                        viewModel.setCustomizeMenuState(CustomizationMenuState.ACCENT_COLOR_PICKER)
                                        backHandler.push { viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT) }
                                    }
                                }
                                item {
                                    if (viewModel.settingsManager.catGifButton && viewModel.settingsManager.devMode) {
                                        FormattedSettingsButton(
                                            imageResource = Res.drawable.question_icon, text = "Random Cat Gif"
                                        ) {
                                            val catGifs = listOf(
                                                "https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExbnh3NjJiYWNxZGdkaGR3eWR0NGFjczFpYmgzOXNpODY0aTRkaWNnbyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9dg/IsDjNQPc4weWPEwhWm/giphy.gif",
                                                "https://media1.tenor.com/m/Jc9jT66AJRwAAAAd/chipi-chipi-chapa-chapa.gif",
                                                "https://media1.tenor.com/m/nisaHYy8yAYAAAAd/besito-catlove.gif",
                                                "https://media1.tenor.com/m/goY0VJNhQSIAAAAd/bleh-bleh-cat.gif",
                                                "https://media1.tenor.com/m/s50cn0tfWewAAAAC/cat.gif",
                                                "https://media1.tenor.com/m/UyXyHDmPBOcAAAAC/cat-stare-stare.gif",
                                                "https://media1.tenor.com/m/8oWF4zMAmQgAAAAd/cat-funny.gif",
                                                "https://media1.tenor.com/m/2If2O7HO1CYAAAAC/cat-staring-at-camera-fr.gif",
                                                "https://media1.tenor.com/m/Dm4Ahmoh3nwAAAAd/cat-awful.gif",
                                                "https://media1.tenor.com/m/ZuXnTDxIbjQAAAAC/shocked-shocked-cat.gif",
                                                "https://media1.tenor.com/m/OUehVPHGpQ8AAAAd/cat-cat-lick.gif"
                                            )
                                            viewModel.setImageUri(
                                                if (viewModel.state.value.player.imageString != null && viewModel.state.value.player.imageString in catGifs) catGifs[catGifs.indexOf(
                                                    viewModel.state.value.player.imageString
                                                ).plus(1).rem(catGifs.size)]
                                                else catGifs.random()
                                            )
                                        }
                                    }
                                }
                                item {
                                    FormattedSettingsButton(
                                        imageResource = Res.drawable.reset_icon,
                                        text = "Reset",
                                    ) {
                                        viewModel.showResetPrefsDialog(true)
                                    }
                                }
                                item {
                                    FormattedSettingsButton(
                                        imageResource = Res.drawable.gif_icon,
                                        text = "Gif",
                                    ) {
                                        viewModel.setCustomizeMenuState(CustomizationMenuState.GIF_SEARCH)
                                        backHandler.push { viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT) }
                                    }
                                }
                            })
                        Spacer(modifier = Modifier.weight(0.15f))
                    }
                }
            }, Pair(state.customizationMenuState == CustomizationMenuState.LOAD_PLAYER) {
                val playerList = remember {
                    mutableStateListOf<Player>().apply {
                        addAll(viewModel.settingsManager.loadPlayerPrefs().filter { !it.isDefaultOrEmptyName() })
                    }
                }
                LoadPlayerDialogContent(
                    playerList = playerList,
                    locateImage = { viewModel.locateImage(it) },
                    onPlayerSelected = { player ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.copyPrefs(player)
                    backHandler.pop()
                }, onPlayerDeleted = { player ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    playerList.remove(player)
                    viewModel.settingsManager.deletePlayerPref(player)
                })
            }, Pair(state.customizationMenuState == CustomizationMenuState.SCRYFALL_SEARCH) {
                val scryfallBackStack = remember { mutableListOf<String>("Main") }
                ScryfallDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    addToBackStack = { label, block ->
                        backHandler.push(label, block)
                        scryfallBackStack.add(label)
                    },
                    selectButtonEnabled = true,
                    printingsButtonEnabled = true,
                    rulingsButtonEnabled = false,
                    onImageSelected = {
                        viewModel.setImageUri(it)
                        while(scryfallBackStack.isNotEmpty()) {
                            backHandler.pop()
                            scryfallBackStack.removeLast()
                        }
//                        backHandler.pop()
//                        backHandler.pop()
                    }
                )
            }, Pair(state.customizationMenuState == CustomizationMenuState.BACKGROUND_COLOR_PICKER) {
                ColorPickerDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    title = "Background Color",
                    initialColor = state.player.color,
                    setColor = { viewModel.onChangeBackgroundColor(it) },
                    onDone = {
                        backHandler.pop()
                    }
                )
            }, Pair(state.customizationMenuState == CustomizationMenuState.ACCENT_COLOR_PICKER) {
                ColorPickerDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    title = "Accent Color",
                    initialColor = state.player.textColor,
                    setColor = { viewModel.onChangeTextColor(it) },
                    onDone = {
                        backHandler.pop()
                    }
                )
            }, Pair(state.customizationMenuState == CustomizationMenuState.GIF_SEARCH) {
               GifDialogContent(
                   modifier = Modifier.fillMaxSize(),
                   onGifSelected = {
                       viewModel.viewModelScope.launch {
                           viewModel.setImageUri(null)
                           backHandler.pop()
                           delay(100)
                           viewModel.setImageUri(it)
                       }
                   }
               )
            }
        )
    )
}

@Composable
fun PlayerButtonPreview(
    modifier: Modifier = Modifier,
    name: String,
    state: PBState,
    lifeNumber: Int,
    isDead: Boolean,
    imageUri: String?,
    backgroundColor: Color,
    accentColor: Color,

    ) {
    BoxWithConstraints(
        modifier = modifier.clip(RoundedCornerShape(12)),
    ) {
        val padding = remember { maxHeight / 6f }
        PlayerButtonBackground(
            state = state,
            imageUri = imageUri,
            color = backgroundColor,
            isDead = isDead,
        )
        LifeNumber(
            modifier = Modifier.fillMaxHeight().fillMaxWidth().padding(bottom = padding), largeText = lifeNumber.toString(), name = name, textColor = accentColor, recentChangeText = ""
        )
    }
}