package xyz.meowing.vexel.elements

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animatePosition
import xyz.meowing.vexel.animations.animateSize
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import java.awt.Color
import kotlin.math.roundToInt

class Slider(
    var value: Float = 0.5f,
    var minValue: Float = 0f,
    var maxValue: Float = 1f,
    var step: Float? = null,
    var thumbColor: Int = 0xFFFFFFFF.toInt(),
    var thumbHoverColor: Int? = null,
    var thumbPressedColor: Int? = null,
    var trackColor: Int = 0xFF424242.toInt(),
    var trackFillColor: Int = 0xFF4c87f9.toInt(),
    var trackHoverColor: Int? = null,
    var trackPressedColor: Int? = null,
    thumbWidth: Float = 20f,
    thumbHeight: Float = 20f,
    thumbRadius: Float = 10f,
    var trackHeight: Float = 4f,
    trackRadius: Float = 2f,
    backgroundColor: Int = 0x00000000,
    borderColor: Int = 0x00000000,
    borderRadius: Float = 0f,
    borderThickness: Float = 0f,
    padding: FloatArray = floatArrayOf(0f, 0f, 0f, 0f),
    hoverColor: Int? = null,
    pressedColor: Int? = null,
    widthType: Size = Size.Pixels,
    heightType: Size = Size.Pixels
) : VexelElement<Slider>(widthType, heightType) {
    var isDragging = false
    private var dragStartX = 0f
    private var dragStartValue = 0f
    private var globalReleaseListener: ((Float, Float, Int) -> Boolean)? = null
    private val separators: MutableList<Rectangle> = mutableListOf()

    val container = Rectangle(backgroundColor, borderColor, borderRadius, borderThickness, padding, hoverColor, pressedColor, Size.ParentPerc, Size.ParentPerc)
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .ignoreMouseEvents()
        .childOf(this)

    val trackBackground = Rectangle(trackColor, 0x00000000, trackRadius, 0f, floatArrayOf(0f, 0f, 0f, 0f), null, null, Size.ParentPerc, Size.Pixels)
        .setSizing(100f, Size.ParentPerc, trackHeight, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .ignoreMouseEvents()
        .childOf(container)

    val stepContainer = Rectangle(0x00000000, 0x00000000, 0f, 0f, floatArrayOf(0f, 0f, 0f, 0f), null, null, Size.ParentPerc, Size.ParentPerc)
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .ignoreMouseEvents()
        .childOf(container)

    val trackFill = Rectangle(trackFillColor, 0x00000000, trackRadius, 0f, floatArrayOf(0f, 0f, 0f, 0f), null, null, Size.Pixels, Size.Pixels)
        .setSizing(0f, Size.Pixels, trackHeight, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .ignoreMouseEvents()
        .childOf(container)

    val thumb = Rectangle(thumbColor, 0x00000000, thumbRadius, 0f, floatArrayOf(0f, 0f, 0f, 0f), thumbHoverColor, thumbPressedColor, Size.Pixels, Size.Pixels)
        .setSizing(thumbWidth, Size.Pixels, thumbHeight, Size.Pixels)
        .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        .ignoreMouseEvents()
        .childOf(container)

    init {
        setSizing(200f, Size.Pixels, maxOf(thumbHeight, trackHeight) + 4f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)

        updateThumbPosition(false)
        updateTrackFill()

        onMouseClick { mouseX, mouseY, button ->
            if (button == 0) {
                val relativeX = mouseX - container.x
                val trackWidth = container.width
                val newValue = minValue + (relativeX / trackWidth) * (maxValue - minValue)
                setValue(newValue.coerceIn(minValue, maxValue), animated = true)

                startDragging(mouseX)
                true
            } else false
        }

        setSliderSeparators()
    }

    private fun setSliderSeparators() {
        separators.forEach { it.destroy() }

        step?.let { stepSize ->
            val totalSteps = ((maxValue - minValue) / stepSize).roundToInt()
            if(totalSteps > 20 ) return

            for (i in 1 until totalSteps) {
                val stepValue = minValue + i * stepSize
                val stepPercent = (stepValue - minValue) / (maxValue - minValue) * 100f

                val separator = Rectangle(
                    Color(trackColor).brighter().brighter().rgb, 0x00000000,
                    1f, 0f,
                    floatArrayOf(0f, 0f, 0f, 0f),
                    null, null,
                    Size.Pixels, Size.Pixels
                )
                    .setSizing(3f, Size.Pixels, trackHeight, Size.Pixels)
                    .setPositioning(stepPercent, Pos.ParentPercent, 0f, Pos.ParentCenter)
                    .ignoreMouseEvents()
                    .childOf(stepContainer)

                separators.add(separator)
            }
        }
    }

    fun startDragging(mouseX: Float) {
        isDragging = true
        dragStartX = mouseX
        dragStartValue = value

        globalReleaseListener = { mx, my, btn ->
            if (btn == 0 && isDragging) {
                stopDragging()
                true
            } else false
        }

        getRootElement().onMouseRelease(globalReleaseListener!!)
    }

    fun stopDragging() {
        isDragging = false
        globalReleaseListener = null
    }

    override fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (isDragging) {
            val deltaX = mouseX - dragStartX
            val trackWidth = container.width
            val valueChange = (deltaX / trackWidth) * (maxValue - minValue)
            val newValue = (dragStartValue + valueChange).coerceIn(minValue, maxValue)
            setValue(newValue, animated = false)
            return true
        }
        return super.handleMouseMove(mouseX, mouseY)
    }

    override fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        val result = super.handleMouseRelease(mouseX, mouseY, button)
        if (button == 0 && isDragging) stopDragging()
        return result
    }

    override fun destroy() {
        if (isDragging) stopDragging()
        super.destroy()
    }

    fun setValue(newValue: Float, animated: Boolean = true, silent: Boolean = false): Slider {
        val clampedValue = newValue.coerceIn(minValue, maxValue)
        val steppedValue = step?.let {
            val steps = ((clampedValue - minValue) / it).roundToInt()
            minValue + steps * it
        } ?: clampedValue

        value = steppedValue
        updateThumbPosition(animated)
        updateTrackFill(animated)

        if (!silent) onValueChange.forEach { it.invoke(value) }
        return this
    }

    private fun updateThumbPosition(animated: Boolean) {
        val progress = if (maxValue == minValue) 0f else (value - minValue) / (maxValue - minValue)
        val trackWidth = container.width - thumb.width
        val targetX = trackWidth * progress

        if (animated) {
            thumb.animatePosition(targetX, thumb.yConstraint, 150, EasingType.EASE_OUT)
        } else {
            thumb.xConstraint = targetX
        }
    }

    private fun updateTrackFill(animated: Boolean = false) {
        val progress = if (maxValue == minValue) 0f else (value - minValue) / (maxValue - minValue)
        val fillWidth = (container.width * progress).coerceAtLeast(0f).coerceAtMost(container.width)

        if (animated) {
            trackFill.animateSize(fillWidth, trackFill.height, 150, EasingType.EASE_OUT)
        } else {
            trackFill.width = fillWidth
        }
    }

    private fun updateTrackColor() {
        val baseColor = trackColor
        val currentColor = when {
            pressed && trackPressedColor != null -> trackPressedColor!!
            hovered && trackHoverColor != null -> trackHoverColor!!
            else -> baseColor
        }
        trackBackground.backgroundColor = currentColor
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        container.isHovered = hovered
        container.isPressed = pressed || isDragging
        thumb.isHovered = hovered
        thumb.isPressed = pressed || isDragging
        updateTrackColor()

        if (!isDragging) {
            updateThumbPosition(false)
            updateTrackFill(false)
        }
    }

    fun minValue(min: Float): Slider = apply {
        minValue = min
        setValue(value.coerceIn(minValue, maxValue), false)
    }

    fun maxValue(max: Float): Slider = apply {
        maxValue = max
        setValue(value.coerceIn(minValue, maxValue), false)
    }

    fun step(stepSize: Float?): Slider = apply {
        step = stepSize
        setValue(value, false)
        setSliderSeparators()
    }

    fun thumbColor(color: Int): Slider = apply {
        thumbColor = color
        thumb.backgroundColor = color
    }

    fun thumbHoverColor(color: Int): Slider = apply {
        thumbHoverColor = color
        thumb.hoverColor = color
    }

    fun thumbPressedColor(color: Int): Slider = apply {
        thumbPressedColor = color
        thumb.pressedColor = color
    }

    fun trackColor(color: Int): Slider = apply {
        trackColor = color
        trackBackground.backgroundColor = color
    }

    fun trackFillColor(color: Int): Slider = apply {
        trackFillColor = color
        trackFill.backgroundColor = color
    }

    fun trackHoverColor(color: Int): Slider = apply {
        trackHoverColor = color
    }

    fun trackPressedColor(color: Int): Slider = apply {
        trackPressedColor = color
    }
}