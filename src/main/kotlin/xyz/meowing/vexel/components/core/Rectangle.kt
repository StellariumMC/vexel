package xyz.meowing.vexel.components.core

import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.animateFloat
import xyz.meowing.vexel.animations.fadeIn
import xyz.meowing.vexel.animations.fadeOut
import xyz.meowing.vexel.core.VexelWindow
import xyz.meowing.vexel.components.base.Size
import xyz.meowing.vexel.components.base.VexelElement
import xyz.meowing.vexel.utils.render.NVGRenderer
import xyz.meowing.vexel.utils.style.Gradient
import java.awt.Color

open class Rectangle(
    var backgroundColor: Int = 0x80000000.toInt(),
    var borderColor: Int = 0xFFFFFFFF.toInt(),
    var borderRadius: Float = 0f,
    var borderThickness: Float = 0f,
    var padding: FloatArray = floatArrayOf(0f, 0f, 0f, 0f),
    var hoverColor: Int? = null,
    var pressedColor: Int? = null,
    widthType: Size = Size.Auto,
    heightType: Size = Size.Auto,
    var scrollable: Boolean = false
) : VexelElement<Rectangle>(widthType, heightType) {
    var secondBorderColor: Int = -1
    var secondBackgroundColor: Int = -1
    var gradientType: Gradient = Gradient.TopLeftToBottomRight
    var scrollOffset: Float = 0f
    var dropShadow: Boolean = false
    var showScrollbar: Boolean = true
    var scrollbarWidth: Float = 6f
    var scrollbarColor: Int = 0xFF7c7c7d.toInt()
    var scrollbarRadius: Float = 3f
    var scrollbarPadding: Float = 0f
    var scrollbarIgnorePadding: Boolean = false
    var scrollbarCustomPadding: Float = 0f
    var rotation: Float = 0f
    var scissorBufferVertical: Float = 0f
    var scissorBufferHorizontal: Float = 2f

    var shadowBlur = 30f
    var shadowSpread = 1f
    var shadowColor = 0x80000000.toInt()

    private var isDraggingScrollbar = false
    private var scrollbarDragOffset = 0f

    override fun onRender(mouseX: Float, mouseY: Float) {
        if (!visible || (height - (padding[0] + padding[2])) == 0f || (width - (padding[1] + padding[3])) == 0f) return

        val currentBgColor = when {
            pressed && pressedColor != null -> pressedColor!!
            hovered && hoverColor != null -> hoverColor!!
            else -> backgroundColor
        }

        val centerX = x + width / 2f
        val centerY = y + height / 2f

        if (rotation != 0f) {
            NVGRenderer.push()
            NVGRenderer.translate(centerX, centerY)
            NVGRenderer.rotate(Math.toRadians(rotation.toDouble()).toFloat())
            NVGRenderer.translate(-centerX, -centerY)
        }

        if (dropShadow) {
            NVGRenderer.dropShadow(x, y, width, height, shadowBlur, shadowSpread, Color(shadowColor), borderRadius)
        }

        if (currentBgColor != 0) {
            if(currentBgColor == backgroundColor && secondBackgroundColor != -1) {
                NVGRenderer.gradientRect(x, y, width, height, backgroundColor, secondBackgroundColor, gradientType, borderRadius)
            } else {
                NVGRenderer.rect(x, y, width, height, currentBgColor, borderRadius)
            }
        }

        if (borderThickness > 0f) {
            if (secondBorderColor != -1) {
                NVGRenderer.hollowGradientRect(x, y, width, height, borderThickness, borderColor, secondBorderColor, gradientType, borderRadius)
            } else {
                NVGRenderer.hollowRect(x, y, width, height, borderThickness, borderColor, borderRadius)
            }
        }

        if (rotation != 0f) {
            NVGRenderer.pop()
        }
    }

    private fun drawScrollbar() {
        if (!scrollable || !showScrollbar) return
        val contentHeight = getContentHeight()
        val viewHeight = height - padding[0] - padding[2]
        if (contentHeight <= viewHeight) return

        val scrollbarPaddingValue = if (scrollbarIgnorePadding) scrollbarCustomPadding else scrollbarPadding
        val scrollbarX = x + width - padding[1] - scrollbarWidth - scrollbarPaddingValue
        val scrollbarHeight = (viewHeight / contentHeight) * viewHeight
        val scrollbarY = y + padding[0] + (scrollOffset / contentHeight) * viewHeight

        NVGRenderer.rect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight, scrollbarColor, scrollbarRadius)
    }

    private fun isPointInScrollbar(mouseX: Float, mouseY: Float): Boolean {
        if (!scrollable || !showScrollbar) return false
        val contentHeight = getContentHeight()
        val viewHeight = height - padding[0] - padding[2]
        if (contentHeight <= viewHeight) return false

        val scrollbarPaddingValue = if (scrollbarIgnorePadding) scrollbarCustomPadding else scrollbarPadding
        val scrollbarX = x + width - padding[1] - scrollbarWidth - scrollbarPaddingValue
        val scrollbarHeight = (viewHeight / contentHeight) * viewHeight
        val scrollbarY = y + padding[0] + (scrollOffset / contentHeight) * viewHeight

        return mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight
    }

    private fun updateHoverStates(mouseX: Float, mouseY: Float) {
        val adjustedMouseY = if (scrollable) mouseY + scrollOffset else mouseY
        val wasHovered = isHovered
        isHovered = isPointInside(mouseX, mouseY)

        when {
            isHovered && !wasHovered -> {
                mouseEnterListeners.forEach { it(mouseX, mouseY) }
                tooltipElement?.let {
                    it.fadeIn(200, EasingType.EASE_OUT)
                    it.innerText.fadeIn(200, EasingType.EASE_OUT)
                }
            }
            !isHovered && wasHovered -> {
                mouseExitListeners.forEach { it(mouseX, mouseY) }
                tooltipElement?.let {
                    it.fadeOut(200, EasingType.EASE_OUT)
                    it.innerText.fadeOut(200, EasingType.EASE_OUT)
                }
            }
        }

        if (isHovered) mouseMoveListeners.forEach { it(mouseX, mouseY) }

        children.reversed().forEach { child ->
            if (scrollable && !isMouseOnVisible(mouseX, mouseY)) {
                unhoverRecursive(child, mouseX, adjustedMouseY)
            } else {
                child.handleMouseMove(mouseX, adjustedMouseY)
            }
        }
    }

    private fun unhoverRecursive(element: VexelElement<*>, mouseX: Float, mouseY: Float) {
        if (element.isHovered) {
            element.isHovered = false
            element.mouseExitListeners.forEach { it(mouseX, mouseY) }
        }
        element.children.forEach { unhoverRecursive(it, mouseX, mouseY) }
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

        return childHandled || (wasPressed && isPointInside(mouseX, mouseY) && (mouseReleaseListeners.any { it(mouseX, mouseY, button) } || mouseReleaseListeners.isEmpty()))
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

        return mouseX >= contentX && mouseX <= contentX + viewWidth && mouseY >= contentY && mouseY <= contentY + viewHeight
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
                        is Container -> {
                            if (current.scrollable && !current.isMouseOnVisible(centerX, centerY)) return false
                        }
                        is Rectangle -> {
                            if (current.scrollable && !current.isMouseOnVisible(centerX, centerY)) return false
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

            NVGRenderer.push()
            NVGRenderer.pushScissor(
                contentX - scissorBufferHorizontal,
                contentY - scissorBufferVertical,
                viewWidth + scissorBufferHorizontal * 2,
                viewHeight + scissorBufferVertical * 2
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

    fun rotateTo(angle: Float, duration: Long = 300, type: EasingType = EasingType.EASE_OUT, onComplete: (() -> Unit)? = null): Rectangle {
        animateFloat({ rotation }, { rotation = it }, angle, duration, type, onComplete = onComplete)
        return this
    }

    fun dropShadow(shadowBlur: Float=30f, shadowSpread: Float=1f, shadowColor: Int =0x000000): Rectangle = apply {
        dropShadow = true
        this.shadowBlur = shadowBlur
        this.shadowSpread = shadowSpread
        this.shadowColor = shadowColor
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

    open fun scrollable(enabled: Boolean): Rectangle = apply {
        scrollable = enabled
    }

    open fun setScissorBuffer(vertical: Float, horizontal: Float) = apply {
        scissorBufferVertical = vertical
        scissorBufferHorizontal = horizontal
    }

    open fun padding(top: Float = 0f, right: Float = 0f, bottom: Float = 0f, left: Float = 0f): Rectangle = apply {
        padding[0] = top
        padding[1] = right
        padding[2] = bottom
        padding[3] = left
    }

    open fun padding(all: Float): Rectangle = padding(all, all, all, all)

    open fun backgroundColor(color: Int): Rectangle = apply {
        backgroundColor = color
        secondBackgroundColor = -1
    }

    open fun setBackgroundGradientColor(color1: Int, color2: Int): Rectangle = apply {
        backgroundColor = color1
        secondBackgroundColor = color2
    }

    open fun setBorderGradientColor(color1: Int, color2: Int): Rectangle = apply {
        borderColor = color1
        secondBorderColor = color2
    }

    open fun borderColor(color: Int): Rectangle = apply {
        borderColor = color
        secondBorderColor = -1
    }

    open fun borderGradient(type: Gradient): Rectangle = apply {
        gradientType = type
    }

    open fun borderRadius(radius: Float): Rectangle = apply {
        borderRadius = radius
    }

    open fun borderThickness(thickness: Float): Rectangle = apply {
        borderThickness = thickness
    }

    open fun hoverColor(color: Int): Rectangle = apply {
        hoverColor = color
    }

    open fun pressedColor(color: Int): Rectangle = apply {
        pressedColor = color
    }

    open fun showScrollbar(show: Boolean): Rectangle = apply {
        showScrollbar = show
    }

    open fun scrollbarWidth(width: Float): Rectangle = apply {
        scrollbarWidth = width
    }

    open fun scrollbarColor(color: Int): Rectangle = apply {
        scrollbarColor = color
    }

    open fun scrollbarRadius(radius: Float): Rectangle = apply {
        scrollbarRadius = radius
    }

    open fun scrollbarPadding(padding: Float): Rectangle = apply {
        scrollbarPadding = padding
    }

    open fun scrollbarIgnorePadding(ignore: Boolean): Rectangle = apply {
        scrollbarIgnorePadding = ignore
    }

    open fun scrollbarCustomPadding(padding: Float): Rectangle = apply {
        scrollbarCustomPadding = padding
    }

    open fun width(newWidth: Float): Rectangle = apply {
        width = newWidth
    }

    open fun height(newHeight: Float): Rectangle = apply {
        height = newHeight
    }
}