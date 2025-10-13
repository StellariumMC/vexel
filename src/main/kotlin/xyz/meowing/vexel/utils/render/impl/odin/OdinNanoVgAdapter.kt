package xyz.meowing.vexel.utils.render.impl.odin

import me.odin.lwjgl.Lwjgl3Wrapper
import me.odin.lwjgl.NanoSVGImageWrapper
import org.lwjgl.nanovg.NSVGImage
import xyz.meowing.vexel.utils.render.api.NanoVgApi
import java.nio.ByteBuffer

class OdinNanoVgAdapter(private val wrapper: Lwjgl3Wrapper) : NanoVgApi {

    inner class OdinNanoVgConstantsImpl : NanoVgApi.NanoVgConstants {
        override val NVG_ANTIALIAS = 1 shl 0
        override val NVG_STENCIL_STROKES = 1 shl 1
        override val NVG_ALIGN_LEFT = 1 shl 0
        override val NVG_ALIGN_TOP = 1 shl 3
        override val NVG_HOLE = 2
        override val NVG_IMAGE_NEAREST = 1 shl 2
        override val NVG_IMAGE_NODELETE = 1 shl 4
    }

    override val constants = OdinNanoVgConstantsImpl()
    override var handle: Long = -1L
        private set
    override var svgHandle: Long = -1L
        private set

    private val colorMap = mutableMapOf<Long, me.odin.lwjgl.NanoVGColorWrapper>()
    private val paintMap = mutableMapOf<Long, me.odin.lwjgl.NanoVGPaintWrapper>()
    private val svgMap = mutableMapOf<Long, NanoSVGImageWrapper>()
    private var nextId = 0L

    override fun maybeSetup() {
        handle = wrapper.nvgCreate(constants.NVG_ANTIALIAS)
        svgHandle = wrapper.nsvgCreateRasterizer()
    }

    override fun beginFrame(width: Float, height: Float, pixelRatio: Float) {
        wrapper.nvgBeginFrame(handle, width, height, pixelRatio)
    }

    override fun endFrame() {
        wrapper.nvgEndFrame(handle)
    }

    override fun createColor(): Long {
        val id = nextId++
        colorMap[id] = wrapper.createColor()
        return id
    }

    override fun createPaint(): Long {
        val id = nextId++
        paintMap[id] = wrapper.createPaint()
        return id
    }

    override fun rgba(address: Long, r: Byte, g: Byte, b: Byte, a: Byte) {
        colorMap[address]?.let { wrapper.nvgRGBA(r, g, b, a, it) }
    }

    override fun fillColor(address: Long) {
        colorMap[address]?.let { wrapper.nvgFillColor(handle, it) }
    }

    override fun fillPaint(address: Long) {
        paintMap[address]?.let { wrapper.nvgFillPaint(handle, it) }
    }

    override fun strokeColor(address: Long) {
        colorMap[address]?.let { wrapper.nvgStrokeColor(handle, it) }
    }

    override fun strokePaint(address: Long) {
        paintMap[address]?.let { wrapper.nvgFillPaint(handle, it) }
    }

    override fun beginPath() = wrapper.nvgBeginPath(handle)

    override fun fill() = wrapper.nvgFill(handle)

    override fun stroke() = wrapper.nvgStroke(handle)

    override fun closePath() = wrapper.nvgClosePath(handle)

    override fun moveTo(x: Float, y: Float) = wrapper.nvgMoveTo(handle, x, y)

    override fun lineTo(x: Float, y: Float) = wrapper.nvgLineTo(handle, x, y)

    override fun arcTo(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float) {
        wrapper.nvgArcTo(handle, x1, y1, x2, y2, radius)
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float) {
        wrapper.nvgRect(handle, x, y, w, h)
    }

    override fun roundedRect(x: Float, y: Float, w: Float, h: Float, radius: Float) {
        wrapper.nvgRoundedRect(handle, x, y, w, h, radius)
    }

    override fun roundedRectVarying(x: Float, y: Float, w: Float, h: Float, tl: Float, tr: Float, br: Float, bl: Float) {
        wrapper.nvgRoundedRectVarying(handle, x, y, w, h, tl, tr, br, bl)
    }

    override fun circle(x: Float, y: Float, radius: Float) {
        wrapper.nvgCircle(handle, x, y, radius)
    }

    override fun strokeWidth(width: Float) = wrapper.nvgStrokeWidth(handle, width)

    override fun pathWinding(winding: Int) = wrapper.nvgPathWinding(handle, winding)

    override fun linearGradient(address: Long, x0: Float, y0: Float, x1: Float, y1: Float, startColor: Long, endColor: Long) {
        val paint = paintMap[address] ?: return
        val start = colorMap[startColor] ?: return
        val end = colorMap[endColor] ?: return
        wrapper.nvgLinearGradient(handle, x0, y0, x1, y1, start, end, paint)
    }

    override fun boxGradient(x: Float, y: Float, w: Float, h: Float, radius: Float, feather: Float, innerColor: Long, outerColor: Long, address: Long) {
        val paint = paintMap[address] ?: return
        val inner = colorMap[innerColor] ?: return
        val outer = colorMap[outerColor] ?: return
        wrapper.nvgBoxGradient(handle, x, y, w, h, radius, feather, inner, outer, paint)
    }

    override fun fontSize(size: Float) = wrapper.nvgFontSize(handle, size)

    override fun fontFaceId(id: Int) = wrapper.nvgFontFaceId(handle, id)

    override fun textAlign(align: Int) = wrapper.nvgTextAlign(handle, align)

    override fun textLineHeight(lineHeight: Float) = wrapper.nvgTextLineHeight(handle, lineHeight)

    override fun text(x: Float, y: Float, text: String) {
        wrapper.nvgText(handle, x, y, text)
    }

    override fun textBox(x: Float, y: Float, breakWidth: Float, text: String) {
        wrapper.nvgTextBox(handle, x, y, breakWidth, text)
    }

    override fun textBounds(x: Float, y: Float, text: String, bounds: FloatArray): Float {
        return wrapper.nvgTextBounds(handle, x, y, text, bounds)
    }

    override fun textBoxBounds(x: Float, y: Float, breakWidth: Float, text: String, bounds: FloatArray) {
        wrapper.nvgTextBoxBounds(handle, x, y, breakWidth, text, bounds)
    }

    override fun createFontMem(name: String, buffer: ByteBuffer, freeData: Boolean): Int {
        return wrapper.nvgCreateFontMem(handle, name, buffer, if (freeData) 1 else 0)
    }

    override fun createImageRGBA(width: Int, height: Int, flags: Int, buffer: ByteBuffer): Int {
        return wrapper.nvgCreateImageRGBA(handle, width, height, flags, buffer)
    }

    override fun createImageFromHandle(textureId: Int, width: Int, height: Int, flags: Int): Int {
        return wrapper.nvglCreateImageFromHandle(handle, textureId, width, height, flags)
    }

    override fun deleteImage(image: Int) = wrapper.nvgDeleteImage(handle, image)

    override fun imagePattern(x: Float, y: Float, w: Float, h: Float, angle: Float, image: Int, alpha: Float, address: Long) {
        paintMap[address]?.let { wrapper.nvgImagePattern(handle, x, y, w, h, angle, image, alpha, it) }
    }

    override fun scissor(x: Float, y: Float, w: Float, h: Float) {
        wrapper.nvgScissor(handle, x, y, w, h)
    }

    override fun resetScissor() = wrapper.nvgResetScissor(handle)

    override fun save() = wrapper.nvgSave(handle)

    override fun restore() = wrapper.nvgRestore(handle)

    override fun scale(x: Float, y: Float) = wrapper.nvgScale(handle, x, y)

    override fun translate(x: Float, y: Float) = wrapper.nvgTranslate(handle, x, y)

    override fun rotate(angle: Float) = wrapper.nvgRotate(handle, angle)

    override fun globalAlpha(alpha: Float) = wrapper.nvgGlobalAlpha(handle, alpha)

    override fun svgParse(data: String, units: String, dpi: Float): NSVGImage {
        val id = nextId++
        val svg = wrapper.nsvgParse(data, units, dpi)
        svgMap[id] = svg
        return NSVGImage.create(id)
    }

    override fun svgWidth(address: NSVGImage): Float {
        return svgMap[address.address()]?.width() ?: 0f
    }

    override fun svgHeight(address: NSVGImage): Float {
        return svgMap[address.address()]?.height() ?: 0f
    }

    override fun svgDelete(address: NSVGImage) {
        svgMap[address.address()]?.let { wrapper.nsvgDelete(it) }
        svgMap.remove(address.address())
    }

    override fun svgCreateRasterizer(): Long = wrapper.nsvgCreateRasterizer()

    override fun svgDeleteRasterizer(address: Long) = wrapper.nsvgDeleteRasterizer(address)

    override fun svgRasterize(rasterizerAddress: Long, svgAddress: NSVGImage, x: Float, y: Float, scale: Float, buffer: ByteBuffer, width: Int, height: Int, stride: Int) {
        svgMap[svgAddress.address()]?.let {
            wrapper.nsvgRasterize(rasterizerAddress, it, x, y, scale, buffer, width, height, stride)
        }
    }
}