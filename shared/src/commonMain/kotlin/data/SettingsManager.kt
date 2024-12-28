package data


import com.russhwolf.settings.Settings
import data.serializable.Card
import domain.timer.GameTimerState
import di.VersionNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


interface ISettingsManager {
    val autoKo: StateFlow<Boolean>
    fun setAutoKo(value: Boolean)

    val autoSkip: StateFlow<Boolean>
    fun setAutoSkip(value: Boolean)

    val keepScreenOn: StateFlow<Boolean>
    fun setKeepScreenOn(value: Boolean)

    val cameraRollDisabled: StateFlow<Boolean>
    fun setCameraRollDisabled(value: Boolean)

    val fastCoinFlip: StateFlow<Boolean>
    fun setFastCoinFlip(value: Boolean)

    val numPlayers: StateFlow<Int>
    fun setNumPlayers(value: Int)

    val alt4PlayerLayout: StateFlow<Boolean>
    fun setAlt4PlayerLayout(value: Boolean)

    val darkTheme: StateFlow<Boolean>
    fun setDarkTheme(value: Boolean)

    val startingLife: StateFlow<Int>
    fun setStartingLife(value: Int)

    val tutorialSkip: StateFlow<Boolean>
    fun setTutorialSkip(value: Boolean)

    val lastSplashScreenShown: StateFlow<String>
    fun setLastSplashScreenShown(value: String)

    val turnTimer: StateFlow<Boolean>
    fun setTurnTimer(value: Boolean)

    val devMode: StateFlow<Boolean>
    fun setDevMode(value: Boolean)

    fun loadPlayerStates(): List<Player>
    fun savePlayerStates(players: List<Player>)

    fun savePlanechaseState(planarDeck: List<Card>, planarBackStack: List<Card>)
    fun loadPlanechaseState(): Pair<List<Card>, List<Card>>

    fun savePlayerPref(player: Player)
    fun deletePlayerPref(player: Player)
    fun loadPlayerPrefs(): ArrayList<Player>

    val patchNotes: StateFlow<String>
    fun setPatchNotes(value: String)

    val savedTimerState: StateFlow<GameTimerState?>
    fun setSavedTimerState(value: GameTimerState?)
}

class SettingsManager private constructor() : ISettingsManager {

    companion object {
        val instance: SettingsManager by lazy { SettingsManager() }
    }

    private val settings: Settings = Settings()

    private val _autoKo = MutableStateFlow(settings.getBoolean("autoKo", true))
    override val autoKo: StateFlow<Boolean> = _autoKo.asStateFlow()
    override fun setAutoKo(value: Boolean) {
        settings.putBoolean("autoKo", value)
        _autoKo.value = value
    }

    private val _autoSkip = MutableStateFlow(settings.getBoolean("autoSkip", false))
    override val autoSkip: StateFlow<Boolean> = _autoSkip.asStateFlow()
    override fun setAutoSkip(value: Boolean) {
        settings.putBoolean("autoSkip", value)
        _autoSkip.value = value
    }

    private val _keepScreenOn = MutableStateFlow(settings.getBoolean("keepScreenOn", false))
    override val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()
    override fun setKeepScreenOn(value: Boolean) {
        settings.putBoolean("keepScreenOn", value)
        _keepScreenOn.value = value
    }

    private val _cameraRollDisabled = MutableStateFlow(settings.getBoolean("cameraRollDisabled", false))
    override val cameraRollDisabled: StateFlow<Boolean> = _cameraRollDisabled.asStateFlow()
    override fun setCameraRollDisabled(value: Boolean) {
        settings.putBoolean("cameraRollDisabled", value)
        _cameraRollDisabled.value = value
    }

    private val _fastCoinFlip = MutableStateFlow(settings.getBoolean("fastCoinFlip", false))
    override val fastCoinFlip: StateFlow<Boolean> = _fastCoinFlip.asStateFlow()
    override fun setFastCoinFlip(value: Boolean) {
        settings.putBoolean("fastCoinFlip", value)
        _fastCoinFlip.value = value
    }

    private val _numPlayers = MutableStateFlow(settings.getInt("numPlayers", 4))
    override var numPlayers: StateFlow<Int> = _numPlayers.asStateFlow()
    override fun setNumPlayers(value: Int) {
        settings.putInt("numPlayers", value)
        _numPlayers.value = value
    }

    private val _alt4PlayerLayout = MutableStateFlow(settings.getBoolean("alt4PlayerLayout", false))
    override val alt4PlayerLayout: StateFlow<Boolean> = _alt4PlayerLayout.asStateFlow()
    override fun setAlt4PlayerLayout(value: Boolean) {
        settings.putBoolean("alt4PlayerLayout", value)
        _alt4PlayerLayout.value = value
    }

    private val _darkTheme = MutableStateFlow(settings.getBoolean("darkTheme", true))
    override val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()
    override fun setDarkTheme(value: Boolean) {
        settings.putBoolean("darkTheme", value)
        _darkTheme.value = value
    }

    private val _startingLife = MutableStateFlow(settings.getInt("startingLife", 40))
    override val startingLife: StateFlow<Int> = _startingLife.asStateFlow()
    override fun setStartingLife(value: Int) {
        settings.putInt("startingLife", value)
        _startingLife.value = value
    }

    private val _tutorialSkip = MutableStateFlow(settings.getBoolean("tutorialSkip", false))
    override val tutorialSkip: StateFlow<Boolean> = _tutorialSkip.asStateFlow()
    override fun setTutorialSkip(value: Boolean) {
        settings.putBoolean("tutorialSkip", value)
        _tutorialSkip.value = value
    }

    private val _lastSplashScreenShown = MutableStateFlow(settings.getString("lastSplashScreenShown", VersionNumber.zero.value))
    override val lastSplashScreenShown: StateFlow<String> = _lastSplashScreenShown.asStateFlow()
    override fun setLastSplashScreenShown(value: String) {
        settings.putString("lastSplashScreenShown", value)
        _lastSplashScreenShown.value = value
    }

    private val _turnTimer = MutableStateFlow(settings.getBoolean("turnTimer", false))
    override val turnTimer: StateFlow<Boolean> = _turnTimer.asStateFlow()
    override fun setTurnTimer(value: Boolean) {
        settings.putBoolean("turnTimer", value)
        _turnTimer.value = value
    }

    private val _devMode = MutableStateFlow(settings.getBoolean("devMode", false))
    override val devMode: StateFlow<Boolean> = _devMode.asStateFlow()
    override fun setDevMode(value: Boolean) {
        settings.putBoolean("devMode", value)
        _devMode.value = value
    }

    private val _patchNotes = MutableStateFlow(settings.getString("patchNotes", ""))
    override val patchNotes: StateFlow<String> = _patchNotes.asStateFlow()
    override fun setPatchNotes(value: String) {
        settings.putString("patchNotes", value)
        _patchNotes.value = value
    }

    private val _savedTimerState = MutableStateFlow(loadSavedTimerState())
    override val savedTimerState: StateFlow<GameTimerState?> = _savedTimerState.asStateFlow()
    
    override fun setSavedTimerState(value: GameTimerState?) {
        if (value == null) {
            settings.remove("savedTimerState")
        } else {
            settings.putString("savedTimerState", Json.encodeToString(value))
        }
        _savedTimerState.value = value
    }
    
    private fun loadSavedTimerState(): GameTimerState? {
        val savedState = settings.getStringOrNull("savedTimerState")
        return savedState?.let { Json.decodeFromString<GameTimerState>(it) }
    }

    override fun loadPlayerStates(): List<Player> {
        val allPrefString = settings.getString("playerStates", "[]")
        return Json.decodeFromString<List<Player>>(allPrefString)
    }

    override fun savePlayerStates(players: List<Player>) {
        val allPrefString = Json.encodeToString(players)
        settings.putString("playerStates", allPrefString)
    }

    override fun savePlanechaseState(planarDeck: List<Card>, planarBackStack: List<Card>) {
        val allPrefString = Json.encodeToString(planarDeck)
        settings.putString("planarDeck", allPrefString)
        val backPrefString = Json.encodeToString(planarBackStack)
        settings.putString("planarBackStack", backPrefString)
    }

    override fun loadPlanechaseState(): Pair<List<Card>, List<Card>> {
        val deckPrefString = settings.getString("planarDeck", "[]")
        val backPrefString = settings.getString("planarBackStack", "[]")
        return Pair(Json.decodeFromString(deckPrefString), Json.decodeFromString(backPrefString))
    }

    override fun savePlayerPref(player: Player) {
        val playerList = loadPlayerPrefs()
        internalSavePlayerPref(player, playerList)
    }

    private fun internalSavePlayerPref(player: Player, playerList: ArrayList<Player> = loadPlayerPrefs()) {
        removePlayerPref(player, playerList)
        playerList.add(player)
        savePlayerPrefs(playerList)
    }

    private fun removePlayerPref(player: Player, playerList: ArrayList<Player>) {
        val iterator = playerList.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().name == player.name) {
                iterator.remove()
            }
        }
    }

    override fun deletePlayerPref(player: Player) {
        val playerList = loadPlayerPrefs()
        internalDeletePlayerPref(player, playerList)
    }

    private fun internalDeletePlayerPref(player: Player, playerList: ArrayList<Player> = loadPlayerPrefs()) {
        removePlayerPref(player, playerList)
        savePlayerPrefs(playerList)
    }

    override fun loadPlayerPrefs(): ArrayList<Player> {
        val allPrefString = settings.getString("playerPrefs", "[]")
        return Json.decodeFromString<List<Player>>(allPrefString).reversed().toCollection(ArrayList())
    }

    private fun savePlayerPrefs(players: ArrayList<Player>) {
        val allPrefString = Json.encodeToString(players)
        settings.putString("playerPrefs", allPrefString)
    }
}
