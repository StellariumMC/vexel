package xyz.meowing.vexel.components.core

import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer
import xyz.meowing.vexel.utils.style.Font

class Text(
    var text: String = "",
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var fontSize: Float = 12f,
    var shadowEnabled: Boolean = false,
    var font: Font = NVGRenderer.defaultFont
) : VexelElement<Text>() {

    init {
        setSizing(Size.Auto, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        ignoreMouseEvents()
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (text.isEmpty()) return

        if (shadowEnabled) NVGRenderer.textShadow(text, x, y, fontSize, textColor, font)
        else NVGRenderer.text(text, x, y, fontSize, textColor, font)
    }

    override fun getAutoWidth(): Float = NVGRenderer.textWidth(text, fontSize, font)

    override fun getAutoHeight(): Float = fontSize

    fun text(newText: String): Text = apply {
        text = newText
    }

    fun color(color: Int): Text = apply {
        textColor = color
    }

    fun fontSize(size: Float): Text = apply {
        fontSize = size
    }

    fun font(newFont: Font): Text = apply {
        font = newFont
    }

    fun shadow(enabled: Boolean = true): Text = apply {
        shadowEnabled = enabled
    }
}