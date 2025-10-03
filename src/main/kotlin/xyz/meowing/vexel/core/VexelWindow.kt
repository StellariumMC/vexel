package xyz.meowing.vexel.core

import xyz.meowing.vexel.Vexel.client
import xyz.meowing.vexel.Vexel.renderEngine
import xyz.meowing.vexel.animations.AnimationManager
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.MouseUtils

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
        renderEngine.beginFrame(client.displayWidth.toFloat(), client.displayHeight.toFloat())
        renderEngine.push()
        children.forEach { it.render(0f, 0f) }
        AnimationManager.update()
        renderEngine.pop()
        renderEngine.endFrame()
    }

    fun mouseClick(button: Int) {
        children.reversed().any { it.handleMouseClick(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat(), button) }
    }

    fun mouseRelease(button: Int) {
        children.reversed().forEach { it.handleMouseRelease(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat(), button) }
    }

    fun mouseMove() {
        children.reversed().any { it.handleMouseMove(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat()) }
    }

    fun mouseScroll(horizontalDelta: Double, verticalDelta: Double) {
        children.reversed().any { it.handleMouseScroll(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat(), horizontalDelta, verticalDelta) }
    }

    fun charType(keyCode: Int, scanCode: Int , charTyped: Char): Boolean {
        return children.reversed().any { it.handleCharType(keyCode, scanCode, charTyped) }
    }

    fun onWindowResize() {
        children.forEach { it.onWindowResize() }
    }

    fun cleanup() {
        children.forEach { it.destroy() }
        children.clear()
        AnimationManager.clear()
        renderEngine.cleanCache()
    }
}