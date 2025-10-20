package xyz.meowing.vexel.elements

import org.lwjgl.input.Keyboard
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Text
import xyz.meowing.vexel.components.base.Pos
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement

class Keybind(
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(12f, 24f, 12f, 24f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : VexelElement<Keybind>(widthType, heightType) {
    var selectedKeyId: Int? = null
    var selectedScanId: Int? = null
    var listen: Boolean = false

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

    val innerText = Text("Key A", 0xFFFFFFFF.toInt(), 12f)
        .setPositioning(Pos.ParentCenter, Pos.ParentCenter)
        .childOf(background)

    init {
        setSizing(100f, Size.Pixels, 0f, Size.Auto)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        ignoreFocus()

        onClick { _, _, _ ->
            listenForKeybind()
            true
        }

        onCharType { keyCode, scanCode, char ->
            if (!listen) return@onCharType false
            if (keyCode == Keyboard.KEY_ESCAPE) {
                innerText.text = "None"
                selectedKeyId = null
                selectedScanId = null
            } else {
                innerText.text = getKeyName(keyCode, scanCode)
                selectedKeyId = keyCode
                selectedScanId = scanCode
            }

            onValueChange?.invoke(keyCode)
            listen = false
            true
        }
    }

    fun listenForKeybind() {
        innerText.text = "Press a key.."
        listen = true
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        background.isHovered = hovered
        background.isPressed = pressed
    }

    private fun getKeyName(keyCode: Int, scanCode: Int): String = when (keyCode) {
        Keyboard.KEY_LSHIFT -> "LShift"
        Keyboard.KEY_RSHIFT -> "RShift"
        Keyboard.KEY_LCONTROL -> "LCtrl"
        Keyboard.KEY_RCONTROL -> "RCtrl"
        Keyboard.KEY_LMENU -> "LAlt"
        Keyboard.KEY_RMENU -> "RAlt"
        Keyboard.KEY_RETURN -> "Enter"
        Keyboard.KEY_ESCAPE -> "None"
        Keyboard.KEY_SPACE -> "Space"
        Keyboard.KEY_F1 -> "F1"
        Keyboard.KEY_F2 -> "F2"
        Keyboard.KEY_F3 -> "F3"
        Keyboard.KEY_F4 -> "F4"
        Keyboard.KEY_F5 -> "F5"
        Keyboard.KEY_F6 -> "F6"
        Keyboard.KEY_F7 -> "F7"
        Keyboard.KEY_F8 -> "F8"
        Keyboard.KEY_F9 -> "F9"
        Keyboard.KEY_F10 -> "F10"
        Keyboard.KEY_F11 -> "F11"
        Keyboard.KEY_F12 -> "F12"
        Keyboard.KEY_TAB -> "Tab"
        Keyboard.KEY_BACK -> "Backspace"
        Keyboard.KEY_DELETE -> "Delete"
        Keyboard.KEY_HOME -> "Home"
        Keyboard.KEY_END -> "End"
        Keyboard.KEY_PRIOR -> "Page Up"
        Keyboard.KEY_NEXT -> "Page Down"
        Keyboard.KEY_UP -> "Up"
        Keyboard.KEY_DOWN -> "Down"
        Keyboard.KEY_LEFT -> "Left"
        Keyboard.KEY_RIGHT -> "Right"
        Keyboard.KEY_INSERT -> "Insert"
        Keyboard.KEY_NUMLOCK -> "Num Lock"
        Keyboard.KEY_SCROLL -> "Scroll Lock"
        Keyboard.KEY_PAUSE -> "Pause"
        Keyboard.KEY_CAPITAL -> "Caps Lock"
        else -> {
            val keyName = Keyboard.getKeyName(keyCode)
            keyName?.uppercase() ?: "Key $keyCode"
        }
    }

    override fun getAutoWidth(): Float = background.getAutoWidth()
    override fun getAutoHeight(): Float = background.getAutoHeight()

    fun padding(top: Float, right: Float, bottom: Float, left: Float): Keybind = apply {
        background.padding(top, right, bottom, left)
    }

    fun padding(all: Float): Keybind = apply {
        background.padding(all)
    }

    fun backgroundColor(color: Int): Keybind = apply {
        background.backgroundColor(color)
    }

    fun borderColor(color: Int): Keybind = apply {
        background.borderColor(color)
    }

    fun borderRadius(radius: Float): Keybind = apply {
        background.borderRadius(radius)
    }

    fun borderThickness(thickness: Float): Keybind = apply {
        background.borderThickness(thickness)
    }

    fun hoverColor(color: Int): Keybind = apply {
        background.hoverColor(color)
    }

    fun pressedColor(color: Int): Keybind = apply {
        background.pressedColor(color)
    }
}