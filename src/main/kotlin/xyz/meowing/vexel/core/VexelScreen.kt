package xyz.meowing.vexel.core

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import xyz.meowing.vexel.Vexel
import xyz.meowing.vexel.core.VexelWindow

abstract class VexelScreen : Screen(Text.literal("Canvas Screen")) {
    val window = VexelWindow()

    final override fun init() {
        afterInitialization()

        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, scancode, modifiers ->
                val charTyped = GLFW.glfwGetKeyName(key, scancode)?.firstOrNull() ?: '\u0000'

                window.charType(key, scancode, charTyped)
                true
            }

            GLFW.glfwSetCharCallback(Vexel.mc.window.handle) { _, codepoint ->
                val charTyped = codepoint.toChar()

                window.charType(GLFW.GLFW_KEY_UNKNOWN, 0, charTyped)
            }
        }

        super.init()
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
        super.close()
    }

    override fun resize(client: MinecraftClient, width: Int, height: Int) {
        super.resize(client, width, height)
        window.onWindowResize()
    }
}