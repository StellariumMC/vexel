package xyz.meowing.vexel.utils.render.api

import org.lwjgl.nanovg.NSVGImage
import java.nio.ByteBuffer

/**
 * This code was inspired by OneConfig and PolyUI's NanoVG impl.
 * Modified code, some parts of it are from OneConfig/PolyUI.
 */
interface NanoVgApi {

    interface NanoVgConstants {
        val NVG_ANTIALIAS: Int
        val NVG_STENCIL_STROKES: Int
        val NVG_ALIGN_LEFT: Int
        val NVG_ALIGN_TOP: Int
        val NVG_HOLE: Int
        val NVG_IMAGE_NEAREST: Int
        val NVG_IMAGE_NODELETE: Int
    }

    val constants: NanoVgConstants
    val handle: Long
    val svgHandle: Long

    fun maybeSetup()
    fun beginFrame(width: Float, height: Float, pixelRatio: Float)
    fun endFrame()

    fun createColor(): Long
    fun createPaint(): Long

    fun rgba(address: Long, r: Byte, g: Byte, b: Byte, a: Byte)
    fun fillColor(address: Long)
    fun fillPaint(address: Long)
    fun strokeColor(address: Long)
    fun strokePaint(address: Long)

    fun beginPath()
    fun fill()
    fun stroke()
    fun closePath()

    fun moveTo(x: Float, y: Float)
    fun lineTo(x: Float, y: Float)
    fun arcTo(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float)

    fun rect(x: Float, y: Float, w: Float, h: Float)
    fun roundedRect(x: Float, y: Float, w: Float, h: Float, radius: Float)
    fun roundedRectVarying(x: Float, y: Float, w: Float, h: Float, tl: Float, tr: Float, br: Float, bl: Float)
    fun circle(x: Float, y: Float, radius: Float)

    fun strokeWidth(width: Float)
    fun pathWinding(winding: Int)

    fun linearGradient(address: Long, x0: Float, y0: Float, x1: Float, y1: Float, startColor: Long, endColor: Long)
    fun boxGradient(x: Float, y: Float, w: Float, h: Float, radius: Float, feather: Float, innerColor: Long, outerColor: Long, address: Long)

    fun fontSize(size: Float)
    fun fontFaceId(id: Int)
    fun textAlign(align: Int)
    fun textLineHeight(lineHeight: Float)
    fun text(x: Float, y: Float, text: String)
    fun textBox(x: Float, y: Float, breakWidth: Float, text: String)
    fun textBounds(x: Float, y: Float, text: String, bounds: FloatArray): Float
    fun textBoxBounds(x: Float, y: Float, breakWidth: Float, text: String, bounds: FloatArray)
    fun createFontMem(name: String, buffer: ByteBuffer, freeData: Boolean): Int

    fun createImageRGBA(width: Int, height: Int, flags: Int, buffer: ByteBuffer): Int
    fun createImageFromHandle(textureId: Int, width: Int, height: Int, flags: Int): Int
    fun deleteImage(image: Int)
    fun imagePattern(x: Float, y: Float, w: Float, h: Float, angle: Float, image: Int, alpha: Float, address: Long)

    fun scissor(x: Float, y: Float, w: Float, h: Float)
    fun resetScissor()

    fun save()
    fun restore()
    fun scale(x: Float, y: Float)
    fun translate(x: Float, y: Float)
    fun rotate(angle: Float)
    fun globalAlpha(alpha: Float)

    fun svgParse(data: String, units: String, dpi: Float): NSVGImage
    fun svgWidth(address: NSVGImage): Float
    fun svgHeight(address: NSVGImage): Float
    fun svgDelete(address: NSVGImage)
    fun svgCreateRasterizer(): Long
    fun svgDeleteRasterizer(address: Long)
    fun svgRasterize(rasterizerAddress: Long, svgAddress: NSVGImage, x: Float, y: Float, scale: Float, buffer: ByteBuffer, width: Int, height: Int, stride: Int)
}