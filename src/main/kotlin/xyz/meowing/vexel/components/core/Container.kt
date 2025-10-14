package xyz.meowing.vexel.components.core

import xyz.meowing.vexel.core.VexelWindow
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer

open class Container(
    var padding: FloatArray = floatArrayOf(0f, 0f, 0f, 0f),
    var scrollable: Boolean = false,
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto
) : VexelElement<Container>(widthType, heightType) {
    var scrollOffset: Float = 0f
    var showScrollbar: Boolean = true
    var scrollbarWidth: Float = 6f
    var scrollbarColor: Int = 0xFF7c7c7d.toInt()
    var scrollbarRadius: Float = 3f
    var scrollbarPadding: Float = 0f
    private var isDraggingScrollbar = false
    private var scrollbarDragOffset = 0f

    override fun onRender(mouseX: Float, mouseY: Float) {}

    private fun drawScrollbar() {
        if (!scrollable || !showScrollbar) return
        val contentHeight = getContentHeight()
        val viewHeight = height - padding[0] - padding[2]
        if (contentHeight <= viewHeight) return

        val scrollbarX = x + width - padding[1] - scrollbarWidth - scrollbarPadding
        val scrollbarHeight = (viewHeight / contentHeight) * viewHeight
        val scrollbarY = y + padding[0] + (scrollOffset / contentHeight) * viewHeight

        NVGRenderer.rect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, scrollbarColor, scrollbarRadius)
    }

    private fun isPointInScrollbar(mouseX: Float, mouseY: Float): Boolean {
        if (!scrollable || !showScrollbar) return false
        val contentHeight = getContentHeight()
        val viewHeight = height - padding[0] - padding[2]
        if (contentHeight <= viewHeight) return false

        val scrollbarX = x + width - padding[1] - scrollbarWidth - scrollbarPadding
        val scrollbarHeight = (viewHeight / contentHeight) * viewHeight
        val scrollbarY = y + padding[0] + (scrollOffset / contentHeight) * viewHeight

        return mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight
    }

    private fun updateHoverStates(mouseX: Float, mouseY: Float) {
        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val wasHovered = isHovered
        isHovered = isPointInside(mouseX, mouseY)

        when {
            isHovered && !wasHovered -> mouseEnterListeners.forEach { it(mouseX, mouseY) }
            !isHovered && wasHovered -> mouseExitListeners.forEach { it(mouseX, mouseY) }
        }

        if (isHovered) mouseMoveListeners.forEach { it(mouseX, mouseY) }

        children.reversed().forEach { child ->
            if (scrollable && !isMouseOnVisible(mouseX, mouseY)) return@forEach
            child.handleMouseMove(mouseX, adjustedMouseY)
        }
    }

    override fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        if (!visible) return false
        if (!isMouseOnVisible(mouseX, mouseY)) return false

        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val childHandled = children.reversed().any { it.handleMouseScroll(mouseX, adjustedMouseY, horizontal, vertical) }

        if (!childHandled && scrollable && isPointInside(mouseX, mouseY)) {
            val contentHeight = getContentHeight()
            val viewHeight = height - padding[0] - padding[2]

            if (contentHeight > viewHeight) {
                val scrollAmount = vertical.toFloat() * -30f
                val maxScroll = contentHeight - viewHeight
                scrollOffset = (scrollOffset + scrollAmount).coerceIn(0f, maxScroll)
                updateHoverStates(mouseX, mouseY)
                return true
            }
        }

        return childHandled
    }

    override fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false

        if (isDraggingScrollbar) {
            val contentHeight = getContentHeight()
            val viewHeight = height - padding[0] - padding[2]
            val relativeY = mouseY - scrollbarDragOffset - (y + padding[0])
            val scrollRatio = (relativeY / viewHeight).coerceIn(0f, 1f)
            val maxScroll = contentHeight - viewHeight
            scrollOffset = (scrollRatio * maxScroll).coerceIn(0f, maxScroll)
            updateHoverStates(mouseX, mouseY)
            return true
        }

        updateHoverStates(mouseX, mouseY)
        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        children.reversed().forEach { child ->
            if (scrollable && !isMouseOnVisible(mouseX, mouseY)) {
                if (child.isHovered) {
                    child.isHovered = false
                    child.mouseExitListeners.forEach { it(mouseX, adjustedMouseY) }
                }
            } else {
                child.handleMouseMove(mouseX, adjustedMouseY)
            }
        }

        return isHovered
    }

    override fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        if (isPointInScrollbar(mouseX, mouseY)) {
            isDraggingScrollbar = true
            val contentHeight = getContentHeight()
            val viewHeight = height - padding[0] - padding[2]
            val scrollbarY = y + padding[0] + (scrollOffset / contentHeight) * viewHeight
            scrollbarDragOffset = mouseY - scrollbarY
            return true
        }

        if (scrollable && !isMouseOnVisible(mouseX, mouseY)) return false

        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val childHandled = children.reversed().any { it.handleMouseClick(mouseX, adjustedMouseY, button) }

        return when {
            childHandled -> true
            isPointInside(mouseX, mouseY) -> {
                isPressed = true
                focus()
                mouseClickListeners.any { it(mouseX, mouseY, button) } || mouseClickListeners.isEmpty()
            }
            else -> false
        }
    }

    override fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        if (isDraggingScrollbar) {
            isDraggingScrollbar = false
            return true
        }

        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val wasPressed = isPressed
        isPressed = false

        val childHandled = if (scrollable && !isMouseOnVisible(mouseX, mouseY)) {
            false
        } else {
            children.reversed().any { it.handleMouseRelease(mouseX, adjustedMouseY, button) }
        }

        return childHandled || (wasPressed && isPointInside(mouseX, mouseY) &&
                (mouseReleaseListeners.any { it(mouseX, mouseY, button) } || mouseReleaseListeners.isEmpty()))
    }

    fun getContentHeight(): Float {
        val visibleChildren = children.filter { !it.isFloating }
        if (visibleChildren.isEmpty()) return 0f
        val bottomChild = visibleChildren.maxByOrNull { it.y + it.height } ?: return 0f
        return bottomChild.y + bottomChild.height - (y + padding[0])
    }

    fun isMouseOnVisible(mouseX: Float, mouseY: Float): Boolean {
        if (!scrollable) return true
        val contentX = x + padding[3]
        val contentY = y + padding[0]
        val viewWidth = width - padding[1] - padding[3]
        val viewHeight = height - padding[0] - padding[2]
        return mouseX >= contentX && mouseX <= contentX + viewWidth &&
                mouseY >= contentY && mouseY <= contentY + viewHeight
    }

    fun isVisibleInScrollableParents(): Boolean {
        var current: Any? = this
        while (current != null) {
            when (current) {
                is VexelElement<*> -> {
                    if (!current.visible) return false
                    val centerX = getScreenX() + width / 2
                    val centerY = getScreenY() + height / 2
                    when (current) {
                        is Container if current.scrollable -> {
                            if (!current.isMouseOnVisible(centerX, centerY)) return false
                        }

                        is Rectangle if current.scrollable -> {
                            if (!current.isMouseOnVisible(centerX, centerY)) return false
                        }
                    }
                    current = current.parent
                }
                is VexelWindow -> break
                else -> break
            }
        }
        return true
    }

    public override fun getAutoWidth(): Float {
        val visibleChildren = children.filter { it.visible && !it.isFloating }
        if (visibleChildren.isEmpty()) return padding[1] + padding[3]
        val minX = visibleChildren.minOf { it.x }
        val maxX = visibleChildren.maxOf { it.x + it.width }
        return (maxX - minX) + padding[3] + padding[1]
    }

    public override fun getAutoHeight(): Float {
        val visibleChildren = children.filter { it.visible && !it.isFloating }
        if (visibleChildren.isEmpty()) return padding[0] + padding[2]
        val minY = visibleChildren.minOf { it.y }
        val maxY = visibleChildren.maxOf { it.y + it.height }
        return (maxY - minY) + padding[0] + padding[2]
    }

    override fun renderChildren(mouseX: Float, mouseY: Float) {
        if (scrollable) {
            val contentX = x + padding[3]
            val contentY = y + padding[0]
            val viewWidth = width - padding[1] - padding[3]
            val viewHeight = height - padding[0] - padding[2]
            val buffer = 2f

            NVGRenderer.push()
            NVGRenderer.pushScissor(
                contentX - buffer,
                contentY - buffer,
                viewWidth + buffer * 2,
                viewHeight + buffer * 2
            )
            NVGRenderer.translate(0f, -scrollOffset)
        }

        children.forEach { it.render(mouseX, mouseY) }

        if (scrollable) {
            NVGRenderer.popScissor()
            NVGRenderer.pop()
        }

        if (showScrollbar && (isHovered || isDraggingScrollbar)) drawScrollbar()
    }

    open fun getScreenX(): Float = x

    open fun getScreenY(): Float {
        var totalScrollOffset = 0f
        var current = parent
        while (current != null) {
            when (current) {
                is Container -> totalScrollOffset += current.scrollOffset
                is Rectangle -> totalScrollOffset += current.scrollOffset
                is VexelWindow -> break
            }
            current = if (current is VexelElement<*>) current.parent else null
        }
        return y - totalScrollOffset
    }

    open fun scrollable(enabled: Boolean): Container = apply { scrollable = enabled }

    open fun showScrollbar(show: Boolean): Container = apply {
        showScrollbar = show
    }

    open fun scrollbarWidth(width: Float): Container = apply {
        scrollbarWidth = width
    }

    open fun scrollbarColor(color: Int): Container = apply {
        scrollbarColor = color
    }

    open fun scrollbarRadius(radius: Float): Container = apply {
        scrollbarRadius = radius
    }

    open fun scrollbarPadding(padding: Float): Container = apply {
        scrollbarPadding = padding
    }

    open fun padding(top: Float = 0f, right: Float = 0f, bottom: Float = 0f, left: Float = 0f): Container = apply {
        padding[0] = top
        padding[1] = right
        padding[2] = bottom
        padding[3] = left
    }

    open fun padding(all: Float): Container = padding(all, all, all, all)

    open fun width(newWidth: Float): Container = apply { width = newWidth }

    open fun height(newHeight: Float): Container = apply { height = newHeight }
}