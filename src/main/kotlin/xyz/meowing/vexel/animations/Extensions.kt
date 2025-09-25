package xyz.meowing.vexel.animations

import xyz.meowing.vexel.components.base.VexelElement

fun <T : VexelElement<T>> T.animateFloat(
    getter: () -> Float,
    setter: (Float) -> Unit,
    endValue: Float,
    duration: Long,
    type: EasingType = EasingType.LINEAR,
    animationType: AnimationType = AnimationType.CUSTOM,
    onComplete: (() -> Unit)? = null
): FloatAnimation {
    val target = AnimationTarget(getter(), endValue, setter)
    val animation = FloatAnimation(target, duration, type, animationType, "${hashCode()}+$endValue", onComplete)
    animation.start()
    return animation
}

fun <T : VexelElement<T>> T.animateColor(
    getter: () -> Int,
    setter: (Int) -> Unit,
    endValue: Int,
    duration: Long,
    type: EasingType = EasingType.LINEAR,
    onComplete: (() -> Unit)? = null
): ColorAnimation {
    val target = AnimationTarget(getter(), endValue, setter)
    val animation = ColorAnimation(target, duration, type, "${hashCode()}+$endValue", onComplete)
    animation.start()
    return animation
}

fun <T : VexelElement<T>> T.animatePosition(
    endX: Float,
    endY: Float,
    duration: Long,
    type: EasingType = EasingType.LINEAR,
    onComplete: (() -> Unit)? = null
): VectorAnimation {
    val target = AnimationTarget(
        xConstraint to yConstraint,
        endX to endY
    ) { (x, y) ->
        xConstraint = x
        yConstraint = y
    }
    val animation = VectorAnimation(target, duration, type, AnimationType.POSITION, "${hashCode()}:pos", onComplete)
    animation.start()
    return animation
}

fun <T : VexelElement<T>> T.animateSize(
    endWidth: Float,
    endHeight: Float,
    duration: Long,
    type: EasingType = EasingType.LINEAR,
    onComplete: (() -> Unit)? = null
): VectorAnimation {
    val target = AnimationTarget(
        width to height,
        endWidth to endHeight
    ) { (w, h) ->
        width = w
        height = h
    }
    val animation = VectorAnimation(target, duration, type, AnimationType.SIZE, "${hashCode()}:size", onComplete)
    animation.start()
    return animation
}