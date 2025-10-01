package xyz.meowing.vexel.core

import dev.deftu.omnicore.api.client.input.OmniMouse
import net.minecraft.client.util.Window
import xyz.meowing.vexel.Vexel
import xyz.meowing.vexel.animations.AnimationManager
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer

class VexelWindow {
    val children: MutableList<VexelElement<*>> = mutableListOf()
    val window: Window get() = Vexel.mc.window

    fun addChild(element: VexelElement<*>) {
        element.parent = this
        children.add(element)
    }

    fun removeChild(element: VexelElement<*>) {
        element.parent = null
        children.remove(element)
    }

    fun draw() {
        NVGRenderer.beginFrame(window.width.toFloat(), window.height.toFloat())
        NVGRenderer.push()
        children.forEach { it.render(0f, 0f) }
        AnimationManager.update()
        NVGRenderer.pop()
        NVGRenderer.endFrame()
    }

    fun mouseClick(button: Int) {
        children.reversed().any { it.handleMouseClick(OmniMouse.rawX.toFloat(), OmniMouse.rawY.toFloat(), button) }
    }

    fun mouseRelease(button: Int) {
        children.reversed().forEach { it.handleMouseRelease(OmniMouse.rawX.toFloat(), OmniMouse.rawY.toFloat(), button) }
    }

    fun mouseMove() {
        children.reversed().any { it.handleMouseMove(OmniMouse.rawX.toFloat(), OmniMouse.rawY.toFloat()) }
    }

    fun mouseScroll(horizontalDelta: Double, verticalDelta: Double) {
        children.reversed().any { it.handleMouseScroll(OmniMouse.rawX.toFloat(), OmniMouse.rawY.toFloat(), horizontalDelta, verticalDelta) }
    }

    fun charType(keyCode: Int, scanCode: Int , charTyped: Char) {
        children.reversed().any { it.handleCharType(keyCode, scanCode, charTyped) }
    }

    fun onWindowResize() {
        children.forEach { it.onWindowResize() }
    }

    fun cleanup() {
        children.forEach { it.destroy() }
        children.clear()
        AnimationManager.clear()
        NVGRenderer.cleanCache()
    }
}