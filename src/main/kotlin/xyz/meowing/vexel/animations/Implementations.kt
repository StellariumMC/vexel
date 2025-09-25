package xyz.meowing.vexel.animations

class FloatAnimation(
    target: AnimationTarget<Float>,
    duration: Long,
    type: EasingType,
    animationType: AnimationType,
    elementId: String,
    onComplete: (() -> Unit)? = null
) : Animation<Float>(target, duration, type, animationType, elementId, onComplete) {
    override fun interpolate(start: Float, end: Float, progress: Float): Float = start + (end - start) * progress
}

class ColorAnimation(
    target: AnimationTarget<Int>,
    duration: Long,
    type: EasingType,
    elementId: String,
    onComplete: (() -> Unit)? = null
) : Animation<Int>(target, duration, type, AnimationType.COLOR, elementId, onComplete) {
    override fun interpolate(start: Int, end: Int, progress: Float): Int {
        val startA = (start shr 24) and 0xFF
        val startR = (start shr 16) and 0xFF
        val startG = (start shr 8) and 0xFF
        val startB = start and 0xFF

        val endA = (end shr 24) and 0xFF
        val endR = (end shr 16) and 0xFF
        val endG = (end shr 8) and 0xFF
        val endB = end and 0xFF

        val a = (startA + (endA - startA) * progress).toInt()
        val r = (startR + (endR - startR) * progress).toInt()
        val g = (startG + (endG - startG) * progress).toInt()
        val b = (startB + (endB - startB) * progress).toInt()

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}

class VectorAnimation(
    target: AnimationTarget<Pair<Float, Float>>,
    duration: Long,
    type: EasingType,
    animationType: AnimationType,
    elementId: String,
    onComplete: (() -> Unit)? = null
) : Animation<Pair<Float, Float>>(target, duration, type, animationType, elementId, onComplete) {
    override fun interpolate(start: Pair<Float, Float>, end: Pair<Float, Float>, progress: Float): Pair<Float, Float> {
        return (start.first + (end.first - start.first) * progress) to (start.second + (end.second - start.second) * progress)
    }
}