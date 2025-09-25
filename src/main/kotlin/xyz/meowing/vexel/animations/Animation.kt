package xyz.meowing.vexel.animations

abstract class Animation<T>(
    val target: AnimationTarget<T>,
    val duration: Long,
    val type: EasingType,
    val animationType: AnimationType,
    val elementId: String,
    val onComplete: (() -> Unit)? = null
) {
    private var startTime: Long = 0
    private var isStarted = false
    var isCompleted = false
        private set

    fun start() {
        if (!isStarted) {
            startTime = System.currentTimeMillis()
            isStarted = true
            AnimationManager.register(this)
        }
    }

    fun update(): Boolean {
        if (!isStarted || isCompleted) return false

        val elapsed = System.currentTimeMillis() - startTime
        val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
        val easedProgress = applyEasing(progress)

        target.setter(interpolate(target.startValue, target.endValue, easedProgress))

        if (progress >= 1f) {
            isCompleted = true
            onComplete?.invoke()
            return true
        }

        return false
    }

    private fun applyEasing(t: Float): Float = when (type) {
        EasingType.LINEAR -> t
        EasingType.EASE_IN -> t * t
        EasingType.EASE_OUT -> 1f - (1f - t) * (1f - t)
        EasingType.EASE_IN_OUT -> if (t < 0.5f) 2f * t * t else 1f - 2f * (1f - t) * (1f - t)
    }

    abstract fun interpolate(start: T, end: T, progress: Float): T
}