package xyz.meowing.vexel.elements

import xyz.meowing.knit.api.input.KnitInputs
import xyz.meowing.knit.api.input.KnitKeyboard
import xyz.meowing.knit.api.input.KnitKeys
import xyz.meowing.vexel.Vexel.mc
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class TextInput(
    initialValue: String = "",
    var placeholder: String = "",
    var fontSize: Float = 12f,
    selectionColor: Int = 0x80aac7ff.toInt(),
    var textColor: Int = 0xFFFFFFFF.toInt(),
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(6f, 6f, 6f, 6f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80505050.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : VexelElement<TextInput>(widthType, heightType) {
    var value = initialValue
        set(newVal) {
            if (field == newVal) return
            field = newVal
            cursorIndex = cursorIndex.coerceIn(0, field.length)
            selectionAnchor = selectionAnchor.coerceIn(0, field.length)
            onValueChange?.invoke(field)
        }

    private var isDragging = false
    private var caretVisible = true
    private var lastBlink = System.currentTimeMillis()
    private val caretBlinkRate = 500L

    private var cursorIndex = value.length
    private var selectionAnchor = value.length

    private val selectionStart: Int get() = min(cursorIndex, selectionAnchor)
    private val selectionEnd: Int get() = max(cursorIndex, selectionAnchor)
    private val hasSelection: Boolean get() = selectionStart != selectionEnd

    var scrollOffset = 0f
    private var lastClickTime = 0L
    private var clickCount = 0

    private val background = Rectangle(backgroundColor, borderColor, borderRadius, borderThickness, padding, hoverColor, pressedColor, Size.ParentPerc, Size.ParentPerc)
        .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
        .ignoreMouseEvents()
        .childOf(this)

    private val innerText = Text(placeholder, textColor, fontSize)
        .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
        .childOf(background)

    private val selectionRectangle = Rectangle(selectionColor, 0x00000000, 0f, 0f)
        .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
        .setSizing(Size.Pixels, Size.Pixels)
        .ignoreMouseEvents()
        .ignoreFocus()
        .childOf(background)

    private val caret = Rectangle(0xFFFFFFFF.toInt(), 0xFF000000.toInt(), 1f, 0f)
        .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
        .setSizing(Size.Pixels, Size.Pixels)
        .ignoreMouseEvents()
        .ignoreFocus()
        .childOf(background)

    init {
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        setRequiresFocus()

        onClick { mouseX, mouseY, button ->
            if (button != 0) return@onClick false

            val clickedOnField = mouseX in x..(x + width) && mouseY in y..(y + height)

            if (clickedOnField) {
                isFocused = true
                isDragging = true

                val clickRelX = mouseX - (x - scrollOffset)
                val newCursorIndex = getCharIndexAtAbsX(clickRelX)

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 250) clickCount++
                else clickCount = 1

                lastClickTime = currentTime

                when (clickCount) {
                    1 -> {
                        cursorIndex = newCursorIndex
                        if (!KnitKeyboard.isShiftKeyPressed) {
                            selectionAnchor = cursorIndex
                        }
                    }
                    2 -> selectWordAt(newCursorIndex)
                    else -> {
                        selectAll()
                        clickCount = 0
                    }
                }
                resetCaretBlink()
                return@onClick true
            } else {
                isFocused = false
                isDragging = false
                return@onClick false
            }
            true
        }

        onCharType { keyCode, scanCode, char ->
            keyTyped(keyCode, scanCode, char)
        }
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        background.isHovered = hovered
        background.isPressed = pressed

        val shouldShowPlaceholder = value.isEmpty() && !focused
        val textColor = if (shouldShowPlaceholder) Color(120, 120, 120).rgb else textColor

        innerText.text = if (shouldShowPlaceholder) placeholder else value
        innerText.textColor = textColor

        if(hasSelection && !shouldShowPlaceholder) {
            val selStartStr = value.substring(0, selectionStart)
            val selEndStr = value.substring(0, selectionEnd)
            val x1 = scrollOffset + NVGRenderer.textWidth(selStartStr, fontSize, NVGRenderer.defaultFont)
            val x2 = scrollOffset + NVGRenderer.textWidth(selEndStr, fontSize, NVGRenderer.defaultFont)

            selectionRectangle.setPositioning(x1, Pos.ParentPixels, 0f, Pos.ParentCenter)
            selectionRectangle.setSizing(x2-x1, Size.Pixels, fontSize, Size.Pixels)
            selectionRectangle.visible = true
        } else {
            selectionRectangle.visible = false
        }

        caret.height = fontSize
        caret.width = 1f
        val x = NVGRenderer.textWidth(value.substring(0, cursorIndex.coerceIn(0, value.length)), fontSize, NVGRenderer.defaultFont) - scrollOffset
        caret.setPositioning(x, Pos.ParentPixels, 0f, Pos.ParentCenter)
        caret.visible = focused && caretVisible && !shouldShowPlaceholder

        if (System.currentTimeMillis() - lastBlink > caretBlinkRate) {
            caretVisible = !caretVisible
            lastBlink = System.currentTimeMillis()
        }
    }

    fun keyTyped(keyCode: Int, scanCode: Int, char: Char): Boolean {
        if (!isFocused) return false

        val ctrlDown = KnitKeyboard.isCtrlKeyPressed
        val shiftDown = KnitKeyboard.isShiftKeyPressed

        when (keyCode) {
            KnitKeys.KEY_ESCAPE.code, KnitKeys.KEY_ENTER.code -> {
                isFocused = false
                return true
            }
            KnitKeys.KEY_BACKSPACE.code -> {
                if (ctrlDown) deletePrevWord() else deleteChar(-1)
                return true
            }
            KnitKeys.KEY_DELETE.code -> {
                if (ctrlDown) deleteNextWord() else deleteChar(1)
                return true
            }
            KnitKeys.KEY_LEFT.code -> {
                if (ctrlDown) moveWord(-1, shiftDown) else moveCaret(-1, shiftDown)
                return true
            }
            KnitKeys.KEY_RIGHT.code -> {
                if (ctrlDown) moveWord(1, shiftDown) else moveCaret(1, shiftDown)
                return true
            }
            KnitKeys.KEY_HOME.code -> {
                moveCaretTo(0, shiftDown)
                return true
            }
            KnitKeys.KEY_END.code -> {
                moveCaretTo(value.length, shiftDown)
                return true
            }
        }

        if (ctrlDown) {
            val keyName = KnitInputs.getDisplayName(keyCode, scanCode).substringAfterLast(".").lowercase()
            when (keyName) {
                "a" -> {
                    selectAll()
                    return true
                }
                "c" -> {
                    copySelection()
                    return true
                }
                "v" -> {
                    paste()
                    return true
                }
                "x" -> {
                    cutSelection()
                    return true
                }
            }
            return false
        }

        if (char.code >= 32 && char != 127.toChar()) {
            insertText(char.toString())
            return true
        }

        return false
    }

    private fun resetCaretBlink() {
        lastBlink = System.currentTimeMillis()
        caretVisible = true
    }

    private fun getCharIndexAtAbsX(absClickX: Float): Int {
        if (absClickX <= 0) return 0
        var currentWidth = 0f
        for (i in value.indices) {
            val charWidth = NVGRenderer.textWidth(value[i].toString(), fontSize, NVGRenderer.defaultFont)
            if (absClickX < currentWidth + charWidth / 2) {
                return i
            }
            currentWidth += charWidth
        }
        return value.length
    }

    private fun selectWordAt(pos: Int) {
        if (value.isEmpty()) return
        val currentPos = pos.coerceIn(0, value.length)

        if (currentPos < value.length && !value[currentPos].isWhitespace()) {
            var start = currentPos
            while (start > 0 && !value[start - 1].isWhitespace()) start--
            var end = currentPos
            while (end < value.length && !value[end].isWhitespace()) end++
            cursorIndex = end
            selectionAnchor = start
        } else {
            cursorIndex = currentPos
            selectionAnchor = currentPos
        }
        ensureCaretVisible()
    }

    private fun insertText(text: String) {
        val builder = StringBuilder(value)
        val newCursorPos = if (!hasSelection) cursorIndex
        else {
            val currentSelectionStart = selectionStart
            builder.delete(currentSelectionStart, selectionEnd)
            currentSelectionStart
        }

        builder.insert(newCursorPos, text)
        this.value = builder.toString()
        cursorIndex = (newCursorPos + text.length).coerceIn(0, this.value.length)
        selectionAnchor = cursorIndex

        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun deleteChar(direction: Int) {
        var textChanged = false
        var newText = value
        var newCursor = cursorIndex

        if (hasSelection) {
            val builder = StringBuilder(value)
            val selStart = selectionStart
            builder.delete(selStart, selectionEnd)
            newText = builder.toString()
            newCursor = selStart
            textChanged = true
        } else {
            if (direction == -1 && cursorIndex > 0) {
                val builder = StringBuilder(value)
                builder.deleteCharAt(cursorIndex - 1)
                newText = builder.toString()
                newCursor = cursorIndex - 1
                textChanged = true
            } else if (direction == 1 && cursorIndex < value.length) {
                val builder = StringBuilder(value)
                builder.deleteCharAt(cursorIndex)
                newText = builder.toString()
                textChanged = true
            }
        }

        if (textChanged) {
            this.value = newText
            cursorIndex = newCursor.coerceIn(0, this.value.length)
            selectionAnchor = cursorIndex

            val maxScroll = max(0f, NVGRenderer.textWidth(this.value, fontSize, NVGRenderer.defaultFont).toInt() - (width * 2))
            if (scrollOffset > maxScroll) {
                scrollOffset = maxScroll
            }

            ensureCaretVisible()
        }
        resetCaretBlink()
    }

    private fun moveCaret(amount: Int, shiftHeld: Boolean) {
        cursorIndex = (cursorIndex + amount).coerceIn(0, value.length)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun moveCaretTo(position: Int, shiftHeld: Boolean) {
        cursorIndex = position.coerceIn(0, value.length)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun moveWord(direction: Int, shiftHeld: Boolean) {
        cursorIndex = findWordBoundary(cursorIndex, direction)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    private fun findWordBoundary(startIndex: Int, direction: Int): Int {
        var i = startIndex
        val len = value.length
        if (direction < 0) {
            if (i > 0) i--
            while (i > 0 && value[i].isWhitespace()) i--
            while (i > 0 && !value[i - 1].isWhitespace()) i--
        } else {
            while (i < len && !value[i].isWhitespace()) i++
            while (i < len && value[i].isWhitespace()) i++
        }
        return i.coerceIn(0, len)
    }

    private fun deletePrevWord() {
        if (hasSelection) {
            deleteChar(0)
            return
        }
        if (cursorIndex == 0) return
        val oldCursor = cursorIndex
        cursorIndex = findWordBoundary(cursorIndex, -1)
        selectionAnchor = oldCursor
        deleteChar(0)
    }

    private fun deleteNextWord() {
        if (hasSelection) {
            deleteChar(0)
            return
        }
        if (cursorIndex == value.length) return
        val oldCursor = cursorIndex
        cursorIndex = findWordBoundary(cursorIndex, 1)
        selectionAnchor = oldCursor
        deleteChar(0)
    }

    private fun selectAll() {
        selectionAnchor = 0
        cursorIndex = value.length
        resetCaretBlink()
    }

    private fun getSelectedText(): String {
        return if (hasSelection) value.substring(selectionStart, selectionEnd) else ""
    }

    private fun copySelection() {
        if (!hasSelection) return
        mc.keyboard.clipboard = getSelectedText()
    }

    private fun cutSelection() {
        if (!hasSelection) return
        copySelection()
        deleteChar(0)
    }

    private fun paste() {
        val clipboardText = mc.keyboard.clipboard
        if (clipboardText.isNotEmpty()) {
            insertText(clipboardText)
        }
    }

    private fun ensureCaretVisible() {
        val caretXAbsolute = NVGRenderer.textWidth(value.substring(0, cursorIndex.coerceIn(0, value.length)), fontSize, NVGRenderer.defaultFont).toInt()
        val visibleTextStart = scrollOffset
        val visibleTextEnd = scrollOffset + (width * 2)

        if (caretXAbsolute < visibleTextStart) {
            scrollOffset = caretXAbsolute.toFloat()
        } else if (caretXAbsolute > visibleTextEnd - 1) {
            scrollOffset = caretXAbsolute - (width * 2) + 1
        }

        val maxScrollPossible = max(0f, NVGRenderer.textWidth(value, fontSize, NVGRenderer.defaultFont).toInt() - (width * 2))
        scrollOffset = scrollOffset.coerceIn(0f, maxScrollPossible)
        if (NVGRenderer.textWidth(value, fontSize, NVGRenderer.defaultFont).toInt() <= width * 2) {
            scrollOffset = 0f
        }
    }

    override fun getAutoWidth(): Float = background.getAutoWidth()
    override fun getAutoHeight(): Float = background.getAutoHeight()

    fun padding(top: Float, right: Float, bottom: Float, left: Float): TextInput = apply {
        background.padding(top, right, bottom, left)
    }

    fun padding(all: Float): TextInput = apply {
        background.padding(all)
    }

    fun fontSize(size: Float): TextInput = apply {
        this.fontSize = size
        innerText.fontSize = size
    }

    fun backgroundColor(color: Int): TextInput = apply {
        background.backgroundColor(color)
    }

    fun borderColor(color: Int): TextInput = apply {
        background.borderColor(color)
    }

    fun borderRadius(radius: Float): TextInput = apply {
        background.borderRadius(radius)
    }

    fun borderThickness(thickness: Float): TextInput = apply {
        background.borderThickness(thickness)
    }

    fun hoverColor(color: Int): TextInput = apply {
        background.hoverColor(color)
    }

    fun pressedColor(color: Int): TextInput = apply {
        background.pressedColor(color)
    }
}