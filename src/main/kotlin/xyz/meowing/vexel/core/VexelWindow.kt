package xyz.meowing.vexel.core

import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.knit.api.render.KnitResolution
import xyz.meowing.vexel.animations.AnimationManager
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer

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
        children.forEach { it.render(KnitMouse.Raw.x.toFloat(), KnitMouse.Raw.y.toFloat()) }
        AnimationManager.update()
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

    fun charType(keyCode: Int, scanCode: Int, charTyped: Char): Boolean {
        return children.reversed().any { it.handleCharType(keyCode, scanCode, charTyped) }
    }

    fun onWindowResize() {
        children.forEach { it.onWindowResize() }
    }

    fun cleanup() {
        children.toList().forEach { it.destroy() }
        children.clear()
        AnimationManager.clear()
        NVGRenderer.cleanCache()
    }
}