package xyz.meowing.vexel.utils.render.impl

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import xyz.meowing.vexel.Vexel.client as mc
import xyz.meowing.vexel.utils.render.api.MemoryApi
import xyz.meowing.vexel.utils.render.api.NanoVgApi
import xyz.meowing.vexel.utils.render.api.RenderApi
import xyz.meowing.vexel.utils.render.api.StbApi
import xyz.meowing.vexel.utils.style.*
import xyz.meowing.vexel.utils.style.Color.Companion.alpha
import xyz.meowing.vexel.utils.style.Color.Companion.blue
import xyz.meowing.vexel.utils.style.Color.Companion.green
import xyz.meowing.vexel.utils.style.Color.Companion.red
import java.awt.Color as AwtColor
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * This code was inspired by OneConfig and PolyUI's NanoVG impl.
 * Modified code, some parts of it are from OneConfig/PolyUI.
 */
class NVGRendererImpl(
    private val nanoVg: NanoVgApi,
    private val stb: StbApi,
    private val memory: MemoryApi,
    private val isOdin: Boolean = false
) : RenderApi {

    private data class NVGImage(var count: Int, val nvg: Int)
    private data class NVGFont(val id: Int, val buffer: ByteBuffer)

    private class Scissor(val previous: Scissor?, val x: Float, val y: Float, val maxX: Float, val maxY: Float) {
        fun apply(nanoVg: NanoVgApi) {
            if (previous == null) {
                nanoVg.scissor(x, y, maxX - x, maxY - y)
            } else {
                val nx = max(x, previous.x)
                val ny = max(y, previous.y)
                val width = max(0f, min(maxX, previous.maxX) - nx)
                val height = max(0f, min(maxY, previous.maxY) - ny)
                nanoVg.scissor(nx, ny, width, height)
            }
        }
    }

    private val nvgColor: Long
    private val nvgColor2: Long
    private val nvgPaint: Long

    override val defaultFont = getDefFont()

    private val fontMap = HashMap<Font, NVGFont>()
    private val fontBounds = FloatArray(4)
    private val images = HashMap<Image, NVGImage>()
    private val svgCache = HashMap<String, NVGImage>()

    private var scissor: Scissor? = null
    private var drawing = false

    init {
        nanoVg.maybeSetup()

        nvgColor = nanoVg.createColor()
        nvgColor2 = nanoVg.createColor()
        nvgPaint = nanoVg.createPaint()
    }

    private fun getDefFont(): Font {
        return try {
            Font("Default", mc.resourceManager.getResource(ResourceLocation("vexel:font.ttf")).inputStream)
        } catch (_: Exception) {
            Font("Default", "/assets/vexel/font.ttf")
        }
    }

    override fun beginFrame(width: Float, height: Float) {
        if (drawing) throw IllegalStateException("Already drawing")

        GlStateManager.pushMatrix()
        if (!mc.framebuffer.isStencilEnabled) mc.framebuffer.enableStencil()
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        GlStateManager.disableAlpha()

        nanoVg.beginFrame(width, height, 1f)
        nanoVg.textAlign(nanoVg.constants.NVG_ALIGN_LEFT or nanoVg.constants.NVG_ALIGN_TOP)
        drawing = true
    }

    override fun endFrame() {
        if (!drawing) throw IllegalStateException("Not drawing")
        nanoVg.endFrame()
        GlStateManager.enableDepth()
        GlStateManager.setActiveTexture(33984)
        GlStateManager.bindTexture(5)
        GL11.glPopAttrib()
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
        drawing = false
    }

    override fun push() = nanoVg.save()

    override fun pop() = nanoVg.restore()

    override fun scale(x: Float, y: Float) = nanoVg.scale(x, y)

    override fun translate(x: Float, y: Float) = nanoVg.translate(x, y)

    override fun rotate(amount: Float) = nanoVg.rotate(amount)

    override fun globalAlpha(amount: Float) = nanoVg.globalAlpha(amount.coerceIn(0f, 1f))

    override fun pushScissor(x: Float, y: Float, w: Float, h: Float) {
        scissor = Scissor(scissor, x, y, w + x, h + y)
        scissor?.apply(nanoVg)
    }

    override fun popScissor() {
        nanoVg.resetScissor()
        scissor = scissor?.previous
        scissor?.apply(nanoVg)
    }

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int) {
        nanoVg.beginPath()
        nanoVg.moveTo(x1, y1)
        nanoVg.lineTo(x2, y2)
        nanoVg.strokeWidth(thickness)
        setColor(color, nvgColor)
        nanoVg.strokeColor(nvgColor)
        nanoVg.stroke()
    }

    override fun drawHalfRoundedRect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float, roundTop: Boolean) {
        nanoVg.beginPath()
        if (roundTop) {
            nanoVg.moveTo(x, y + h)
            nanoVg.lineTo(x + w, y + h)
            nanoVg.lineTo(x + w, y + radius)
            nanoVg.arcTo(x + w, y, x + w - radius, y, radius)
            nanoVg.lineTo(x + radius, y)
            nanoVg.arcTo(x, y, x, y + radius, radius)
            nanoVg.lineTo(x, y + h)
        } else {
            nanoVg.moveTo(x, y)
            nanoVg.lineTo(x + w, y)
            nanoVg.lineTo(x + w, y + h - radius)
            nanoVg.arcTo(x + w, y + h, x + w - radius, y + h, radius)
            nanoVg.lineTo(x + radius, y + h)
            nanoVg.arcTo(x, y + h, x, y + h - radius, radius)
            nanoVg.lineTo(x, y)
        }
        nanoVg.closePath()
        setColor(color, nvgColor)
        nanoVg.fillColor(nvgColor)
        nanoVg.fill()
    }

    override fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, tr: Float, tl: Float, br: Float, bl: Float) {
        nanoVg.beginPath()
        nanoVg.roundedRectVarying(round(x), round(y), round(w), round(h), tr, tl, br, bl)
        setColor(color, nvgColor)
        nanoVg.fillColor(nvgColor)
        nanoVg.fill()
    }

    override fun hollowRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color: Int, radius: Float) {
        nanoVg.beginPath()
        nanoVg.roundedRect(x, y, w, h, radius)
        nanoVg.strokeWidth(thickness)
        nanoVg.pathWinding(nanoVg.constants.NVG_HOLE)
        setColor(color, nvgColor)
        nanoVg.strokeColor(nvgColor)
        nanoVg.stroke()
    }

    override fun hollowGradientRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color1: Int, color2: Int, gradient: Gradient, radius: Float) {
        nanoVg.beginPath()
        nanoVg.roundedRect(x, y, w, h, radius)
        nanoVg.strokeWidth(thickness)
        applyGradient(color1, color2, x, y, w, h, gradient)
        nanoVg.strokeColor(nvgColor)
        nanoVg.stroke()
    }

    override fun gradientRect(x: Float, y: Float, w: Float, h: Float, color1: Int, color2: Int, gradient: Gradient, radius: Float) {
        nanoVg.beginPath()
        nanoVg.roundedRect(x, y, w, h, radius)
        applyGradient(color1, color2, x, y, w, h, gradient)
        nanoVg.fillPaint(nvgPaint)
        nanoVg.fill()
    }

    override fun dropShadow(x: Float, y: Float, width: Float, height: Float, blur: Float, spread: Float, shadowColor: AwtColor, radius: Float) {
        val r = shadowColor.red.toByte()
        val g = shadowColor.green.toByte()
        val b = shadowColor.blue.toByte()

        nanoVg.rgba(nvgColor, r, g, b, 125)
        nanoVg.rgba(nvgColor2, r, g, b, 0)

        nanoVg.boxGradient(x - spread, y - spread, width + 2 * spread, height + 2 * spread, radius + spread, blur, nvgColor, nvgColor2, nvgPaint)
        nanoVg.beginPath()
        nanoVg.roundedRect(x - spread - blur, y - spread - blur, width + 2 * spread + 2 * blur, height + 2 * spread + 2 * blur, radius + spread)
        nanoVg.roundedRect(x, y, width, height, radius)
        nanoVg.pathWinding(nanoVg.constants.NVG_HOLE)
        nanoVg.fillPaint(nvgPaint)
        nanoVg.fill()
    }

    override fun circle(x: Float, y: Float, radius: Float, color: Int) {
        nanoVg.beginPath()
        nanoVg.circle(x, y, radius)
        setColor(color, nvgColor)
        nanoVg.fillColor(nvgColor)
        nanoVg.fill()
    }

    override fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font) {
        nanoVg.fontSize(size)
        nanoVg.fontFaceId(getFontID(font))
        setColor(color, nvgColor)
        nanoVg.fillColor(nvgColor)
        nanoVg.text(round(x), round(y + 0.5f), text)
    }

    override fun textShadow(text: String, x: Float, y: Float, size: Float, color: Int, font: Font) {
        nanoVg.fontFaceId(getFontID(font))
        nanoVg.fontSize(size)
        setColor(-16777216, nvgColor)
        nanoVg.fillColor(nvgColor)
        nanoVg.text(round(x + 3f), round(y + 3f), text)
        setColor(color, nvgColor)
        nanoVg.fillColor(nvgColor)
        nanoVg.text(round(x), round(y), text)
    }

    override fun textWidth(text: String, size: Float, font: Font): Float {
        nanoVg.fontSize(size)
        nanoVg.fontFaceId(getFontID(font))
        return nanoVg.textBounds(0f, 0f, text, fontBounds)
    }

    override fun drawWrappedString(text: String, x: Float, y: Float, w: Float, size: Float, color: Int, font: Font, lineHeight: Float) {
        nanoVg.fontSize(size)
        nanoVg.fontFaceId(getFontID(font))
        nanoVg.textLineHeight(lineHeight)
        setColor(color, nvgColor)
        nanoVg.fillColor(nvgColor)
        nanoVg.textBox(x, y, w, text)
    }

    override fun wrappedTextBounds(text: String, w: Float, size: Float, font: Font, lineHeight: Float): FloatArray {
        val bounds = FloatArray(4)
        nanoVg.fontSize(size)
        nanoVg.fontFaceId(getFontID(font))
        nanoVg.textLineHeight(lineHeight)
        nanoVg.textBoxBounds(0f, 0f, w, text, bounds)
        return bounds
    }

    override fun image(image: Int, textureWidth: Int, textureHeight: Int, subX: Int, subY: Int, subW: Int, subH: Int, x: Float, y: Float, w: Float, h: Float, radius: Float) {
        if (image == -1) return

        val sx = subX.toFloat() / textureWidth
        val sy = subY.toFloat() / textureHeight
        val sw = subW.toFloat() / textureWidth
        val sh = subH.toFloat() / textureHeight

        val iw = w / sw
        val ih = h / sh
        val ix = x - iw * sx
        val iy = y - ih * sy

        nanoVg.imagePattern(ix, iy, iw, ih, 0f, image, 1f, nvgPaint)
        nanoVg.beginPath()
        nanoVg.roundedRect(x, y, w, h + .5f, radius)
        nanoVg.fillPaint(nvgPaint)
        nanoVg.fill()
    }

    override fun createNVGImage(textureId: Int, textureWidth: Int, textureHeight: Int): Int =
        nanoVg.createImageFromHandle(textureId, textureWidth, textureHeight, nanoVg.constants.NVG_IMAGE_NEAREST or nanoVg.constants.NVG_IMAGE_NODELETE)

    override fun image(image: Image, x: Float, y: Float, w: Float, h: Float, tr: Float, tl: Float, br: Float, bl: Float) {
        nanoVg.imagePattern(x, y, w, h, 0f, getImage(image), 1f, nvgPaint)
        nanoVg.beginPath()
        nanoVg.roundedRectVarying(x, y, w, h + .5f, tr, tl, br, bl)
        nanoVg.fillPaint(nvgPaint)
        nanoVg.fill()
    }

    override fun createImage(resourcePath: String, width: Int, height: Int, color: AwtColor, id: String): Image {
        val image = Image(resourcePath)
        if (image.isSVG) {
            svgCache[id] = NVGImage(0, loadSVG(image, width, height, color))
            svgCache[id]!!.count++
        } else {
            images.getOrPut(image) { NVGImage(0, loadImage(image)) }.count++
        }
        return image
    }

    override fun deleteImage(image: Image) {
        val nvgImage = images[image] ?: return
        nvgImage.count--
        if (nvgImage.count == 0) {
            nanoVg.deleteImage(nvgImage.nvg)
            images.remove(image)
        }
    }

    override fun svg(id: String, x: Float, y: Float, w: Float, h: Float, a: Float) {
        val nvg = svgCache[id]?.nvg ?: throw IllegalStateException("SVG Image ($id) doesn't exist")
        nanoVg.imagePattern(x, y, w, h, 0f, nvg, a, nvgPaint)
        nanoVg.beginPath()
        nanoVg.rect(x, y, w, h + .5f)
        nanoVg.fillPaint(nvgPaint)
        nanoVg.fill()
    }

    override fun cleanCache() {
        images.entries.forEach { nanoVg.deleteImage(it.value.nvg) }
        images.clear()
        svgCache.entries.forEach { nanoVg.deleteImage(it.value.nvg) }
        svgCache.clear()
    }

    override fun deleteSVG(id: String) {
        val nvgImage = svgCache[id] ?: return
        nvgImage.count--
        if (nvgImage.count == 0) {
            nanoVg.deleteImage(nvgImage.nvg)
            svgCache.remove(id)
        }
    }

    private fun getImage(image: Image): Int =
        images.getOrPut(image) { NVGImage(0, loadImage(image)) }.nvg

    private fun loadImage(image: Image): Int {
        val w = IntArray(1)
        val h = IntArray(1)
        val channels = IntArray(1)
        val buffer = stb.loadFromMemory(image.buffer(), w, h, channels, 4)
        return nanoVg.createImageRGBA(w[0], h[0], 0, buffer)
    }

    private fun loadSVG(image: Image, svgWidth: Int, svgHeight: Int, color: AwtColor): Int {
        var vec = image.stream.use { it.bufferedReader().readText() }
        val hexColor = "#%06X".format(color.rgb and 0xFFFFFF)
        vec = vec.replace("currentColor", hexColor)

        val svgAddress = nanoVg.svgParse(vec, "px", 96f)
        val width = if (svgWidth > 0) svgWidth else nanoVg.svgWidth(svgAddress).toInt()
        val height = if (svgHeight > 0) svgHeight else nanoVg.svgHeight(svgAddress).toInt()
        val buffer = memory.memAlloc(width * height * 4)

        try {
            val scale = width.toFloat() / nanoVg.svgWidth(svgAddress)
            nanoVg.svgRasterize(nanoVg.svgHandle, svgAddress, 0f, 0f, scale, buffer, width, height, width * 4)
            return nanoVg.createImageRGBA(width, height, 0, buffer)
        } finally {
            nanoVg.svgDelete(svgAddress)
            if (isOdin) buffer.clear() else memory.memFree(buffer)
        }
    }

    private fun setColor(color: Int, address: Long) {
        nanoVg.rgba(address, color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte())
    }

    private fun setColors(color1: Int, color2: Int) {
        setColor(color1, nvgColor)
        setColor(color2, nvgColor2)
    }

    private fun applyGradient(color1: Int, color2: Int, x: Float, y: Float, w: Float, h: Float, direction: Gradient) {
        setColors(color1, color2)
        when (direction) {
            Gradient.LeftToRight -> nanoVg.linearGradient(nvgPaint, x, y, x + w, y, nvgColor, nvgColor2)
            Gradient.TopToBottom -> nanoVg.linearGradient(nvgPaint, x, y, x, y + h, nvgColor, nvgColor2)
            Gradient.TopLeftToBottomRight -> nanoVg.linearGradient(nvgPaint, x, y, x + w, y + h, nvgColor, nvgColor2)
        }
    }

    private fun getFontID(font: Font): Int {
        return fontMap.getOrPut(font) {
            val buffer = font.buffer()
            NVGFont(nanoVg.createFontMem(font.name, buffer, false), buffer)
        }.id
    }
}