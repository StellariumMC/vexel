package xyz.meowing.vexel.core

import dev.deftu.omnicore.api.client.input.KeyboardModifiers
import dev.deftu.omnicore.api.client.input.OmniKey
import dev.deftu.omnicore.api.client.input.OmniMouseButton
import dev.deftu.omnicore.api.client.render.OmniRenderingContext
import dev.deftu.omnicore.api.client.screen.KeyPressEvent
import dev.deftu.omnicore.api.client.screen.OmniScreen

abstract class VexelScreen : OmniScreen() {
    var initialized = false
        private set
    var hasInitialized = false
        private set

    val window = VexelWindow()

    final override fun onInitialize(width: Int, height: Int) {
        super.onInitialize(width, height)

        if (!hasInitialized) {
            hasInitialized = true
            initialized = true

            window.cleanup()

            afterInitialization()
        } else {
            initialized = true
        }
    }

    open fun afterInitialization() {}

    override fun onRender(ctx: OmniRenderingContext, mouseX: Int, mouseY: Int, tickDelta: Float) {
        window.draw()
    }

    override fun onMouseClick(button: OmniMouseButton, x: Double, y: Double, modifiers: KeyboardModifiers): Boolean {
        window.mouseClick(button.code)
        return super.onMouseClick(button, x, y, modifiers)
    }

    override fun onMouseRelease(button: OmniMouseButton, x: Double, y: Double, modifiers: KeyboardModifiers): Boolean {
        window.mouseRelease(button.code)
        return super.onMouseRelease(button, x, y, modifiers)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        window.mouseMove()
        super.mouseMoved(mouseX, mouseY)
    }

    override fun onKeyPress(key: OmniKey, scanCode: Int, typedChar: Char, modifiers: KeyboardModifiers, event: KeyPressEvent): Boolean {
        window.charType(key.code, scanCode,typedChar)
        return super.onKeyPress(key, scanCode, typedChar, modifiers, event)
    }

    override fun onMouseScroll(x: Double, y: Double, amount: Double, horizontalAmount: Double): Boolean {
        window.mouseScroll(horizontalAmount, amount)
        return super.onMouseScroll(x, y, amount, horizontalAmount)
    }

    override fun onScreenClose() {
        window.cleanup()
        hasInitialized = false
        super.onScreenClose()
    }

    override fun onResize(width: Int, height: Int) {
        super.onResize(width, height)
        window.onWindowResize()
    }
}