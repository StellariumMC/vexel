package xyz.meowing.vexel.core

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import xyz.meowing.vexel.events.EventBus
import xyz.meowing.vexel.events.GuiEvent

abstract class VexelScreen : Screen(Text.literal("Vexel Screen")) {
    var initialized = false
        private set
    var hasInitialized = false
        private set

    val window = VexelWindow()
    val eventCalls = mutableListOf<EventBus.EventCall>()

    final override fun init() {
        super.init()

        if (!hasInitialized) {
            eventCalls.add(EventBus.register<GuiEvent.Key>(0, { event ->
                window.charType(event.key, event.scanCode, event.character)
            }))

            hasInitialized = true
            initialized = true

            window.cleanup()

            afterInitialization()
        } else {
            initialized = true
        }
    }

    open fun afterInitialization() {}

    override fun render(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        window.draw()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        window.mouseClick(button)
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        window.mouseRelease(button)
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        window.mouseMove()
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        window.mouseScroll(horizontalAmount, verticalAmount)
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun close() {
        window.cleanup()
        eventCalls.clear()
        hasInitialized = false
        super.close()
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        super.resize(client, width, height)
        window.onWindowResize()
    }
}