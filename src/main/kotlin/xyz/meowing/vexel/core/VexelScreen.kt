package xyz.meowing.vexel.core

import xyz.meowing.knit.api.screen.KnitScreen
import xyz.meowing.vexel.Vexel.renderEngine

abstract class VexelScreen : KnitScreen() {
    var initialized = false
        private set
    var hasInitialized = false
        private set

    val window = VexelWindow()

    open fun afterInitialization() {}

    final override fun onInitGui() {
        if (!hasInitialized) {
            hasInitialized = true
            initialized = true

            renderEngine.cleanCache()

            afterInitialization()
        } else {
            initialized = true
        }
    }

    override fun onCloseGui() {
        window.cleanup()
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
        window.charType(keyCode, scanCode, typedChar)
    }
}