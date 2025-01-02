package domain.system

import androidx.compose.runtime.Composable

expect class SystemManager {
    companion object {
        @Composable
        fun getAnimationCorrectionFactor(): Float

        @Composable
        fun updateSystemBarsColors(isDarkTheme: Boolean)

        @Composable
        fun keepScreenOn(keepScreenOn: Boolean)

        fun legacyMonarchyIndicator(): Boolean
    }
}