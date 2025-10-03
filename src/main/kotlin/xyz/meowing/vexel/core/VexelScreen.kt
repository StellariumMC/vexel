package xyz.meowing.vexel.core

import dev.deftu.omnicore.api.client.input.KeyboardModifiers
import dev.deftu.omnicore.api.client.input.OmniKey
import dev.deftu.omnicore.api.client.input.OmniMouse
import dev.deftu.omnicore.api.client.input.OmniMouseButton
import dev.deftu.omnicore.api.client.render.OmniRenderingContext
import dev.deftu.omnicore.api.client.screen.KeyPressEvent
import dev.deftu.omnicore.api.client.screen.OmniScreen

abstract class VexelScreen : OmniScreen() {
    private var lastX: Double = -1.0
    private var lastY: Double = -1.0

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

        if (OmniMouse.rawX != lastX || OmniMouse.rawY != lastY) {
            window.mouseMove()
            lastX = OmniMouse.rawX
            lastY = OmniMouse.rawY
        }
    }

    override fun onMouseClick(button: OmniMouseButton, x: Double, y: Double, modifiers: KeyboardModifiers): Boolean {
        window.mouseClick(button.code)
        return super.onMouseClick(button, x, y, modifiers)
    }

    override fun onMouseRelease(button: OmniMouseButton, x: Double, y: Double, modifiers: KeyboardModifiers): Boolean {
        window.mouseRelease(button.code)
        return super.onMouseRelease(button, x, y, modifiers)
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