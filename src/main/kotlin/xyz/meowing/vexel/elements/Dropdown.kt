package xyz.meowing.vexel.elements

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.fadeIn
import xyz.meowing.vexel.animations.fadeOut
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.SvgImage
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class Dropdown(
    var options: List<String>,
    var selectedIndex: Int = 0,
    backgroundColor: Int = 0xFF282e3a.toInt(),
    iconColor: Int = 0xFF4c87f9.toInt(),
    borderColor: Int = 0xFF0194d8.toInt(),
    borderRadius: Float = 6f,
    borderThickness: Float = 2f,
    padding: FloatArray = floatArrayOf(6f, 6f, 6f, 6f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Pixels,
    heightType: Size = Size.Pixels
) : VexelElement<Dropdown>(widthType, heightType) {
    var fontSize = 12f
    private var isPickerOpen = false
    private var isAnimating = false
    private var lastPosition = Pair(0f, 0f)

    private val previewRect = Rectangle(
        backgroundColor,
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
        .setGradientBorderColor(0xFF0194d8.toInt(), 0xFF062897.toInt())
        .childOf(this)

    private val selectedText = Text(options[selectedIndex], 0xFFFFFFFF.toInt(), fontSize)
        .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
        .childOf(previewRect)

    val dropdownArrow = SvgImage(svgPath = "/assets/vexel/dropdown.svg", color = Color(iconColor))
        .setSizing(20f, Size.Pixels, 20f, Size.Pixels)
        .setPositioning(80f, Pos.ParentPercent, 0f, Pos.ParentCenter)
        .childOf(previewRect)

    private var pickerPanel: DropDownPanel? = null

    init {
        setSizing(180f, Size.Pixels, 0f, Size.Auto)
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

        dropdownArrow.rotateTo(180f, 200)

        val currentX = previewRect.getScreenX()
        val currentY = previewRect.getScreenY() + previewRect.height + 4f
        lastPosition = Pair(currentX, currentY)

        pickerPanel = DropDownPanel(selectedIndex, options, fontSize = fontSize, sourceDropdown = previewRect)
            .setSizing(previewRect.width, Size.Pixels, 0f, Size.Auto)
            .setPositioning(currentX, Pos.ScreenPixels, currentY, Pos.ScreenPixels)
            .childOf(getRootElement())

        pickerPanel?.onValueChange { index ->
            selectedIndex = index as Int
            selectedText.text = options[selectedIndex]
            closePicker()
            onValueChange?.invoke(index)
        }

        pickerPanel?.backgroundPopup?.fadeIn(200, EasingType.EASE_OUT) {
            isAnimating = false
        }

        isPickerOpen = true
    }

    override fun getAutoWidth(): Float {
        return previewRect.getAutoWidth()
    }

    override fun getAutoHeight(): Float {
        return previewRect.getAutoHeight()
    }

    private fun closePicker() {
        if (!isPickerOpen || pickerPanel == null || isAnimating) return
        isAnimating = true

        dropdownArrow.rotateTo(0f, 200)

        pickerPanel?.backgroundPopup?.fadeOut(200, EasingType.EASE_IN) {
            getRootElement().children.remove(pickerPanel!!)
            pickerPanel!!.destroy()
            pickerPanel = null
            isAnimating = false
        }

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
        val currentY = previewRect.getScreenY() + previewRect.height + 4f

        if (lastPosition.first != currentX || lastPosition.second != currentY) {
            lastPosition = Pair(currentX, currentY)
            pickerPanel?.setPositioning(currentX, Pos.ScreenPixels, currentY, Pos.ScreenPixels)
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        previewRect.isHovered = hovered
        previewRect.isPressed = pressed

        if (isPickerOpen && pickerPanel != null) {
            if (!previewRect.isVisibleInScrollableParents()) closePicker() else updatePickerPosition()
        }
    }

    override fun destroy() {
        if (isPickerOpen) closePicker()
        super.destroy()
    }

    fun fontSize(size: Float): Dropdown = apply {
        fontSize = size
        selectedText.fontSize = size
    }
}

class DropDownPanel(
    selectedIndex: Int,
    var options: List<String> = listOf(),
    backgroundColor: Int = 0xFF333741.toInt(),
    borderColor: Int = 0xFF3e414b.toInt(),
    selectedColor: Int = 0xFF2a2f35.toInt(),
    fontSize: Float = 12f,
    private val sourceDropdown: Rectangle
) : VexelElement<DropDownPanel>() {
    val backgroundPopup = Rectangle(backgroundColor, borderColor, 8f, 1f, floatArrayOf(7f, 7f, 7f, 7f))
        .setSizing(width, widthType, 120f, Size.Pixels)
        .scrollable(true)
        .childOf(this)
        .dropShadow()

    var optionHeight = 0f

    init {
        setSizing(Size.Auto, Size.Auto)
        setFloating()

        options.forEachIndexed { index, option ->
            val rect = Rectangle(if (index == selectedIndex) selectedColor else backgroundColor, borderColor, 5f, 0f, floatArrayOf(5f, 5f, 5f, 5f), hoverColor = 0x80505050.toInt())
                .setSizing(100f, Size.ParentPerc, 0f, Size.Auto)
                .setPositioning(0f, Pos.ParentPixels, 1f, Pos.AfterSibling)
                .onClick { _, _, _ ->
                    onValueChange?.invoke(index)
                    true
                }
                .childOf(backgroundPopup)

            Text(option, 0xFFFFFFFF.toInt(), fontSize)
                .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
                .childOf(rect)
                .ignoreMouseEvents()

            rect.updateHeight()
            optionHeight = rect.getAutoHeight() * 2.5f
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        backgroundPopup.visible = sourceDropdown.isVisibleInScrollableParents()

        if (backgroundPopup.visible) {
            backgroundPopup.setSizing(width, widthType, min(max(70f, 22 + (options.size * optionHeight)), 140f), Size.Pixels)
        }
    }

    override fun getAutoHeight(): Float {
        return backgroundPopup.height
    }
}