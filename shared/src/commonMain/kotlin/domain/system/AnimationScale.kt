package domain.system

internal fun animationCorrectionFactorFor(systemAnimatorScale: Float): Float {
    return if (systemAnimatorScale > 0f) systemAnimatorScale else 1f
}
