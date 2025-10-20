package xyz.meowing.vexel.elements

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.fadeIn
import xyz.meowing.vexel.animations.fadeOut
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.SvgImage
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import java.awt.Color

class CheckBox(
    var checkmarkColor: Int = 0xFF4c87f9.toInt(),
    var disabledBackgroundColor: Int = 0xFF303030.toInt(),
    var enabledBackgroundColor: Int = 0xFF212121.toInt(),
    var backgroundHoverColor: Int = 0xFF424242.toInt(),
    var backgroundPressedColor: Int = 0xFF1B1B1B.toInt(),
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(4f, 4f, 4f, 4f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Pixels,
    heightType: Size = Size.Pixels
) : VexelElement<CheckBox>(widthType, heightType) {
    var checked: Boolean = false

    val background = Rectangle(
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

    val checkMark = SvgImage(svgPath = "/assets/vexel/checkmark.svg", color = Color(checkmarkColor and 0x00FFFFFF, true))
        .setSizing(120f, Size.ParentPerc, 120f, Size.ParentPerc)
        .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
        .childOf(background)

    init {
        setSizing(20f, Size.Pixels, 20f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        onClick { _, _, _ ->
            setChecked(!checked, animated = true)
            true
        }
    }

    fun setChecked(value: Boolean, animated: Boolean = true, silent: Boolean = false) {
        checked = value
        if (!checked) {
            if (animated) {
                checkMark.color = Color(checkmarkColor, true)
                checkMark.fadeOut(100, EasingType.EASE_IN)
            }
        } else if (animated) {
            checkMark.fadeIn(100, EasingType.EASE_IN)
        }
        if (!silent) onValueChange?.invoke(checked)
    }

    private fun updateBackgroundColor() {
        val newColor = when {
            !checked -> disabledBackgroundColor
            pressed -> backgroundPressedColor
            hovered -> backgroundHoverColor
            else -> enabledBackgroundColor
        }
        background.backgroundColor(newColor)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        background.isHovered = hovered
        background.isPressed = pressed
        updateBackgroundColor()
    }
}