package ui.dialog.customization

import PhysicsDraggable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import di.BackHandler
import domain.common.NumberWithRecentChange
import domain.system.NotificationManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.camera_icon
import lifelinked.shared.generated.resources.color_picker_icon
import lifelinked.shared.generated.resources.download_icon
import lifelinked.shared.generated.resources.gif_icon
import lifelinked.shared.generated.resources.pencil_icon
import lifelinked.shared.generated.resources.reset_icon
import lifelinked.shared.generated.resources.search_icon
import lifelinked.shared.generated.resources.text_icon
import model.Player
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.LocalDimensions
import theme.halfAlpha
import ui.components.SettingsButton
import ui.components.TextFieldWithButton
import ui.dialog.AnimatedGridDialog
import ui.dialog.WarningDialog
import ui.dialog.color.ColorDialogViewModel
import ui.dialog.color.ColorPickerDialogContent
import ui.dialog.gif.GifDialogContent
import ui.dialog.scryfall.ScryfallDialogContent
import ui.lifecounter.playerbutton.LifeNumber
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonBackground

@Composable
fun PlayerCustomizationDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: CustomizationViewModel,
    backHandler: BackHandler = koinInject(),
    notificationManager: NotificationManager = koinInject(),
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

    LaunchedEffect(Unit) {
        viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT)
    }

    val singleImagePicker = rememberImagePickerLauncher(selectionMode = SelectionMode.Single, scope = rememberCoroutineScope(), onResult = { byteArrays ->
        byteArrays.firstOrNull()?.let {
            viewModel.onImageFileSelected(it)
            notificationManager.showNotification("Image successfully uploaded", 3000)
        }
    })

    if (state.showCameraWarning) {
        if (viewModel.settingsManager.cameraRollDisabled.value) {
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

    AnimatedGridDialog(modifier = modifier, onDismiss = {
        onDismiss()
        if (state.changeWasMade) notificationManager.showNotification("Changes saved successfully", 3000)
    }, backHandler = backHandler, pages = listOf(Pair(state.customizationMenuState == CustomizationMenuState.DEFAULT) {
        BoxWithConstraints(modifier = modifier) {
            val padding = remember(Unit) { maxHeight / 40f }
            val textFieldHeight = remember(Unit) { maxWidth / 9f + 30.dp }
            val playerButtonPreviewHeight = remember(Unit) { min(maxWidth / 2f, maxHeight / 3f) }
            val focusManager = LocalFocusManager.current

            val buttonModifier = remember(Unit) {
                Modifier.then(
                    if (maxWidth / 3f < maxHeight / 4f) {
                        Modifier.fillMaxHeight(0.75f).padding(maxWidth / 50f)
                    } else {
                        Modifier.fillMaxWidth(0.75f).padding(maxHeight / 50f)
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
                        modifier = buttonModifier.height(textFieldHeight * 1.25f).padding(horizontal = padding / 4f).padding(bottom = padding / 2f),
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
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(padding / 2f), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
                ) { //TODO: check if the  name is currently taken or invalid
                    TextFieldWithButton(modifier = Modifier.fillMaxWidth(0.75f).height(textFieldHeight).clip(RoundedCornerShape(8))
                        .border(dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.halfAlpha(), RoundedCornerShape(8)), value = state.changeNameTextField, onValueChange = {
                        viewModel.setChangeNameField(it)
                    }, label = "Name", keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                    ), keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }), button = {
                        SettingsButton(modifier = Modifier.fillMaxSize().padding(padding / 6f), imageVector = vectorResource(Res.drawable.pencil_icon), shadowEnabled = false, onPress = {
                            focusManager.clearFocus()
                        })
                    })
                    Spacer(modifier = Modifier.width(padding / 4f))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(textFieldHeight).clip(RoundedCornerShape(8))
                            .border(dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.halfAlpha(), RoundedCornerShape(8)).pointerInput(Unit) {
                                detectTapGestures(onPress = {
                                    viewModel.setCustomizeMenuState(CustomizationMenuState.LOAD_PLAYER)
                                    backHandler.push { viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT) }
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
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
                PhysicsDraggable(
                    Modifier.height(playerButtonPreviewHeight).aspectRatio(1.75f)
                ) {
                    PlayerButtonPreview(
                        modifier = Modifier.fillMaxSize(),
                        name = state.changeNameTextField.text,
                        lifeNumber = 40,
                        state = PBState.NORMAL,
                        isDead = false,
                        imageUri = state.player.imageString,
                        backgroundColor = state.player.color,
                        accentColor = state.player.textColor,
                    )
                }
                Spacer(modifier = Modifier.weight(0.1f))
                LazyVerticalGrid(modifier = Modifier.padding(horizontal = padding / 2f).wrapContentSize(),
                    columns = GridCells.Fixed(3),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center,
                    content = {
                        item {
                            FormattedSettingsButton(
                                imageResource = Res.drawable.gif_icon,
                                text = "Gif",
                            ) {
                                viewModel.setCustomizeMenuState(CustomizationMenuState.GIF_SEARCH)
                                backHandler.push { viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT) }
                            }
                        }
                        item {
                            FormattedSettingsButton(
                                imageResource = Res.drawable.search_icon, text = "Search Image"
                            ) {
                                viewModel.setCustomizeMenuState(CustomizationMenuState.SCRYFALL_SEARCH)
                                backHandler.push { viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT) }
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
                                imageResource = Res.drawable.color_picker_icon, text = "Background Color"
                            ) {
                                viewModel.setCustomizeMenuState(CustomizationMenuState.BACKGROUND_COLOR_PICKER)
                                backHandler.push {
                                    viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT)
                                    if (state.colorChangeWasMade) notificationManager.showNotification("Background color modified")
                                    viewModel.setColorChangeWasMade(false)
                                }
                            }
                        }
                        item {
                            FormattedSettingsButton(
                                imageResource = Res.drawable.text_icon, text = "Text Color"
                            ) {
                                viewModel.setCustomizeMenuState(CustomizationMenuState.ACCENT_COLOR_PICKER)
                                backHandler.push {
                                    viewModel.setCustomizeMenuState(CustomizationMenuState.DEFAULT)
                                    if (state.colorChangeWasMade) notificationManager.showNotification("Text color modified")
                                    viewModel.setColorChangeWasMade(false)
                                }
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                SettingsButton(
                                    modifier = buttonModifier.height(textFieldHeight * 1.25f).padding(horizontal = padding / 4f).padding(bottom = padding / 2f),
                                    imageVector = vectorResource(Res.drawable.reset_icon),
                                    text = "Undo Changes",
                                    shadowEnabled = false,
                                    onPress = {
                                        if (state.changeWasMade) {
                                            viewModel.revertChanges()
                                            notificationManager.showNotification("Changes successfully reverted", 3000)
                                        } else {
                                            notificationManager.showNotification("No changes to revert", 1500)
                                        }
                                    },
                                    hapticEnabled = state.changeWasMade,
                                    enabled = true,
                                    mainColor = if (state.changeWasMade) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.halfAlpha()
                                )
                            }
                        }
                    })
                Spacer(modifier = Modifier.weight(0.15f))
            }
        }
    }, Pair(state.customizationMenuState == CustomizationMenuState.LOAD_PLAYER) {
        val playerList = remember {
            mutableStateListOf<Player>().apply {
                addAll(viewModel.settingsManager.loadPlayerPrefs().filter { it.name == "P${state.player.playerNum}" })
                addAll(viewModel.settingsManager.loadPlayerPrefs().filter { !it.isDefaultOrEmptyName() })
//                addAll(viewModel.settingsManager.loadPlayerPrefs())
            }
        }
        LoadPlayerDialogContent(playerList = playerList, onPlayerSelected = { player ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.setPlayer(player)
            backHandler.pop()
            notificationManager.showNotification("Selected ${player.name} Successfully", 3000)
        }, onPlayerDeleted = { player ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            playerList.remove(player)
            viewModel.settingsManager.deletePlayerPref(player)
            notificationManager.showNotification("Deleted ${player.name} Successfully", 3000)
        })
    }, Pair(state.customizationMenuState == CustomizationMenuState.SCRYFALL_SEARCH) {
        val scryfallBackStack = remember { mutableListOf("Main") }
        ScryfallDialogContent(modifier = Modifier.fillMaxSize(), addToBackStack = { label, block ->
            backHandler.push(block)
            scryfallBackStack.add(label)
        }, selectButtonEnabled = true, printingsButtonEnabled = true, rulingsButtonEnabled = false, onImageSelected = {
            notificationManager.showNotification("Selected Image Successfully", 3000)
            viewModel.onChangeImage(it)
            while (scryfallBackStack.isNotEmpty()) {
                backHandler.pop()
                scryfallBackStack.removeLast()
            }
        })
    }, Pair(state.customizationMenuState == CustomizationMenuState.BACKGROUND_COLOR_PICKER) {
        val colorViewModel = remember { ColorDialogViewModel() }
        val initialColor by remember(Unit) { mutableStateOf(state.player.color) }
        ColorPickerDialogContent(
            modifier = Modifier.fillMaxSize(), title = "Background Color", initialColor = initialColor, initialPlayer = state.player, updateColor = {
                viewModel.onChangeBackgroundColor(it)
                viewModel.setColorChangeWasMade(it != initialColor)
                state.player.copy(color = it)
            }, viewModel = colorViewModel
        )
    }, Pair(state.customizationMenuState == CustomizationMenuState.ACCENT_COLOR_PICKER) {
        val colorViewModel = remember { ColorDialogViewModel() }
        val initialColor by remember(Unit) { mutableStateOf(state.player.textColor) }
        ColorPickerDialogContent(
            modifier = Modifier.fillMaxSize(), title = "Accent Color", initialColor = initialColor, initialPlayer = state.player, updateColor = {
                viewModel.onChangeTextColor(it)
                viewModel.setColorChangeWasMade(it != initialColor)
                state.player.copy(textColor = it)
            }, viewModel = colorViewModel
        )
    }, Pair(state.customizationMenuState == CustomizationMenuState.GIF_SEARCH) {
        GifDialogContent(modifier = Modifier.fillMaxSize(), onGifSelected = {
            notificationManager.showNotification("Selected Gif Successfully", 3000)
            viewModel.onChangeImage(it)
            backHandler.pop()
        })
    }))
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
        val padding = remember(Unit) { maxHeight / 6f }
        PlayerButtonBackground(
            state = state,
            imageUri = imageUri,
            color = backgroundColor,
            isDead = isDead,
        )
        LifeNumber(
            modifier = Modifier.fillMaxHeight().fillMaxWidth().padding(bottom = padding), value = NumberWithRecentChange(lifeNumber, 0), name = name, textColor = accentColor
        )
    }
}