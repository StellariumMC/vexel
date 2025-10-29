package xyz.meowing.vexel.core

import xyz.meowing.knit.api.KnitClient
import xyz.meowing.knit.api.events.EventCall
import xyz.meowing.knit.api.screen.KnitScreen
import xyz.meowing.vexel.Vexel.eventBus
import xyz.meowing.vexel.events.GuiEvent
import xyz.meowing.vexel.utils.render.NVGRenderer
import java.util.Timer
import kotlin.concurrent.schedule

abstract class VexelScreen(screenName: String = "Vexel-Screen") : KnitScreen(screenName) {
    val events = mutableListOf<EventCall>()

    var initialized = false
        private set
    var hasInitialized = false
        private set

    val window = VexelWindow()

    open fun afterInitialization() {}

    init {
        events.add(eventBus.register<GuiEvent.Render> {
            if (KnitClient.client.currentScreen == this) window.draw()
        })
    }

    final override fun onInitGui() {
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
        events.toList().forEach { it.unregister() }
        events.clear()
        hasInitialized = false
    }

    override fun onResizeGui() {
        window.onWindowResize()
    }

    override fun onMouseClick(mouseX: Int, mouseY: Int, button: Int) {
        window.mouseClick(button)
    }

    override fun onMouseRelease(mouseX: Int, mouseY: Int, button: Int) {
        window.mouseRelease(button)
    }

    override fun onMouseMove(mouseX: Int, mouseY: Int) {
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
            KnitClient.client.execute {
                KnitClient.client.setScreen(this@VexelScreen)
            }
        }
    }
}