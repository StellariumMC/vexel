package xyz.meowing.vexel.utils.render.api

import xyz.meowing.vexel.utils.style.Font
import xyz.meowing.vexel.utils.style.Gradient
import xyz.meowing.vexel.utils.style.Image
import java.awt.Color as AwtColor

interface RenderApi {
    val defaultFont: Font

    fun beginFrame(width: Float, height: Float)

    fun endFrame()

    fun push()

    fun pop()

    fun scale(x: Float, y: Float)

    fun translate(x: Float, y: Float)

    fun rotate(amount: Float)

    fun globalAlpha(amount: Float)

    fun pushScissor(x: Float, y: Float, w: Float, h: Float)

    fun popScissor()

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int)

    fun drawHalfRoundedRect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float, roundTop: Boolean)

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, tr: Float = 0f, tl: Float = tr, br: Float = tr, bl: Float = tr)

    fun hollowRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color: Int, radius: Float)

    fun hollowGradientRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color1: Int, color2: Int, gradient: Gradient, radius: Float)

    fun gradientRect(x: Float, y: Float, w: Float, h: Float, color1: Int, color2: Int, gradient: Gradient, radius: Float)

    fun dropShadow(x: Float, y: Float, width: Float, height: Float, blur: Float, spread: Float, shadowColor: AwtColor, radius: Float)

    fun circle(x: Float, y: Float, radius: Float, color: Int)

    fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font)

    fun textShadow(text: String, x: Float, y: Float, size: Float, color: Int, font: Font)

    fun textWidth(text: String, size: Float, font: Font): Float

    fun drawWrappedString(text: String, x: Float, y: Float, w: Float, size: Float, color: Int, font: Font, lineHeight: Float = 1f)

    fun wrappedTextBounds(text: String, w: Float, size: Float, font: Font, lineHeight: Float = 1f): FloatArray

    fun image(image: Int, textureWidth: Int, textureHeight: Int, subX: Int, subY: Int, subW: Int, subH: Int, x: Float, y: Float, w: Float, h: Float, radius: Float)

    fun createNVGImage(textureId: Int, textureWidth: Int, textureHeight: Int): Int

    fun image(image: Image, x: Float, y: Float, w: Float, h: Float, tr: Float = 0f, tl: Float = tr, br: Float = tr, bl: Float = tr)

    fun createImage(resourcePath: String, width: Int = -1, height: Int = -1, color: AwtColor = AwtColor.WHITE, id: String): Image

    fun deleteImage(image: Image)

    fun svg(id: String, x: Float, y: Float, w: Float, h: Float, a: Float = 1f)

    fun cleanCache()

    fun deleteSVG(id: String)
}