package xyz.meowing.vexel.components.core

import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement

class Tooltip(
    backgroundColor: Int = 0xFF1e1e1e.toInt(),
    borderColor: Int = 0xFF555759.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    var padding: FloatArray = floatArrayOf(4f, 4f, 4f, 4f),
    hoverColor: Int? = 0xFF1e1e1e.toInt(),
    pressedColor: Int? = 0xFF1e1e1e.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : VexelElement<Tooltip>(widthType, heightType) {
    private val backgroundRect = Rectangle(
        backgroundColor,
        borderColor,
        borderRadius,
        borderThickness,
        padding,
        hoverColor,
        pressedColor,
        widthType,
        heightType
    )
        .childOf(this)

    val innerText = Text("Tooltip", 0xFFFFFFFF.toInt(), 12f)
        .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
        .childOf(backgroundRect)

    init {
        setSizing(Size.Auto, Size.Auto)
        var parentPadding = if (parent is Rectangle) (parent as Rectangle).padding else floatArrayOf(0f, 0f, 0f, 0f)
        setPositioning(0f, Pos.ParentCenter, -24f - parentPadding[0], Pos.ParentPixels)
        ignoreMouseEvents()
        setFloating()
        backgroundRect.visible = false
        innerText.visible = false
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (visible) {
            backgroundRect.visible = true
            backgroundRect.width = width
            backgroundRect.height = height
        } else {
            backgroundRect.visible = false
        }
    }

    override fun getAutoWidth(): Float {
        return backgroundRect.getAutoWidth()
    }

    override fun getAutoHeight(): Float {
        return backgroundRect.getAutoHeight()
    }
}