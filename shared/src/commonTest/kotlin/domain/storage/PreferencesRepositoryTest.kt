package domain.storage

import com.russhwolf.settings.Settings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PreferencesRepositoryTest {
    @Test
    fun exposesDefaultPreferenceValues() {
        val repository = PreferencesRepository(TestSettings())

        assertTrue(repository.autoKo.value)
        assertFalse(repository.autoSkip.value)
        assertFalse(repository.keepScreenOn.value)
        assertFalse(repository.cameraRollDisabled.value)
        assertFalse(repository.fastCoinFlip.value)
        assertEquals(4, repository.numPlayers.value)
        assertFalse(repository.alt4PlayerLayout.value)
        assertTrue(repository.darkTheme.value)
        assertEquals(40, repository.startingLife.value)
        assertFalse(repository.tutorialSkip.value)
        assertEquals("0.0.0", repository.lastSplashScreenShown.value)
        assertFalse(repository.turnTimer.value)
        assertFalse(repository.devMode.value)
    }

    @Test
    fun writesPreferencesToSettingsAndFlows() {
        val settings = TestSettings()
        val repository = PreferencesRepository(settings)

        repository.setAutoKo(false)
        repository.setAutoSkip(true)
        repository.setKeepScreenOn(true)
        repository.setCameraRollDisabled(true)
        repository.setFastCoinFlip(true)
        repository.setNumPlayers(6)
        repository.setAlt4PlayerLayout(true)
        repository.setDarkTheme(false)
        repository.setStartingLife(20)
        repository.setTutorialSkip(true)
        repository.setLastSplashScreenShown("1.2.3")
        repository.setTurnTimer(true)
        repository.setDevMode(true)

        assertFalse(repository.autoKo.value)
        assertTrue(repository.autoSkip.value)
        assertTrue(repository.keepScreenOn.value)
        assertTrue(repository.cameraRollDisabled.value)
        assertTrue(repository.fastCoinFlip.value)
        assertEquals(6, repository.numPlayers.value)
        assertTrue(repository.alt4PlayerLayout.value)
        assertFalse(repository.darkTheme.value)
        assertEquals(20, repository.startingLife.value)
        assertTrue(repository.tutorialSkip.value)
        assertEquals("1.2.3", repository.lastSplashScreenShown.value)
        assertTrue(repository.turnTimer.value)
        assertTrue(repository.devMode.value)
    }

    @Test
    fun reloadsPreferencesFromExistingSettings() {
        val settings = TestSettings()
        val firstRepository = PreferencesRepository(settings)

        firstRepository.setAutoKo(false)
        firstRepository.setNumPlayers(2)
        firstRepository.setDarkTheme(false)
        firstRepository.setStartingLife(30)
        firstRepository.setLastSplashScreenShown("2.0.0")

        val reloadedRepository = PreferencesRepository(settings)

        assertFalse(reloadedRepository.autoKo.value)
        assertEquals(2, reloadedRepository.numPlayers.value)
        assertFalse(reloadedRepository.darkTheme.value)
        assertEquals(30, reloadedRepository.startingLife.value)
        assertEquals("2.0.0", reloadedRepository.lastSplashScreenShown.value)
    }
}

class TestSettings : Settings {
    private val values = mutableMapOf<String, Any>()

    override val keys: Set<String>
        get() = values.keys

    override val size: Int
        get() = values.size

    override fun clear() {
        values.clear()
    }

    override fun remove(key: String) {
        values.remove(key)
    }

    override fun hasKey(key: String): Boolean {
        return values.containsKey(key)
    }

    override fun putInt(key: String, value: Int) {
        values[key] = value
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return getIntOrNull(key) ?: defaultValue
    }

    override fun getIntOrNull(key: String): Int? {
        return values[key] as? Int
    }

    override fun putLong(key: String, value: Long) {
        values[key] = value
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return getLongOrNull(key) ?: defaultValue
    }

    override fun getLongOrNull(key: String): Long? {
        return values[key] as? Long
    }

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun getString(key: String, defaultValue: String): String {
        return getStringOrNull(key) ?: defaultValue
    }

    override fun getStringOrNull(key: String): String? {
        return values[key] as? String
    }

    override fun putFloat(key: String, value: Float) {
        values[key] = value
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return getFloatOrNull(key) ?: defaultValue
    }

    override fun getFloatOrNull(key: String): Float? {
        return values[key] as? Float
    }

    override fun putDouble(key: String, value: Double) {
        values[key] = value
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return getDoubleOrNull(key) ?: defaultValue
    }

    override fun getDoubleOrNull(key: String): Double? {
        return values[key] as? Double
    }

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getBooleanOrNull(key) ?: defaultValue
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return values[key] as? Boolean
    }
}
