package xyz.meowing.vexel.elements

import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.style.Font
import xyz.meowing.vexel.utils.render.NVGRenderer

class Button(
    var text: String = "",
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var hoverTextColor: Int? = null,
    var pressedTextColor: Int? = null,
    fontSize: Float = 12f,
    font: Font = NVGRenderer.defaultFont,
    shadowEnabled: Boolean = false,
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(8f, 16f, 8f, 16f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : VexelElement<Button>(widthType, heightType) {
    private val background = Rectangle(
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

    val innerText = Text(text, textColor, fontSize, shadowEnabled, font)
        .childOf(background)
        .setPositioning(Pos.ParentCenter, Pos.ParentCenter)

    init {
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        background.isHovered = hovered
        background.isPressed = pressed

        if (text.isNotEmpty()) {
            val currentTextColor = when {
                pressed && pressedTextColor != null -> pressedTextColor!!
                hovered && hoverTextColor != null -> hoverTextColor!!
                else -> textColor
            }
            innerText.textColor = currentTextColor
        }
    }

    fun text(text: String): Button = apply {
        innerText.text = text
    }

    fun textColor(color: Int): Button = apply {
        this.textColor = color
        innerText.textColor = color
    }

    fun fontSize(size: Float): Button = apply {
        innerText.fontSize = size
    }

    fun font(font: Font): Button = apply {
        innerText.font = font
    }

    fun hoverColors(bg: Int? = null, text: Int? = null): Button = apply {
        background.hoverColor = bg
        this.hoverTextColor = text
    }

    fun pressedColors(bg: Int? = null, text: Int? = null): Button = apply {
        background.pressedColor = bg
        this.pressedTextColor = text
    }

    fun shadow(enabled: Boolean = true): Button = apply {
        innerText.shadowEnabled = enabled
    }

    fun padding(top: Float, right: Float, bottom: Float, left: Float): Button = apply {
        background.padding(top, right, bottom, left)
    }

    fun padding(all: Float): Button = apply {
        background.padding(all)
    }

    fun backgroundColor(color: Int): Button = apply {
        background.backgroundColor(color)
    }

    fun borderColor(color: Int): Button = apply {
        background.borderColor(color)
    }

    fun borderRadius(radius: Float): Button = apply {
        background.borderRadius(radius)
    }

    fun borderThickness(thickness: Float): Button = apply {
        background.borderThickness(thickness)
    }

    fun hoverColor(color: Int): Button = apply {
        background.hoverColor(color)
    }

    fun pressedColor(color: Int): Button = apply {
        background.pressedColor(color)
    }

    override fun getAutoWidth(): Float = background.getAutoWidth()
    override fun getAutoHeight(): Float = background.getAutoHeight()
}