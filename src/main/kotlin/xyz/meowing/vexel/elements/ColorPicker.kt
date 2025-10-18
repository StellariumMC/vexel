package xyz.meowing.vexel.elements

import xyz.meowing.vexel.Vexel.renderEngine
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateFloat
import xyz.meowing.vexel.animations.fadeIn
import xyz.meowing.vexel.animations.fadeOut
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.style.Gradient
import java.awt.Color
import kotlin.math.roundToInt

class ColorPicker(
    initialColor: Color = Color.WHITE,
    val backgroundColor: Int = 0xFF171616.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(4f, 4f, 4f, 4f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Pixels,
    heightType: Size = Size.Pixels
) : VexelElement<ColorPicker>(widthType, heightType) {
    var selectedColor: Color = initialColor
    private var isPickerOpen = false
    private var isAnimating = false
    private var lastPosition = Pair(0f, 0f)

    private val previewRect = Rectangle(
        selectedColor.rgb,
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

    private var pickerPanel: ColorPickerPanel? = null

    init {
        setSizing(30f, Size.Pixels, 20f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)

        onClick { _, _, _ ->
            if (!isAnimating) togglePicker()
            true
        }
    }

    private fun togglePicker() {
        if (isPickerOpen) closePicker() else openPicker()
    }

    private fun openPicker() {
        if (isPickerOpen || isAnimating) return
        isAnimating = true

        val currentX = previewRect.getScreenX()
        val currentY = previewRect.getScreenY() + previewRect.height + 3f
        lastPosition = Pair(currentX, currentY)

        pickerPanel = ColorPickerPanel(selectedColor, backgroundColor, sourceColorPicker = previewRect)
            .setSizing(Size.Auto, Size.Auto)
            .setPositioning(currentX, Pos.ScreenPixels, currentY, Pos.ScreenPixels)
            .setFloating()
            .childOf(getRootElement())

        pickerPanel?.onValueChange { color ->
            selectedColor = color as Color
            previewRect.backgroundColor = color.rgb
            onValueChange?.invoke(color)
        }

        pickerPanel?.background?.fadeIn(200, EasingType.EASE_OUT) {
            isAnimating = false
        }

        pickerPanel?.animateFloat(
            { 0f },
            { alpha ->
                pickerPanel?.pickerArea?.alpha = alpha
                pickerPanel?.hueSlider?.alpha = alpha
                pickerPanel?.alphaSlider?.alpha = alpha
            },
            1f, 200, EasingType.EASE_OUT
        )
        isPickerOpen = true
    }

    private fun closePicker() {
        if (!isPickerOpen || pickerPanel == null || isAnimating) return
        isAnimating = true

        pickerPanel?.background?.fadeOut(200, EasingType.EASE_IN) {
            getRootElement().children.remove(pickerPanel!!)
            pickerPanel!!.destroy()
            pickerPanel = null
            isAnimating = false
        }

        pickerPanel?.animateFloat(
            { 1f },
            { alpha ->
                pickerPanel?.pickerArea?.alpha = alpha
                pickerPanel?.hueSlider?.alpha = alpha
                pickerPanel?.alphaSlider?.alpha = alpha
            },
            0f, 200, EasingType.EASE_IN
        )
        isPickerOpen = false
    }

    override fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        val handled = super.handleMouseClick(mouseX, mouseY, button)

        if (isPickerOpen && pickerPanel != null && !pickerPanel!!.isPointInside(mouseX, mouseY) && !isPointInside(mouseX, mouseY) && !isAnimating) {
            closePicker()
        }

        return handled
    }

    private fun updatePickerPosition() {
        val currentX = previewRect.getScreenX()
        val currentY = previewRect.getScreenY() + previewRect.height + 3f

        if (lastPosition.first != currentX || lastPosition.second != currentY) {
            lastPosition = Pair(currentX, currentY)
            pickerPanel?.setPositioning(currentX, Pos.ScreenPixels, currentY, Pos.ScreenPixels)
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        previewRect.isHovered = hovered
        previewRect.isPressed = pressed

        previewRect.hoverColor = selectedColor.darker().rgb
        previewRect.pressedColor = selectedColor.darker().rgb

        if (isPickerOpen && pickerPanel != null) {
            if (!previewRect.isVisibleInScrollableParents()) {
                closePicker()
            } else {
                updatePickerPosition()
            }
        }
    }

    override fun destroy() {
        if (isPickerOpen) closePicker()
        super.destroy()
    }

    fun setColor(color: Color, silent: Boolean = false): ColorPicker {
        selectedColor = color
        previewRect.backgroundColor = color.rgb
        if (!silent) onValueChange?.invoke(color)
        return this
    }
}

private class ColorPickerPanel(
    initialColor: Color,
    backgroundColor: Int = 0xFF171616.toInt(),
    borderColor: Int = 0xFF505050.toInt(),
    private val sourceColorPicker: Rectangle
) : VexelElement<ColorPickerPanel>() {
    private var currentColor = initialColor
    private var currentHue: Float
    private var currentSaturation: Float
    private var currentBrightness: Float
    private var currentAlpha = initialColor.alpha / 255f
    private var draggingPicker = false
    private var draggingHue = false
    private var draggingAlpha = false

    val background = Rectangle(backgroundColor, borderColor, 2f, 1f, floatArrayOf(8f, 8f, 8f, 8f))
        .setSizing(0f, Size.Auto, 170f, Size.Pixels)
        .ignoreMouseEvents()
        .childOf(this)

    val pickerArea = ColorPickerArea()
        .setSizing(150f, Size.Pixels, 95f, Size.ParentPerc)
        .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
        .childOf(background)

    val hueSlider = HueSlider()
        .setSizing(20f, Size.Pixels, 95f, Size.ParentPerc)
        .setPositioning(Pos.AfterSibling, Pos.ParentCenter)
        .setOffset(5f, 0f)
        .childOf(background)

    val alphaSlider = AlphaSlider()
        .setSizing(20f, Size.Pixels, 95f, Size.ParentPerc)
        .setPositioning(Pos.AfterSibling, Pos.ParentCenter)
        .setOffset(5f, 0f)
        .childOf(background)

    init {
        val hsb = Color.RGBtoHSB(initialColor.red, initialColor.green, initialColor.blue, null)
        currentHue = hsb[0]
        currentSaturation = hsb[1]
        currentBrightness = hsb[2]

        setupInteractions()
        updateColor()
    }

    private fun setupInteractions() {
        pickerArea.onMouseClick { mouseX, mouseY, _ ->
            draggingPicker = true
            updatePickerFromMouse(mouseX, mouseY)
            true
        }

        hueSlider.onMouseClick { _, mouseY, _ ->
            draggingHue = true
            updateHueFromMouse(mouseY)
            true
        }

        alphaSlider.onMouseClick { _, mouseY, _ ->
            draggingAlpha = true
            updateAlphaFromMouse(mouseY)
            true
        }
    }

    override fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        val result = super.handleMouseMove(mouseX, mouseY)

        when {
            draggingPicker -> updatePickerFromMouse(mouseX, mouseY)
            draggingHue -> updateHueFromMouse(mouseY)
            draggingAlpha -> updateAlphaFromMouse(mouseY)
        }

        return result
    }

    override fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        val result = super.handleMouseRelease(mouseX, mouseY, button)

        if (button == 0) {
            draggingPicker = false
            draggingHue = false
            draggingAlpha = false
        }

        return result
    }

    private fun updatePickerFromMouse(mouseX: Float, mouseY: Float) {
        val relativeX = (mouseX - pickerArea.x) / pickerArea.width
        val relativeY = (mouseY - pickerArea.y) / pickerArea.height

        currentSaturation = relativeX.coerceIn(0f, 1f)
        currentBrightness = (1f - relativeY).coerceIn(0f, 1f)

        updateColor()
    }

    private fun updateHueFromMouse(mouseY: Float) {
        val relativeY = (mouseY - hueSlider.y) / hueSlider.height
        currentHue = relativeY.coerceIn(0f, 1f)
        updateColor()
    }

    private fun updateAlphaFromMouse(mouseY: Float) {
        val relativeY = (mouseY - alphaSlider.y) / alphaSlider.height
        currentAlpha = (1f - relativeY).coerceIn(0f, 1f)
        updateColor()
    }

    private fun updateColor() {
        val rgb = Color.HSBtoRGB(currentHue, currentSaturation, currentBrightness)
        val baseColor = Color(rgb)
        val alpha = (currentAlpha * 255).roundToInt().coerceIn(0, 255)

        currentColor = Color(baseColor.red, baseColor.green, baseColor.blue, alpha)

        pickerArea.currentHue = currentHue
        alphaSlider.currentColor = Color(baseColor.red, baseColor.green, baseColor.blue)

        onValueChange?.invoke(currentColor)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        background.visible = sourceColorPicker.isVisibleInScrollableParents()
    }

    inner class ColorPickerArea : VexelElement<ColorPickerArea>() {
        var currentHue = 0f
        var alpha = 1f

        override fun onRender(mouseX: Float, mouseY: Float) {
            if (alpha <= 0f) return

            renderEngine.globalAlpha(alpha)

            val hueColor = Color.HSBtoRGB(currentHue, 1f, 1f)
            val whiteColor = 0xFFFFFFFF.toInt()
            val blackColor = 0xFF000000.toInt()

            renderEngine.gradientRect(x, y, width, height, whiteColor, hueColor, Gradient.LeftToRight, 0f)
            renderEngine.gradientRect(x, y, width, height + 1f, 0x00000000, blackColor, Gradient.TopToBottom, 0f)

            val indicatorX = x + currentSaturation * width - 3f
            val indicatorY = y + (1f - currentBrightness) * height - 3f
            renderEngine.hollowRect(indicatorX, indicatorY, 6f, 6f, 2f, 0xFFFFFFFF.toInt(), 2f)

            renderEngine.globalAlpha(1f)
        }
    }

    inner class HueSlider : VexelElement<HueSlider>() {
        var alpha = 1f

        override fun onRender(mouseX: Float, mouseY: Float) {
            if (alpha <= 0f) return

            renderEngine.globalAlpha(alpha)

            val steps = (height / 1f).toInt()
            val stepHeight = height / steps

            for (i in 0 until steps) {
                val hue = i.toFloat() / steps
                val rgb = Color.HSBtoRGB(hue, 1f, 1f)
                val color = Color(rgb)

                val rectY = y + i * stepHeight
                renderEngine.rect(x, rectY, width, stepHeight, color.rgb, 0f)
            }

            val indicatorY = y + currentHue * height - 2f
            renderEngine.rect(x - 3f, indicatorY, width + 6f, 4f, 0xFFFFFFFF.toInt(), 3f)

            renderEngine.globalAlpha(1f)
        }
    }

    inner class AlphaSlider : VexelElement<AlphaSlider>() {
        var currentColor: Color = Color.WHITE
        var alpha = 1f

        override fun onRender(mouseX: Float, mouseY: Float) {
            if (alpha <= 0f) return

            renderEngine.globalAlpha(alpha)

            val checkerSize = 6f
            val cols = (width / checkerSize).toInt() + 1
            val rows = (height / checkerSize).toInt() + 1

            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val checkerColor = if ((row + col) % 2 == 0) 0xFF404040.toInt() else 0xFF606060.toInt()
                    val rectX = x + col * checkerSize
                    val rectY = y + row * checkerSize
                    val rectWidth = if (rectX + checkerSize > x + width) x + width - rectX else checkerSize
                    val rectHeight = if (rectY + checkerSize > y + height) y + height - rectY else checkerSize

                    if (rectWidth > 0 && rectHeight > 0) {
                        renderEngine.rect(rectX, rectY, rectWidth, rectHeight, checkerColor, 0f)
                    }
                }
            }

            val opaqueColor = Color(currentColor.red, currentColor.green, currentColor.blue, 255).rgb
            val transparentColor = Color(currentColor.red, currentColor.green, currentColor.blue, 0).rgb

            renderEngine.gradientRect(x, y, width, height, opaqueColor, transparentColor, Gradient.TopToBottom, 0f)

            val indicatorY = y + (1f - currentAlpha) * height - 2f
            renderEngine.rect(x - 3f, indicatorY, width + 6f, 4f, 0xFFFFFFFF.toInt(), 2f)

            renderEngine.globalAlpha(1f)
        }
    }
}