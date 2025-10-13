package xyz.meowing.vexel.core

import org.lwjgl.input.Keyboard
import xyz.meowing.knit.api.screen.KnitScreen
import xyz.meowing.vexel.Vexel
import xyz.meowing.vexel.utils.render.NVGRenderer
import java.util.Timer
import kotlin.concurrent.schedule

abstract class VexelScreen(screenName: String = "Vexel-Screen") : KnitScreen(screenName) {
    var initialized = false
        private set
    var hasInitialized = false
        private set

    val window = VexelWindow()

    open fun afterInitialization() {}

    final override fun onInitGui() {
        Keyboard.enableRepeatEvents(true)

        if (!hasInitialized) {
            hasInitialized = true
            initialized = true

            NVGRenderer.cleanCache()

            afterInitialization()
        } else {
            initialized = true
        }
    }

    override fun onCloseGui() {
        window.cleanup()
        Keyboard.enableRepeatEvents(false)
        hasInitialized = false
    }

    override fun onResizeGui() {
        window.onWindowResize()
    }

    override fun onRender() {
        window.draw()
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, button: Int) {
        window.mouseClick(button)
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, button: Int) {
        window.mouseRelease(button)
    }

    override fun onMouseMove() {
        window.mouseMove()
    }

    override fun onMouseScroll(horizontal: Double, vertical: Double) {
        window.mouseScroll(horizontal, vertical)
    }

    override fun onKeyType(typedChar: Char, keyCode: Int, scanCode: Int) {
        window.charType(keyCode, scanCode,typedChar)
    }

    fun display() {
        Timer().schedule(50) {
            Vexel.client.addScheduledTask {
                Vexel.client.displayGuiScreen(this@VexelScreen)
            }
        }
    }
}