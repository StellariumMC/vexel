package xyz.meowing.vexel.core

import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.events.EventCall
import xyz.meowing.knit.api.input.KnitKeys
import xyz.meowing.knit.api.screen.KnitScreen
import xyz.meowing.vexel.Vexel.eventBus
import xyz.meowing.vexel.events.GuiEvent
import java.util.Timer
import kotlin.concurrent.schedule

abstract class VexelScreen(screenName: String = "Vexel-Screen") : KnitScreen(screenName) {
    var renderEvent: EventCall? = null

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

            afterInitialization()

            renderEvent = eventBus.register<GuiEvent.Render> {
                if (KnitClient.client.currentScreen == this) {
                    window.draw()
                    onRenderGui()
                }
            }

        } else {
            initialized = true
        }
    }

    override fun onCloseGui() {
        window.cleanup()
        renderEvent?.unregister()
        renderEvent = null
        hasInitialized = false
    }

    override fun onResizeGui() {
        window.onWindowResize()
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, button: Int): Boolean {
        return window.mouseClick(button)
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, button: Int): Boolean {
        return window.mouseRelease(button)
    }

    override fun onMouseMove(mouseX: Int, mouseY: Int) {
        window.mouseMove()
    }

    override fun onMouseScroll(horizontal: Double, vertical: Double) {
        window.mouseScroll(horizontal, vertical)
    }

    override fun onKeyType(typedChar: Char, keyCode: Int, scanCode: Int): Boolean {
        val handled = window.charType(keyCode, scanCode, typedChar)
        if (!handled && keyCode == KnitKeys.KEY_ESCAPE.code) close()
        return handled
    }

    /**
     * Called after the elements and animations render.
     */
    open fun onRenderGui() {}

    fun display() {
        Timer().schedule(50) {
            KnitClient.client.execute {
                KnitClient.client.setScreen(this@VexelScreen)
            }
        }
    }
}