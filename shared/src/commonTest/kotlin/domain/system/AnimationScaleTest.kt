package domain.system

import kotlin.test.Test
import kotlin.test.assertEquals

class AnimationScaleTest {
    @Test
    fun disabledAnimatorScaleUsesFiniteCorrectionFactor() {
        assertEquals(1f, animationCorrectionFactorFor(systemAnimatorScale = 0f))
    }

    @Test
    fun positiveAnimatorScaleUsesSystemValue() {
        assertEquals(0.5f, animationCorrectionFactorFor(systemAnimatorScale = 0.5f))
    }
}
