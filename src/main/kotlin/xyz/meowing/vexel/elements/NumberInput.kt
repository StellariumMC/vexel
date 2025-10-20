package xyz.meowing.vexel.elements

import xyz.meowing.knit.api.input.KnitInputs
import xyz.meowing.knit.api.input.KnitKeyboard
import xyz.meowing.knit.api.input.KnitKeys
import xyz.meowing.knit.api.KnitClient.client
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class NumberInput(
    initialValue: Int = 0,
    var placeholder: String = "",
    var fontSize: Float = 12f,
    selectionColor: Int = 0x80aac7ff.toInt(),
    var textColor: Int = 0xFFFFFFFF.toInt(),
    var allowDecimals: Boolean = true,
    var allowNegative: Boolean = false,
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(6f, 6f, 6f, 6f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80505050.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : VexelElement<NumberInput>(widthType, heightType) {
    var stringValue = initialValue.toString()
        set(newVal) {
            if (field == newVal) return
            field = newVal
            cursorIndex = cursorIndex.coerceIn(0, field.length)
            selectionAnchor = selectionAnchor.coerceIn(0, field.length)
            onValueChange?.invoke(field)
            numberValue = field.toFloatOrNull() ?: 0f
        }

    var numberValue = 0f

    var isDragging = false
    var lastBlink = System.currentTimeMillis()
        private set
    var caretBlinkRate = 500L

    var cursorIndex = stringValue.length
    var selectionAnchor = stringValue.length

    private val selectionStart: Int get() = min(cursorIndex, selectionAnchor)
    private val selectionEnd: Int get() = max(cursorIndex, selectionAnchor)
    private val hasSelection: Boolean get() = selectionStart != selectionEnd

    var scrollOffset = 0f
    private var lastClickTime = 0L
    private var clickCount = 0
    private var caretVisible = true

    val background = Rectangle(
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
        .childOf(this)

    val innerText = Text(placeholder, textColor, fontSize)
        .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
        .childOf(background)

    val selectionRectangle = Rectangle(selectionColor, 0x00000000, 0f, 0f)
        .setPositioning(Pos.ParentPixels, Pos.ParentCenter)
        .setSizing(Size.Pixels, Size.Pixels)
        .ignoreMouseEvents()
        .ignoreFocus()
        .childOf(background)

    val caret = Rectangle(0xFFFFFFFF.toInt(), 0xFF000000.toInt(), 1f, 0f)
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

        val shouldShowPlaceholder = stringValue.isEmpty() && !focused
        val textColor = if (shouldShowPlaceholder) Color(120, 120, 120).rgb else textColor

        innerText.text = if (shouldShowPlaceholder) placeholder else stringValue
        innerText.textColor = textColor

        if(hasSelection && !shouldShowPlaceholder) {
            val selStartStr = stringValue.substring(0, selectionStart)
            val selEndStr = stringValue.substring(0, selectionEnd)
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
        val x = NVGRenderer.textWidth(stringValue.substring(0, cursorIndex.coerceIn(0, stringValue.length)), fontSize, NVGRenderer.defaultFont) - scrollOffset
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
                moveCaretTo(stringValue.length, shiftDown)
                return true
            }
        }

        if (ctrlDown) {
            val keyName = KnitInputs.getDisplayName(keyCode, scanCode).lowercase()
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

        if (char.code < 32 || char == 127.toChar()) return false
        if (!char.isDigit() && char != '.' && char != '-') return false
        if (char == '.' && stringValue.contains('.')) return false
        if (char == '-' && (cursorIndex != 0 || stringValue.contains('-'))) return false
        if (char == '.' && !allowDecimals) return false
        if (char == '-' && !allowNegative) return false

        insertText(char.toString())
        return true
    }

    fun resetCaretBlink() {
        lastBlink = System.currentTimeMillis()
        caretVisible = true
    }

    fun getCharIndexAtAbsX(absClickX: Float): Int {
        if (absClickX <= 0) return 0
        var currentWidth = 0f
        for (i in stringValue.indices) {
            val charWidth = NVGRenderer.textWidth(stringValue[i].toString(), fontSize, NVGRenderer.defaultFont)
            if (absClickX < currentWidth + charWidth / 2) {
                return i
            }
            currentWidth += charWidth
        }
        return stringValue.length
    }

    fun selectWordAt(pos: Int) {
        if (stringValue.isEmpty()) return
        val currentPos = pos.coerceIn(0, stringValue.length)

        if (currentPos < stringValue.length && !stringValue[currentPos].isWhitespace()) {
            var start = currentPos
            while (start > 0 && !stringValue[start - 1].isWhitespace()) start--
            var end = currentPos
            while (end < stringValue.length && !stringValue[end].isWhitespace()) end++
            cursorIndex = end
            selectionAnchor = start
        } else {
            cursorIndex = currentPos
            selectionAnchor = currentPos
        }
        ensureCaretVisible()
    }

    fun insertText(text: String) {
        val builder = StringBuilder(stringValue)
        val newCursorPos = if (!hasSelection) cursorIndex
        else {
            val currentSelectionStart = selectionStart
            builder.delete(currentSelectionStart, selectionEnd)
            currentSelectionStart
        }

        builder.insert(newCursorPos, text)
        this.stringValue = builder.toString()
        cursorIndex = (newCursorPos + text.length).coerceIn(0, this.stringValue.length)
        selectionAnchor = cursorIndex

        ensureCaretVisible()
        resetCaretBlink()
    }

    fun deleteChar(direction: Int) {
        var textChanged = false
        var newText = stringValue
        var newCursor = cursorIndex

        if (hasSelection) {
            val builder = StringBuilder(stringValue)
            val selStart = selectionStart
            builder.delete(selStart, selectionEnd)
            newText = builder.toString()
            newCursor = selStart
            textChanged = true
        } else {
            if (direction == -1 && cursorIndex > 0) {
                val builder = StringBuilder(stringValue)
                builder.deleteCharAt(cursorIndex - 1)
                newText = builder.toString()
                newCursor = cursorIndex - 1
                textChanged = true
            } else if (direction == 1 && cursorIndex < stringValue.length) {
                val builder = StringBuilder(stringValue)
                builder.deleteCharAt(cursorIndex)
                newText = builder.toString()
                textChanged = true
            }
        }

        if (textChanged) {
            this.stringValue = newText
            cursorIndex = newCursor.coerceIn(0, this.stringValue.length)
            selectionAnchor = cursorIndex

            val maxScroll = max(0f, NVGRenderer.textWidth(this.stringValue, fontSize, NVGRenderer.defaultFont).toInt() - (width * 2))
            if (scrollOffset > maxScroll) {
                scrollOffset = maxScroll
            }

            ensureCaretVisible()
        }
        resetCaretBlink()
    }

    fun moveCaret(amount: Int, shiftHeld: Boolean) {
        cursorIndex = (cursorIndex + amount).coerceIn(0, stringValue.length)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    fun moveCaretTo(position: Int, shiftHeld: Boolean) {
        cursorIndex = position.coerceIn(0, stringValue.length)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    fun moveWord(direction: Int, shiftHeld: Boolean) {
        cursorIndex = findWordBoundary(cursorIndex, direction)
        if (!shiftHeld) {
            selectionAnchor = cursorIndex
        }
        ensureCaretVisible()
        resetCaretBlink()
    }

    fun findWordBoundary(startIndex: Int, direction: Int): Int {
        var i = startIndex
        val len = stringValue.length
        if (direction < 0) {
            if (i > 0) i--
            while (i > 0 && stringValue[i].isWhitespace()) i--
            while (i > 0 && !stringValue[i - 1].isWhitespace()) i--
        } else {
            while (i < len && !stringValue[i].isWhitespace()) i++
            while (i < len && stringValue[i].isWhitespace()) i++
        }
        return i.coerceIn(0, len)
    }

    fun deletePrevWord() {
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

    fun deleteNextWord() {
        if (hasSelection) {
            deleteChar(0)
            return
        }
        if (cursorIndex == stringValue.length) return
        val oldCursor = cursorIndex
        cursorIndex = findWordBoundary(cursorIndex, 1)
        selectionAnchor = oldCursor
        deleteChar(0)
    }

    fun selectAll() {
        selectionAnchor = 0
        cursorIndex = stringValue.length
        resetCaretBlink()
    }

    fun getSelectedText(): String {
        return if (hasSelection) stringValue.substring(selectionStart, selectionEnd) else ""
    }

    fun copySelection() {
        if (!hasSelection) return
        client.keyboard.clipboard = getSelectedText()
    }

    fun cutSelection() {
        if (!hasSelection) return
        copySelection()
        deleteChar(0)
    }

    fun paste() {
        val clipboardText = client.keyboard.clipboard
        if (clipboardText.isNotEmpty()) {
            insertText(clipboardText)
        }
    }

    private fun ensureCaretVisible() {
        val caretXAbsolute = NVGRenderer.textWidth(stringValue.substring(0, cursorIndex.coerceIn(0, stringValue.length)), fontSize, NVGRenderer.defaultFont).toInt()
        val visibleTextStart = scrollOffset
        val visibleTextEnd = scrollOffset + (width * 2)

        if (caretXAbsolute < visibleTextStart) {
            scrollOffset = caretXAbsolute.toFloat()
        } else if (caretXAbsolute > visibleTextEnd - 1) {
            scrollOffset = caretXAbsolute - (width * 2) + 1
        }

        val maxScrollPossible = max(0f, NVGRenderer.textWidth(stringValue, fontSize, NVGRenderer.defaultFont).toInt() - (width * 2))
        scrollOffset = scrollOffset.coerceIn(0f, maxScrollPossible)
        if (NVGRenderer.textWidth(stringValue, fontSize, NVGRenderer.defaultFont).toInt() <= width * 2) {
            scrollOffset = 0f
        }
    }

    override fun getAutoWidth(): Float = background.getAutoWidth()
    override fun getAutoHeight(): Float = background.getAutoHeight()

    fun padding(top: Float, right: Float, bottom: Float, left: Float): NumberInput = apply {
        background.padding(top, right, bottom, left)
    }

    fun padding(all: Float): NumberInput = apply {
        background.padding(all)
    }

    fun fontSize(size: Float): NumberInput = apply {
        this.fontSize = size
        innerText.fontSize = size
    }

    fun backgroundColor(color: Int): NumberInput = apply {
        background.backgroundColor(color)
    }

    fun borderColor(color: Int): NumberInput = apply {
        background.borderColor(color)
    }

    fun borderRadius(radius: Float): NumberInput = apply {
        background.borderRadius(radius)
    }

    fun borderThickness(thickness: Float): NumberInput = apply {
        background.borderThickness(thickness)
    }

    fun hoverColor(color: Int): NumberInput = apply {
        background.hoverColor(color)
    }

    fun pressedColor(color: Int): NumberInput = apply {
        background.pressedColor(color)
    }
}