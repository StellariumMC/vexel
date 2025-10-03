package xyz.meowing.vexel.utils.render.impl

import org.lwjgl.nanovg.*
import org.lwjgl.system.MemoryUtil
import xyz.meowing.vexel.utils.render.api.NanoVgApi
import java.nio.ByteBuffer

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
private typealias JBoolean = java.lang.Boolean

class NanoVgImpl(private val isOpenGl3: JBoolean) : NanoVgApi {

    inner class NanoVgConstantsImpl : NanoVgApi.NanoVgConstants {
        override val NVG_ANTIALIAS = 1 shl 0
        override val NVG_STENCIL_STROKES = 1 shl 1
        override val NVG_ALIGN_LEFT = 1 shl 0
        override val NVG_ALIGN_TOP = 1 shl 3
        override val NVG_HOLE = 2
        override val NVG_IMAGE_NEAREST = 1 shl 2
        override val NVG_IMAGE_NODELETE = 1 shl 4
    }

    override val constants by lazy { NanoVgConstantsImpl() }
    override var handle: Long = -1L
        private set
    override var svgHandle: Long = -1L
        private set

    override fun maybeSetup() {
        val handle = when (isOpenGl3.booleanValue()) {
            true -> NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS)
            false -> NanoVGGL2.nvgCreate(NanoVGGL2.NVG_ANTIALIAS)
        }

        if (handle == MemoryUtil.NULL) {
            throw IllegalStateException("Failed to create NanoVG context")
        }

        val svgHandle = NanoSVG.nsvgCreateRasterizer()
        if (svgHandle == MemoryUtil.NULL) {
            throw IllegalStateException("Failed to create NanoSVG context")
        }

        this.handle = handle
        this.svgHandle = svgHandle
    }

    override fun beginFrame(width: Float, height: Float, pixelRatio: Float) {
        NanoVG.nvgBeginFrame(handle, width, height, pixelRatio)
    }

    override fun endFrame() {
        NanoVG.nvgEndFrame(handle)
    }

    override fun createColor(): Long = NVGColor.malloc().address()

    override fun createPaint(): Long = NVGPaint.malloc().address()

    override fun rgba(address: Long, r: Byte, g: Byte, b: Byte, a: Byte) {
        NanoVG.nvgRGBA(r, g, b, a, NVGColor.create(address))
    }

    override fun fillColor(address: Long) {
        NanoVG.nvgFillColor(handle, NVGColor.create(address))
    }

    override fun fillPaint(address: Long) {
        NanoVG.nvgFillPaint(handle, NVGPaint.create(address))
    }

    override fun strokeColor(address: Long) {
        NanoVG.nvgStrokeColor(handle, NVGColor.create(address))
    }

    override fun strokePaint(address: Long) {
        NanoVG.nvgStrokePaint(handle, NVGPaint.create(address))
    }

    override fun beginPath() = NanoVG.nvgBeginPath(handle)

    override fun fill() = NanoVG.nvgFill(handle)

    override fun stroke() = NanoVG.nvgStroke(handle)

    override fun closePath() = NanoVG.nvgClosePath(handle)

    override fun moveTo(x: Float, y: Float) = NanoVG.nvgMoveTo(handle, x, y)

    override fun lineTo(x: Float, y: Float) = NanoVG.nvgLineTo(handle, x, y)

    override fun arcTo(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float) {
        NanoVG.nvgArcTo(handle, x1, y1, x2, y2, radius)
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float) {
        NanoVG.nvgRect(handle, x, y, w, h)
    }

    override fun roundedRect(x: Float, y: Float, w: Float, h: Float, radius: Float) {
        NanoVG.nvgRoundedRect(handle, x, y, w, h, radius)
    }

    override fun roundedRectVarying(x: Float, y: Float, w: Float, h: Float, tl: Float, tr: Float, br: Float, bl: Float) {
        NanoVG.nvgRoundedRectVarying(handle, x, y, w, h, tl, tr, br, bl)
    }

    override fun circle(x: Float, y: Float, radius: Float) {
        NanoVG.nvgCircle(handle, x, y, radius)
    }

    override fun strokeWidth(width: Float) = NanoVG.nvgStrokeWidth(handle, width)

    override fun pathWinding(winding: Int) = NanoVG.nvgPathWinding(handle, winding)

    override fun linearGradient(address: Long, x0: Float, y0: Float, x1: Float, y1: Float, startColor: Long, endColor: Long) {
        NanoVG.nvgLinearGradient(handle, x0, y0, x1, y1, NVGColor.create(startColor), NVGColor.create(endColor), NVGPaint.create(address))
    }

    override fun boxGradient(x: Float, y: Float, w: Float, h: Float, radius: Float, feather: Float, innerColor: Long, outerColor: Long, address: Long) {
        NanoVG.nvgBoxGradient(handle, x, y, w, h, radius, feather, NVGColor.create(innerColor), NVGColor.create(outerColor), NVGPaint.create(address))
    }

    override fun fontSize(size: Float) = NanoVG.nvgFontSize(handle, size)

    override fun fontFaceId(id: Int) = NanoVG.nvgFontFaceId(handle, id)

    override fun textAlign(align: Int) = NanoVG.nvgTextAlign(handle, align)

    override fun textLineHeight(lineHeight: Float) = NanoVG.nvgTextLineHeight(handle, lineHeight)

    override fun text(x: Float, y: Float, text: String) {
        NanoVG.nvgText(handle, x, y, text)
    }

    override fun textBox(x: Float, y: Float, breakWidth: Float, text: String) {
        NanoVG.nvgTextBox(handle, x, y, breakWidth, text)
    }

    override fun textBounds(x: Float, y: Float, text: String, bounds: FloatArray): Float {
        return NanoVG.nvgTextBounds(handle, x, y, text, bounds)
    }

    override fun textBoxBounds(x: Float, y: Float, breakWidth: Float, text: String, bounds: FloatArray) {
        NanoVG.nvgTextBoxBounds(handle, x, y, breakWidth, text, bounds)
    }

    override fun createFontMem(name: String, buffer: ByteBuffer, freeData: Boolean): Int {
        return NanoVG.nvgCreateFontMem(handle, name, buffer, freeData)
    }

    override fun createImageRGBA(width: Int, height: Int, flags: Int, buffer: ByteBuffer): Int {
        return NanoVG.nvgCreateImageRGBA(handle, width, height, flags, buffer)
    }

    override fun createImageFromHandle(textureId: Int, width: Int, height: Int, flags: Int): Int {
        return NanoVGGL3.nvglCreateImageFromHandle(handle, textureId, width, height, flags)
    }

    override fun deleteImage(image: Int) = NanoVG.nvgDeleteImage(handle, image)

    override fun imagePattern(x: Float, y: Float, w: Float, h: Float, angle: Float, image: Int, alpha: Float, address: Long) {
        NanoVG.nvgImagePattern(handle, x, y, w, h, angle, image, alpha, NVGPaint.create(address))
    }

    override fun scissor(x: Float, y: Float, w: Float, h: Float) {
        NanoVG.nvgScissor(handle, x, y, w, h)
    }

    override fun resetScissor() = NanoVG.nvgResetScissor(handle)

    override fun save() = NanoVG.nvgSave(handle)

    override fun restore() = NanoVG.nvgRestore(handle)

    override fun scale(x: Float, y: Float) = NanoVG.nvgScale(handle, x, y)

    override fun translate(x: Float, y: Float) = NanoVG.nvgTranslate(handle, x, y)

    override fun rotate(angle: Float) = NanoVG.nvgRotate(handle, angle)

    override fun globalAlpha(alpha: Float) = NanoVG.nvgGlobalAlpha(handle, alpha)

    override fun svgParse(data: String, units: String, dpi: Float): NSVGImage {
        return NanoSVG.nsvgParse(data, units, dpi) ?: throw IllegalStateException("Failed to parse SVG")
    }

    override fun svgWidth(address: NSVGImage): Float = NSVGImage.create(address.address()).width()

    override fun svgHeight(address: NSVGImage): Float = NSVGImage.create(address.address()).height()

    override fun svgDelete(address: NSVGImage) = NanoSVG.nsvgDelete(address)

    override fun svgCreateRasterizer(): Long = NanoSVG.nsvgCreateRasterizer()

    override fun svgDeleteRasterizer(address: Long) = NanoSVG.nsvgDeleteRasterizer(address)

    override fun svgRasterize(rasterizerAddress: Long, svgAddress: NSVGImage, x: Float, y: Float, scale: Float, buffer: ByteBuffer, width: Int, height: Int, stride: Int) {
        NanoSVG.nsvgRasterize(rasterizerAddress, NSVGImage.create(svgAddress.address()), x, y, scale, buffer, width, height, stride)
    }
}