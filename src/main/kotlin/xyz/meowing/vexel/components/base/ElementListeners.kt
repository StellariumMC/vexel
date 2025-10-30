package xyz.meowing.vexel.components.base

class ElementListeners {
    val mouseEnter = mutableListOf<(Float, Float) -> Unit>()
    val mouseExit = mutableListOf<(Float, Float) -> Unit>()
    val mouseMove = mutableListOf<(Float, Float) -> Unit>()
    val mouseScroll = mutableListOf<(Float, Float, Double, Double) -> Boolean>()
    val mouseClick = mutableListOf<(Float, Float, Int) -> Boolean>()
    val mouseRelease = mutableListOf<(Float, Float, Int) -> Boolean>()
    val charType = mutableListOf<(Int, Int, Char) -> Boolean>()

    fun clear() {
        mouseEnter.clear()
        mouseExit.clear()
        mouseMove.clear()
        mouseScroll.clear()
        mouseClick.clear()
        mouseRelease.clear()
        charType.clear()
    }
}