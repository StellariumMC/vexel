package xyz.meowing.vexel.animations

enum class AnimationType {
    POSITION,
    SIZE,
    COLOR,
    ALPHA,
    CUSTOM
}

enum class EasingType {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT
}

data class AnimationTarget<T>(
    val startValue: T,
    val endValue: T,
    val setter: (T) -> Unit
)