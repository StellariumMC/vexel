@file:Suppress("UNUSED")

package xyz.meowing.vexel.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.util.Identifier
import org.lwjgl.nanovg.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.vexel.utils.style.Color.Companion.alpha
import xyz.meowing.vexel.utils.style.Color.Companion.blue
import xyz.meowing.vexel.utils.style.Color.Companion.green
import xyz.meowing.vexel.utils.style.Color.Companion.red
import xyz.meowing.vexel.utils.style.Font
import xyz.meowing.vexel.utils.style.Gradient
import xyz.meowing.vexel.utils.style.Image
import java.awt.Color
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

//#if MC > 1.20.1
import net.minecraft.client.gl.GlBackend
import net.minecraft.client.texture.GlTexture
import com.mojang.blaze3d.opengl.GlStateManager
import kotlin.math.round

//#else
//$$ import com.mojang.blaze3d.platform.GlStateManager
//#endif

//#if MC >= 1.21.9
//$$ import org.lwjgl.opengl.GL13
//#endif

/**
 * Implementation adapted from Odin by odtheking
 * Original work: https://github.com/odtheking/Odin
 * Modified to support Vexel
 *
 * @author Odin Contributors
 */
object NVGRenderer {
    private val nvgPaint = NVGPaint.malloc()
    private val nvgColor = NVGColor.malloc()
    private val nvgColor2 = NVGColor.malloc()

    val defaultFont =
        Font("Default", client.resourceManager.getResource(
            //#if MC <= 1.20.1 && FORGE-LIKE
            //$$ net.minecraft.resources.ResourceLocation("vexel", "font.ttf")
            //#elseif MC <= 1.20.1 && FABRIC
            //$$ Identifier.of("vexel", "font.ttf")
            //#else
            Identifier.of("vexel", "font.ttf")
            //#endif
        ).get().inputStream)

    private val fontMap = HashMap<Font, NVGFont>()
    private val fontBounds = FloatArray(4)

    private val images = HashMap<Image, NVGImage>()
    private val svgCache = HashMap<String, NVGImage>()

    private var scissor: Scissor? = null

    private var vg = -1L

    init {
        vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS or NanoVGGL3.NVG_STENCIL_STROKES)
        require(vg != -1L) { "Failed to initialize NanoVG" }
    }

    fun beginFrame(width: Float, height: Float) {
        if (StateTracker.drawing) throw IllegalStateException("[NVGRenderer] Already drawing, but called beginFrame")

        //#if MC >= 1.21.9
        //$$ StateTracker.previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)
        //#else
        StateTracker.previousActiveTexture = GlStateManager._getActiveTexture()
        //#endif

        StateTracker.previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)

        val framebuffer = client.framebuffer ?: return

        if (
            vg == -1L ||
            //#if MC <= 1.20.1 && FORGE-LIKE
            //$$ framebuffer.colorTextureId == -1
            //#elseif MC <= 1.20.1 && FABRIC
            //$$ framebuffer.colorAttachment == null
            //#else
            framebuffer.colorAttachment == null
            //#endif
        ) return

        val glFramebuffer =
            //#if MC <= 1.20.1
                //#if FORGE-LIKE
                //$$ framebuffer.frameBufferId
                //#else
                //$$ framebuffer.fbo
                //#endif
            //#else
            (framebuffer.colorAttachment as GlTexture).getOrCreateFramebuffer(
                //#if MC >= 1.21.9
                    //#if FORGE-LIKE
                    //$$ (RenderSystem.getDevice() as GlDevice).directStateAccess(),
                    //#else
                    //$$ (RenderSystem.getDevice() as GlBackend).bufferManager,
                    //#endif
                //#else
                (RenderSystem.getDevice() as GlBackend).framebufferManager,
                //#endif
                null
            )
            //#endif

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, glFramebuffer)
        GlStateManager._viewport(0, 0, framebuffer.textureWidth, framebuffer.textureHeight)
        GlStateManager._activeTexture(GL30.GL_TEXTURE0)

        NanoVG.nvgBeginFrame(vg, width, height, 1f)
        NanoVG.nvgTextAlign(vg, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_TOP)
        StateTracker.drawing = true
    }

    fun endFrame() {
        if (!StateTracker.drawing) throw IllegalStateException("[NVGRenderer] Not drawing, but called endFrame")
        NanoVG.nvgEndFrame(vg)

        GlStateManager._disableCull() // default states that mc expects
        GlStateManager._disableDepthTest()
        GlStateManager._enableBlend()
        GlStateManager._blendFuncSeparate(770, 771, 1, 0)

        if (StateTracker.previousProgram != -1) GlStateManager._glUseProgram(StateTracker.previousProgram) // fixes invalid program errors when using NVG

        if (StateTracker.previousActiveTexture != -1) { // prevents issues with gui background rendering
            GlStateManager._activeTexture(StateTracker.previousActiveTexture)
            if (StateTracker.previousBoundTexture != -1) GlStateManager._bindTexture(StateTracker.previousBoundTexture)
        }

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0) // fixes macos issues

        //#if MC <= 1.20.1
        //#if FORGE-LIKE
        //$$ client.mainRenderTarget.bindWrite(true)
        //#else
        //$$ client.framebuffer?.beginWrite(true)
        //#endif
        //#endif
        StateTracker.drawing = false
    }

    fun push() = NanoVG.nvgSave(vg)

    fun pop() = NanoVG.nvgRestore(vg)

    fun scale(x: Float, y: Float) = NanoVG.nvgScale(vg, x, y)

    fun translate(x: Float, y: Float) = NanoVG.nvgTranslate(vg, x, y)

    fun rotate(amount: Float) = NanoVG.nvgRotate(vg, amount)

    fun globalAlpha(amount: Float) = NanoVG.nvgGlobalAlpha(vg, amount.coerceIn(0f, 1f))

    fun pushScissor(x: Float, y: Float, w: Float, h: Float) {
        scissor = Scissor(scissor, x, y, w + x, h + y)
        scissor?.applyScissor()
    }

    fun popScissor() {
        NanoVG.nvgResetScissor(vg)
        scissor = scissor?.previous
        scissor?.applyScissor()
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float, color: Int) {
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgMoveTo(vg, x1, y1)
        NanoVG.nvgLineTo(vg, x2, y2)
        NanoVG.nvgStrokeWidth(vg, thickness)
        color(color)
        NanoVG.nvgStrokeColor(vg, nvgColor)
        NanoVG.nvgStroke(vg)
    }

    fun drawHalfRoundedRect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float, roundTop: Boolean) {
        NanoVG.nvgBeginPath(vg)

        if (roundTop) {
            NanoVG.nvgMoveTo(vg, x, y + h)
            NanoVG.nvgLineTo(vg, x + w, y + h)
            NanoVG.nvgLineTo(vg, x + w, y + radius)
            NanoVG.nvgArcTo(vg, x + w, y, x + w - radius, y, radius)
            NanoVG.nvgLineTo(vg, x + radius, y)
            NanoVG.nvgArcTo(vg, x, y, x, y + radius, radius)
            NanoVG.nvgLineTo(vg, x, y + h)
        } else {
            NanoVG.nvgMoveTo(vg, x, y)
            NanoVG.nvgLineTo(vg, x + w, y)
            NanoVG.nvgLineTo(vg, x + w, y + h - radius)
            NanoVG.nvgArcTo(vg, x + w, y + h, x + w - radius, y + h, radius)
            NanoVG.nvgLineTo(vg, x + radius, y + h)
            NanoVG.nvgArcTo(vg, x, y + h, x, y + h - radius, radius)
            NanoVG.nvgLineTo(vg, x, y)
        }

        NanoVG.nvgClosePath(vg)
        color(color)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgFill(vg)
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float) {
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRoundedRect(vg, x, y, w, h + .5f, radius)
        color(color)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgFill(vg)
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRect(vg, x, y, w, h + .5f)
        color(color)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgFill(vg)
    }

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, topRight: Float, topLeft: Float, bottomRight: Float, bottomLeft: Float) {
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRoundedRectVarying(vg, round(x), round(y), round(w), round(h), topRight, topLeft, bottomRight, bottomLeft)
        color(color)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgFill(vg)
    }

    fun hollowRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color: Int, radius: Float) {
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRoundedRect(vg, x, y, w, h, radius)
        NanoVG.nvgStrokeWidth(vg, thickness)
        NanoVG.nvgPathWinding(vg, NanoVG.NVG_HOLE)
        color(color)
        NanoVG.nvgStrokeColor(vg, nvgColor)
        NanoVG.nvgStroke(vg)
    }

    fun hollowGradientRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        thickness: Float,
        color1: Int,
        color2: Int,
        gradient: Gradient,
        radius: Float
    ) {
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRoundedRect(vg, x, y, w, h, radius)
        NanoVG.nvgStrokeWidth(vg, thickness)

        // Gradient stroke
        gradient(color1, color2, x, y, w, h, gradient)
        NanoVG.nvgStrokePaint(vg, nvgPaint)
        NanoVG.nvgStroke(vg)
    }

    fun gradientRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color1: Int,
        color2: Int,
        gradient: Gradient,
        radius: Float
    ) {
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRoundedRect(vg, x, y, w, h, radius)
        gradient(color1, color2, x, y, w, h, gradient)
        NanoVG.nvgFillPaint(vg, nvgPaint)
        NanoVG.nvgFill(vg)
    }

    fun dropShadow(x: Float, y: Float, width: Float, height: Float, blur: Float, spread: Float, shadowColor: Color, radius: Float) {
        val r = shadowColor.red.toByte()
        val g = shadowColor.green.toByte()
        val b = shadowColor.blue.toByte()

        NanoVG.nvgRGBA(r, g, b, 125, nvgColor)
        NanoVG.nvgRGBA(r, g, b, 0, nvgColor2)

        NanoVG.nvgBoxGradient(
            vg,
            x - spread,
            y - spread,
            width + 2 * spread,
            height + 2 * spread,
            radius + spread,
            blur,
            nvgColor,
            nvgColor2,
            nvgPaint
        )
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRoundedRect(
            vg,
            x - spread - blur,
            y - spread - blur,
            width + 2 * spread + 2 * blur,
            height + 2 * spread + 2 * blur,
            radius + spread
        )
        NanoVG.nvgRoundedRect(vg, x, y, width, height, radius)
        NanoVG.nvgPathWinding(vg, NanoVG.NVG_HOLE)
        NanoVG.nvgFillPaint(vg, nvgPaint)
        NanoVG.nvgFill(vg)
    }

    fun circle(x: Float, y: Float, radius: Float, color: Int) {
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgCircle(vg, x, y, radius)
        color(color)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgFill(vg)
    }

    fun text(text: String, x: Float, y: Float, size: Float, color: Int, font: Font = defaultFont) {
        NanoVG.nvgFontSize(vg, size)
        NanoVG.nvgFontFaceId(vg, getFontID(font))
        color(color)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgText(vg, x, y + .5f, text)
    }

    fun textShadow(text: String, x: Float, y: Float, size: Float, color: Int, font: Font = defaultFont, shadowColor: Int = 0x80000000.toInt(), offsetX: Float = 1.5f, offsetY: Float = 1.5f, blur: Float = 2f) {
        NanoVG.nvgFontFaceId(vg, getFontID(font))
        NanoVG.nvgFontSize(vg, size)

        NanoVG.nvgFontBlur(vg, blur)
        color(shadowColor)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgText(vg, x + offsetX, y + offsetY, text)

        NanoVG.nvgFontBlur(vg, 0f)
        color(color)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgText(vg, x, y + .5f, text)
    }

    fun textWidth(text: String, size: Float, font: Font): Float {
        NanoVG.nvgFontSize(vg, size)
        NanoVG.nvgFontFaceId(vg, getFontID(font))
        return NanoVG.nvgTextBounds(vg, 0f, 0f, text, fontBounds)
    }

    fun drawWrappedString(
        text: String,
        x: Float,
        y: Float,
        w: Float,
        size: Float,
        color: Int,
        font: Font,
        lineHeight: Float = 1f
    ) {
        NanoVG.nvgFontSize(vg, size)
        NanoVG.nvgFontFaceId(vg, getFontID(font))
        NanoVG.nvgTextLineHeight(vg, lineHeight)
        color(color)
        NanoVG.nvgFillColor(vg, nvgColor)
        NanoVG.nvgTextBox(vg, x, y, w, text)
    }

    fun wrappedTextBounds(
        text: String,
        w: Float,
        size: Float,
        font: Font,
        lineHeight: Float = 1f
    ): FloatArray {
        val bounds = FloatArray(4)
        NanoVG.nvgFontSize(vg, size)
        NanoVG.nvgFontFaceId(vg, getFontID(font))
        NanoVG.nvgTextLineHeight(vg, lineHeight)
        NanoVG.nvgTextBoxBounds(vg, 0f, 0f, w, text, bounds)
        return bounds // [minX, minY, maxX, maxY]
    }

    fun createNVGImage(textureId: Int, textureWidth: Int, textureHeight: Int): Int =
        NanoVGGL3.nvglCreateImageFromHandle(
            vg,
            textureId,
            textureWidth,
            textureHeight,
            NanoVG.NVG_IMAGE_NEAREST or NanoVGGL3.NVG_IMAGE_NODELETE
        )

    fun image(image: Int, textureWidth: Int, textureHeight: Int, subX: Int, subY: Int, subW: Int, subH: Int, x: Float, y: Float, w: Float, h: Float, radius: Float) {
        if (image == -1) return

        val sx = subX.toFloat() / textureWidth
        val sy = subY.toFloat() / textureHeight
        val sw = subW.toFloat() / textureWidth
        val sh = subH.toFloat() / textureHeight

        val iw = w / sw
        val ih = h / sh
        val ix = x - iw * sx
        val iy = y - ih * sy

        NanoVG.nvgImagePattern(vg, ix, iy, iw, ih, 0f, image, 1f, nvgPaint)
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRoundedRect(vg, x, y, w, h + .5f, radius)
        NanoVG.nvgFillPaint(vg, nvgPaint)
        NanoVG.nvgFill(vg)
    }

    fun image(image: Image, x: Float, y: Float, w: Float, h: Float, radius: Float) {
        NanoVG.nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), 1f, nvgPaint)
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRoundedRect(vg, x, y, w, h + .5f, radius)
        NanoVG.nvgFillPaint(vg, nvgPaint)
        NanoVG.nvgFill(vg)
    }

    fun image(image: Image, x: Float, y: Float, w: Float, h: Float) {
        NanoVG.nvgImagePattern(vg, x, y, w, h, 0f, getImage(image), 1f, nvgPaint)
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRect(vg, x, y, w, h + .5f)
        NanoVG.nvgFillPaint(vg, nvgPaint)
        NanoVG.nvgFill(vg)
    }

    fun svg(id: String, x: Float, y: Float, w: Float, h: Float, a: Float = 1f) {
        val nvg = svgCache[id]?.nvg ?: throw IllegalStateException("SVG Image (${id}) doesn't exist")

        NanoVG.nvgImagePattern(vg, x, y, w, h, 0f, nvg, a, nvgPaint)
        NanoVG.nvgBeginPath(vg)
        NanoVG.nvgRect(vg, x, y, w, h + .5f)
        NanoVG.nvgFillPaint(vg, nvgPaint)
        NanoVG.nvgFill(vg)
    }

    // Might have a memory leak if the menu is left and re-entered lots of times with unique ids
    fun createImage(resourcePath: String, width: Int = -1, height: Int = -1, color: Color = Color.WHITE, id: String): Image {
        val image = Image(resourcePath)

        if (image.isSVG) {
            svgCache[id] = NVGImage(0, loadSVG(image, width, height, color))
            svgCache[id]!!.count++
        } else images.getOrPut(image) { NVGImage(0, loadImage(image)) }.count++
        return image
    }

    fun cleanCache() {
        val iter = images.entries.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            NanoVG.nvgDeleteImage(vg, entry.value.nvg)
            iter.remove()
        }

        val svgIter = svgCache.entries.iterator()
        while (svgIter.hasNext()) {
            val entry = svgIter.next()
            NanoVG.nvgDeleteImage(vg, entry.value.nvg)
            svgIter.remove()
        }
    }

    // lowers reference count by 1, if it reaches 0 it gets deleted from mem
    fun deleteImage(image: Image) {
        val nvgImage = images[image] ?: return
        nvgImage.count--
        if (nvgImage.count == 0) {
            NanoVG.nvgDeleteImage(vg, nvgImage.nvg)
            images.remove(image)
        }
    }

    // Not used anywhere yet, but will prob have to use it at some point
    fun deleteSVG(id: String) {
        val nvgImage = svgCache[id] ?: return
        nvgImage.count--
        if (nvgImage.count == 0) {
            NanoVG.nvgDeleteImage(vg, nvgImage.nvg)
            svgCache.remove(id)
        }
    }

    private fun getImage(image: Image): Int {
        return images[image]?.nvg ?: throw IllegalStateException("Image (${image.identifier}) doesn't exist")
    }

    private fun loadImage(image: Image): Int {
        val w = IntArray(1)
        val h = IntArray(1)
        val channels = IntArray(1)
        val buffer = STBImage.stbi_load_from_memory(
            image.buffer(),
            w,
            h,
            channels,
            4
        ) ?: throw NullPointerException("Failed to load image: ${image.identifier}")
        return NanoVG.nvgCreateImageRGBA(vg, w[0], h[0], 0, buffer)
    }

    private fun loadSVG(image: Image, svgWidth: Int, svgHeight: Int, color: Color): Int {
        var vec = image.stream.use { it.bufferedReader().readText() }

        val hexColor = "#%06X".format(color.rgb and 0xFFFFFF)
        vec = vec.replace("currentColor", hexColor)

        val svg = NanoSVG.nsvgParse(vec, "px", 96f)
            ?: throw IllegalStateException("Failed to parse ${image.identifier}")

        val width = if (svgWidth > 0) svgWidth else svg.width().toInt()
        val height = if (svgHeight > 0) svgHeight else svg.height().toInt()
        val buffer = MemoryUtil.memAlloc(width * height * 4)

        val previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)

        try {
            val rasterizer = NanoSVG.nsvgCreateRasterizer()
            NanoSVG.nsvgRasterize(rasterizer, svg, 0f, 0f, width.toFloat() / svg.width(), buffer, width, height, width * 4)
            val nvgImage = NanoVG.nvgCreateImageRGBA(vg, width, height, 0, buffer)
            NanoSVG.nsvgDeleteRasterizer(rasterizer)

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture)

            return nvgImage
        } finally {
            NanoSVG.nsvgDelete(svg)
            MemoryUtil.memFree(buffer)
        }
    }

    private fun color(color: Int) {
        NanoVG.nvgRGBA(color.red.toByte(), color.green.toByte(), color.blue.toByte(), color.alpha.toByte(), nvgColor)
    }

    private fun color(color1: Int, color2: Int) {
        NanoVG.nvgRGBA(
            color1.red.toByte(),
            color1.green.toByte(),
            color1.blue.toByte(),
            color1.alpha.toByte(),
            nvgColor
        )
        NanoVG.nvgRGBA(
            color2.red.toByte(),
            color2.green.toByte(),
            color2.blue.toByte(),
            color2.alpha.toByte(),
            nvgColor2
        )
    }

    private fun gradient(color1: Int, color2: Int, x: Float, y: Float, w: Float, h: Float, direction: Gradient) {
        color(color1, color2)
        when (direction) {
            Gradient.LeftToRight -> NanoVG.nvgLinearGradient(vg, x, y, x + w, y, nvgColor, nvgColor2, nvgPaint)
            Gradient.TopToBottom -> NanoVG.nvgLinearGradient(vg, x, y, x, y + h, nvgColor, nvgColor2, nvgPaint)
            Gradient.TopLeftToBottomRight -> NanoVG.nvgLinearGradient(vg, x, y, x + w, y + h, nvgColor, nvgColor2, nvgPaint)
        }
    }

    private fun getFontID(font: Font): Int {
        return fontMap.getOrPut(font) {
            val buffer = font.buffer()
            NVGFont(NanoVG.nvgCreateFontMem(
                vg,
                font.name,
                buffer,
                //#if MC <= 1.20.1
                //$$ 0
                //#else
                false
                //#endif
            ), buffer)
        }.id
    }

    private class Scissor(val previous: Scissor?, val x: Float, val y: Float, val maxX: Float, val maxY: Float) {
        fun applyScissor() {
            if (previous == null) NanoVG.nvgScissor(vg, x, y, maxX - x, maxY - y)
            else {
                val x = max(x, previous.x)
                val y = max(y, previous.y)
                val width = max(0f, (min(maxX, previous.maxX) - x))
                val height = max(0f, (min(maxY, previous.maxY) - y))
                NanoVG.nvgScissor(vg, x, y, width, height)
            }
        }
    }

    private data class NVGImage(var count: Int, val nvg: Int)
    private data class NVGFont(val id: Int, val buffer: ByteBuffer)
}