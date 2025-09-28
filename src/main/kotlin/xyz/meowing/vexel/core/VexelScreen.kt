package xyz.meowing.vexel.core

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse
import xyz.meowing.vexel.utils.MouseUtils

abstract class VexelScreen : GuiScreen() {
    private var lastX: Int = -1
    private var lastY: Int = -1

    var initialized = false
        private set
    var hasInitialized = false
        private set

    val window = VexelWindow()
    final override fun initGui() {
        super.initGui()

        if (!hasInitialized) {
            hasInitialized = true
            initialized = true

            afterInitialization()
        } else {
            initialized = true
        }
    }

    open fun afterInitialization() {}

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        window.draw()

        if (MouseUtils.rawX != lastX || MouseUtils.rawY != lastY) {
            window.mouseMove()
            lastX = MouseUtils.rawX
            lastY = MouseUtils.rawY
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        window.mouseClick(mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        window.mouseRelease(Mouse.getEventButton())
    }

    override fun handleMouseInput() {
        val dWheel = Mouse.getEventDWheel()

        if (dWheel != 0) {
            val verticalScroll = if (dWheel > 0) 1.0 else -1.0
            window.mouseScroll(0.0, verticalScroll)
        }

        super.handleMouseInput()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        // scanCode isn't used in 1.8.9 anyway
        val handled = window.charType(keyCode, keyCode, typedChar)
        if (!handled) {
            super.keyTyped(typedChar, keyCode)
        }
    }

    override fun onGuiClosed() {
        window.cleanup()
        hasInitialized = false
        super.onGuiClosed()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun onResize(mcIn: Minecraft?, w: Int, h: Int) {
        super.onResize(mcIn, w, h)
        window.onWindowResize()
    }
}