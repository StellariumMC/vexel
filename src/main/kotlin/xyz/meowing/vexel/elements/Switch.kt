package xyz.meowing.vexel.elements

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animatePosition
import xyz.meowing.vexel.animations.colorTo
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement

class Switch(
    var thumbColor: Int = 0xFFFFFFFF.toInt(),
    var thumbDisabledColor: Int = 0xFF9E9E9E.toInt(),
    var trackEnabledColor: Int = 0xFF4c87f9.toInt(),
    var trackDisabledColor: Int = 0xFF424242.toInt(),
    var trackHoverColor: Int? = null,
    var trackPressedColor: Int? = null,
    thumbWidth: Float? = null,
    thumbHeight: Float? = null,
    thumbRadius: Float = 13f,
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 15f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(2f, 2f, 2f, 2f),
    hoverColor: Int? = null,
    pressedColor: Int? = null,
    widthType: Size = Size.Pixels,
    heightType: Size = Size.Pixels
) : VexelElement<Switch>(widthType, heightType) {
    var enabled: Boolean = false
    
    private val track = Rectangle(
        backgroundColor,
        borderColor,
        borderRadius,
        borderThickness,
        padding,
        hoverColor,
        pressedColor,
        Size.ParentPerc,
        Size.ParentPerc
    )
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .ignoreMouseEvents()
        .childOf(this)
    
    private val thumb = Rectangle(thumbColor, 0x00000000, thumbRadius, 0f, floatArrayOf(0f, 0f, 0f, 0f), null, null, Size.Pixels, Size.Pixels)
        .ignoreMouseEvents()
        .childOf(track)
    
    init {
        setSizing(50f, Size.Pixels, 26f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)

        val thumbWidth = thumbWidth ?: (height - 8f)
        val thumbHeight = thumbHeight ?: (height - 8f)
        thumb.setSizing(thumbWidth, Size.Pixels, thumbHeight, Size.Pixels)
        thumb.setPositioning(2f, Pos.ParentPixels, 0f, Pos.ParentCenter)
        
        onClick { _, _, _ ->
            setEnabled(!enabled, animated = true)
            true
        }
    }
    
    fun setEnabled(value: Boolean, animated: Boolean = true, silent: Boolean = false): Switch {
        enabled = value
        
        val thumbTargetX = if (enabled) width - thumb.width - 6f else 2f
        val trackTargetColor = if (enabled) trackEnabledColor else trackDisabledColor
        val thumbTargetColor = if (enabled) thumbColor else thumbDisabledColor
        
        if (animated) {
            thumb.animatePosition(thumbTargetX, 0f, 200, EasingType.EASE_OUT)
            track.colorTo(trackTargetColor, 200, EasingType.EASE_OUT)
            thumb.colorTo(thumbTargetColor, 200, EasingType.EASE_OUT)
        } else {
            thumb.xConstraint = thumbTargetX
            track.backgroundColor = trackTargetColor
            thumb.backgroundColor = thumbTargetColor
        }
        
        if (!silent) onValueChange?.invoke(enabled)

        return this
    }
    
    private fun updateTrackColor() {
        val baseColor = if (enabled) trackEnabledColor else trackDisabledColor
        val currentColor = when {
            pressed && trackPressedColor != null -> trackPressedColor!!
            hovered && trackHoverColor != null -> trackHoverColor!!
            else -> baseColor
        }
        track.backgroundColor = currentColor
    }
    
    override fun onRender(mouseX: Float, mouseY: Float) {
        track.isHovered = hovered
        track.isPressed = pressed
        updateTrackColor()
    }
    
    fun thumbColor(color: Int): Switch = apply {
        thumbColor = color
        if (enabled) thumb.backgroundColor = color
    }
    
    fun thumbDisabledColor(color: Int): Switch = apply {
        thumbDisabledColor = color
        if (!enabled) thumb.backgroundColor = color
    }
    
    fun trackEnabledColor(color: Int): Switch = apply {
        trackEnabledColor = color
        if (enabled) track.backgroundColor = color
    }
    
    fun trackDisabledColor(color: Int): Switch = apply {
        trackDisabledColor = color
        if (!enabled) track.backgroundColor = color
    }
    
    fun trackHoverColor(color: Int): Switch = apply {
        trackHoverColor = color
    }
    
    fun trackPressedColor(color: Int): Switch = apply {
        trackPressedColor = color
    }

    fun borderThickness(thickness: Float): Switch = apply {
        track.borderThickness = thickness
    }

    fun borderColor(color: Int): Switch = apply {
        track.borderColor = color
    }

    fun borderRadius(radius: Float): Switch = apply {
        track.borderRadius = radius
    }
}