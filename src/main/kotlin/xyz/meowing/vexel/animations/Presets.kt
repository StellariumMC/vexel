package xyz.meowing.vexel.animations

import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.SvgImage
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.VexelElement
import java.awt.Color

private val originalSizes = mutableMapOf<String, Pair<Float, Float>>()
private val originalPositions = mutableMapOf<String, Pair<Float, Float>>()

fun <T : VexelElement<T>> T.fadeIn(
    duration: Long = 300,
    type: EasingType = EasingType.EASE_OUT,
    includeChildren: Boolean = true,
    onComplete: (() -> Unit)? = null
): T {
    visible = true

    if (includeChildren) {
        children.forEach { child ->
            when (child) {
                is Rectangle -> child.fadeIn(duration, type, includeChildren)
                is Text -> child.fadeIn(duration, type, includeChildren)
                is SvgImage -> child.fadeIn(duration, type, includeChildren)
            }
        }
    }

    when (this) {
        is Rectangle -> {
            val targetBg = (backgroundColor and 0x00FFFFFF) or (255 shl 24)
            val targetBorder = (borderColor and 0x00FFFFFF) or (255 shl 24)
            backgroundColor = backgroundColor and 0x00FFFFFF
            borderColor = borderColor and 0x00FFFFFF
            animateColor({ backgroundColor }, { backgroundColor = it }, targetBg, duration, type, onComplete)
            animateColor({ borderColor }, { borderColor = it }, targetBorder, duration, type)
        }
        is Text -> {
            val target = (textColor and 0x00FFFFFF) or (255 shl 24)
            textColor = textColor and 0x00FFFFFF
            animateColor({ textColor }, { textColor = it }, target, duration, type, onComplete)
        }
        is SvgImage -> {
            val target = (color.rgb and 0x00FFFFFF) or (255 shl 24)
            animateColor({ color.rgb }, { setSvgColor(Color(it, true)) }, target, duration, type, onComplete)
        }
        else -> animateFloat({ 0f }, {}, 1f, duration, type, AnimationType.ALPHA, onComplete)
    }

    return this
}

fun <T : VexelElement<T>> T.fadeOut(
    duration: Long = 300,
    type: EasingType = EasingType.EASE_IN,
    includeChildren: Boolean = true,
    onComplete: (() -> Unit)? = null
): T {
    if (includeChildren) {
        children.forEach { child ->
            when (child) {
                is Rectangle -> child.fadeOut(duration, type, includeChildren)
                is Text -> child.fadeOut(duration, type, includeChildren)
                is SvgImage -> child.fadeOut(duration, type, includeChildren)
            }
        }
    }

    when (this) {
        is Rectangle -> {
            val targetBg = backgroundColor and 0x00FFFFFF
            val targetBorder = borderColor and 0x00FFFFFF
            animateColor({ backgroundColor }, { backgroundColor = it }, targetBg, duration, type) {
                visible = false
                onComplete?.invoke()
            }
            animateColor({ borderColor }, { borderColor = it }, targetBorder, duration, type)
        }
        is Text -> {
            val target = textColor and 0x00FFFFFF
            animateColor({ textColor }, { textColor = it }, target, duration, type) {
                visible = false
                onComplete?.invoke()
            }
        }
        is SvgImage -> {
            val target = color.rgb and 0x00FFFFFF
            animateColor({ color.rgb }, { setSvgColor(Color(it, true)) }, target, duration, type) {
                visible = false
                onComplete?.invoke()
            }
        }
        else -> animateFloat({ 1f }, {}, 0f, duration, type, AnimationType.ALPHA) {
            visible = false
            onComplete?.invoke()
        }
    }

    return this
}

fun <T : VexelElement<T>> T.slideIn(
    fromX: Float = -width,
    fromY: Float = 0f,
    duration: Long = 500,
    type: EasingType = EasingType.EASE_OUT,
    onComplete: (() -> Unit)? = null
): T {
    val id = hashCode().toString()
    originalPositions.putIfAbsent(id, xConstraint to yConstraint)

    val (targetX, targetY) = originalPositions[id]!!
    xConstraint = fromX
    yConstraint = fromY
    visible = true

    animatePosition(targetX, targetY, duration, type, onComplete)
    return this
}

fun <T : VexelElement<T>> T.bounceScale(
    scale: Float = 1.2f,
    duration: Long = 200,
    onComplete: (() -> Unit)? = null
): T {
    val id = hashCode().toString()
    AnimationManager.stopAnimations(id, AnimationType.SIZE)
    originalSizes.putIfAbsent(id, width to height)

    val (originalWidth, originalHeight) = originalSizes[id]!!
    val targetWidth = originalWidth * scale
    val targetHeight = originalHeight * scale

    animateSize(targetWidth, targetHeight, duration / 2, EasingType.EASE_OUT) {
        animateSize(originalWidth, originalHeight, duration / 2, EasingType.EASE_IN, onComplete)
    }

    return this
}

fun <T : VexelElement<T>> T.moveTo(
    x: Float,
    y: Float,
    duration: Long = 500,
    type: EasingType = EasingType.EASE_OUT,
    onComplete: (() -> Unit)? = null
): T {
    animatePosition(x, y, duration, type, onComplete)
    return this
}

fun <T : VexelElement<T>> T.scaleTo(
    width: Float,
    height: Float,
    duration: Long = 300,
    type: EasingType = EasingType.EASE_OUT,
    onComplete: (() -> Unit)? = null
): T {
    animateSize(width, height, duration, type, onComplete)
    return this
}

fun <T : VexelElement<T>> T.colorTo(
    color: Int,
    duration: Long = 300,
    type: EasingType = EasingType.LINEAR,
    onComplete: (() -> Unit)? = null
): T {
    when (this) {
        is Rectangle -> animateColor({ backgroundColor }, { backgroundColor = it }, color, duration, type, onComplete)
        is Text -> animateColor({ textColor }, { textColor = it }, color, duration, type, onComplete)
        is SvgImage -> animateColor({ this.color.rgb }, { setSvgColor(Color(it, true)) }, color, duration, type, onComplete)
    }
    return this
}