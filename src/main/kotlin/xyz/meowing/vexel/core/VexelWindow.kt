package xyz.meowing.vexel.core

import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.knit.api.render.KnitResolution
import xyz.meowing.vexel.Vexel.renderEngine
import xyz.meowing.vexel.animations.AnimationManager
import xyz.meowing.vexel.components.base.VexelElement

class VexelWindow {
    val children: MutableList<VexelElement<*>> = mutableListOf()

    fun addChild(element: VexelElement<*>) {
        element.parent = this
        children.add(element)
    }

    fun removeChild(element: VexelElement<*>) {
        element.parent = null
        children.remove(element)
    }

    fun draw() {
        renderEngine.beginFrame(KnitResolution.Window.width.toFloat(), KnitResolution.Window.height.toFloat())
        renderEngine.push()
        children.forEach { it.render(KnitMouse.Raw.x.toFloat(), KnitMouse.Raw.y.toFloat()) }
        AnimationManager.update()
        renderEngine.pop()
        renderEngine.endFrame()
    }

    fun mouseClick(button: Int) {
        children.reversed().forEach { it.handleMouseClick(KnitMouse.Raw.x.toFloat(), KnitMouse.Raw.y.toFloat(), button) }
    }

    fun mouseRelease(button: Int) {
        children.reversed().forEach { it.handleMouseRelease(KnitMouse.Raw.x.toFloat(), KnitMouse.Raw.y.toFloat(), button) }
    }

    fun mouseMove() {
        children.reversed().forEach { it.handleMouseMove(KnitMouse.Raw.x.toFloat(), KnitMouse.Raw.y.toFloat()) }
    }

    fun mouseScroll(horizontalDelta: Double, verticalDelta: Double) {
        children.reversed().forEach { it.handleMouseScroll(KnitMouse.Raw.x.toFloat(), KnitMouse.Raw.y.toFloat(), horizontalDelta, verticalDelta) }
    }

    fun charType(keyCode: Int, scanCode: Int , charTyped: Char) {
        children.reversed().forEach { it.handleCharType(keyCode, scanCode, charTyped) }
    }

    fun onWindowResize() {
        children.forEach { it.onWindowResize() }
    }

    fun cleanup() {
        children.toList().forEach { it.destroy() }
        children.clear()
        AnimationManager.clear()
        renderEngine.cleanCache()
    }
}