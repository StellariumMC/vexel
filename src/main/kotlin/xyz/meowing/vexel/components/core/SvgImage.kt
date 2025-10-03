package xyz.meowing.vexel.components.core

import xyz.meowing.vexel.Vexel.renderEngine
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateFloat
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import java.awt.Color
import java.util.UUID

class SvgImage(
    var svgPath: String = "",
    var startingWidth: Float = 80f,
    var startingHeight: Float = 80f,
    var color: Color = Color.WHITE
) : VexelElement<SvgImage>() {
    var imageId = "${UUID.randomUUID()}"
    var image = renderEngine.createImage(svgPath, startingWidth.toInt(), startingHeight.toInt(), color, imageId)
    var rotation: Float = 0f

    init {
        width = startingWidth
        height = startingHeight
        setSizing(Size.Auto, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        ignoreMouseEvents()
        setSizing(startingWidth, Size.Pixels, startingHeight, Size.Pixels)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (svgPath.isEmpty()) return

        startingWidth = width
        startingHeight = height

        val centerX = x + width / 2f
        val centerY = y + height / 2f

        if (rotation != 0f) {
            renderEngine.push()
            renderEngine.translate(centerX, centerY)
            renderEngine.rotate(Math.toRadians(rotation.toDouble()).toFloat())
            renderEngine.translate(-centerX, -centerY)
        }

        renderEngine.svg(imageId, x, y, startingWidth, startingHeight, color.alpha / 255f)

        if (rotation != 0f) {
            renderEngine.pop()
        }
    }

    fun rotateTo(angle: Float, duration: Long = 300, type: EasingType = EasingType.EASE_OUT, onComplete: (() -> Unit)? = null): SvgImage {
        animateFloat({ rotation }, { rotation = it }, angle, duration, type, onComplete = onComplete)
        return this
    }

    fun setSvgColor(newColor: Color) {
        if (color != newColor) {
            color = newColor
            reloadImage()
        }
    }

    private fun reloadImage() {
        image = renderEngine.createImage(svgPath, startingWidth.toInt(), startingHeight.toInt(), color, imageId)
    }
}