package xyz.meowing.vexel.components.base

import xyz.meowing.vexel.Vexel.client as mc
import xyz.meowing.vexel.Vexel.renderEngine
import xyz.meowing.vexel.animations.AnimationManager
import xyz.meowing.vexel.core.VexelWindow
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.fadeIn
import xyz.meowing.vexel.animations.fadeOut
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Tooltip
import xyz.meowing.vexel.utils.MouseUtils

abstract class VexelElement<T : VexelElement<T>>(
    var widthType: Size = Size.Pixels,
    var heightType: Size = Size.Pixels
) {
    val children: MutableList<VexelElement<*>> = mutableListOf()

    var renderHitbox = false
    var xPositionConstraint = Pos.ParentPixels
    var yPositionConstraint = Pos.ParentPixels

    var x: Float = 0f
        set(value) {
            field = value
            invalidateChildrenPositions()
        }

    var y: Float = 0f
        set(value) {
            field = value
            invalidateChildrenPositions()
        }

    var width: Float = 0f
        set(value) {
            field = value
            invalidateChildrenPositions()
            invalidateChildrenSizes()
        }

    var height: Float = 0f
        set(value) {
            field = value
            invalidateChildrenPositions()
            invalidateChildrenSizes()
        }

    var widthPercent: Float = 100f
    var heightPercent: Float = 100f

    var visible: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                cache.invalidate()
                invalidateChildrenCache()
            }
        }

    var xConstraint: Float = 0f
    var yConstraint: Float = 0f

    var isHovered: Boolean = false
    var isPressed: Boolean = false
    var isFocused: Boolean = false
    var isFloating: Boolean = false
    var ignoreFocus: Boolean = false
    var requiresFocus: Boolean = false

    val screenWidth: Int get() = mc.displayWidth
    val screenHeight: Int get() = mc.displayHeight

    var parent: Any? = null
        set(value) {
            field = value
            cache.invalidate()
        }

    var tooltipElement: Tooltip? = null
    var onValueChange: ((Any) -> Unit)? = null

    internal val cache = ElementCache()
    internal val listeners = ElementListeners()

    val mouseEnterListeners get() = listeners.mouseEnter
    val mouseExitListeners get() = listeners.mouseExit
    val mouseMoveListeners get() = listeners.mouseMove
    val mouseScrollListeners get() = listeners.mouseScroll
    val mouseClickListeners get() = listeners.mouseClick
    val mouseReleaseListeners get() = listeners.mouseRelease
    val charTypeListeners get() = listeners.charType

    private fun invalidateChildrenCache() {
        for (child in children) {
            child.cache.invalidate()
        }
    }

    private fun invalidateChildrenPositions() {
        for (child in children) {
            child.cache.invalidatePosition()
        }
    }

    private fun invalidateChildrenSizes() {
        for (child in children) {
            child.cache.invalidateSize()
        }
    }

    private fun checkScreenResize(): Boolean {
        val resized = cache.lastScreenWidth != screenWidth || cache.lastScreenHeight != screenHeight
        if (resized) {
            cache.lastScreenWidth = screenWidth
            cache.lastScreenHeight = screenHeight

            val screenPosModes = setOf(Pos.ScreenPercent, Pos.ScreenPixels, Pos.ScreenCenter)
            if (xPositionConstraint in screenPosModes) cache.positionCacheValid = false
            if (yPositionConstraint in screenPosModes) cache.positionCacheValid = false
            if (widthType == Size.ParentPerc && parent !is VexelElement<*>) cache.sizeCacheValid = false
            if (heightType == Size.ParentPerc && parent !is VexelElement<*>) cache.sizeCacheValid = false
        }
        return resized
    }

    private fun renderDebugHitbox() {
        if (!renderHitbox) return
        renderEngine.push()
        renderEngine.hollowRect(x, y, width, height, 2f, 0xFF00FF00.toInt(), 0f)
        renderEngine.pop()
    }

    open fun destroy() {
        children.forEach { it.destroy() }
        children.clear()
        listeners.clear()
    }

    fun drawAsRoot() {
        renderEngine.beginFrame(mc.displayWidth.toFloat(), mc.displayHeight.toFloat())
        renderEngine.push()
        render(MouseUtils.rawX.toFloat(), MouseUtils.rawY.toFloat())
        AnimationManager.update()
        renderEngine.pop()
        renderEngine.endFrame()
    }

    fun findFirstVisibleParent(): VexelElement<*>? {
        if (cache.parentCacheValid) return cache.cachedParent

        var current = parent
        while (current != null) {
            if (current is VexelElement<*> && current.visible) {
                cache.cachedParent = current
                cache.parentCacheValid = true
                return current
            }
            if (current is VexelWindow) {
                cache.cachedParent = null
                cache.parentCacheValid = true
                return null
            }
            current = (current as? VexelElement<*>)?.parent
        }

        cache.cachedParent = null
        cache.parentCacheValid = true
        return null
    }

    open fun updateWidth() {
        if (cache.sizeCacheValid) {
            width = cache.cachedWidth
            return
        }

        width = when (widthType) {
            Size.Auto -> getAutoWidth()
            Size.ParentPerc -> {
                val parentElement = findFirstVisibleParent()
                if (parentElement == null) {
                    screenWidth * (widthPercent / 100f)
                } else {
                    var w = parentElement.width * (widthPercent / 100f)
                    if (parentElement is Rectangle) w -= (parentElement.padding[1] + parentElement.padding[3])
                    w
                }
            }
            Size.Pixels -> width
        }

        cache.cachedWidth = width
    }

    open fun updateHeight() {
        if (cache.sizeCacheValid) {
            height = cache.cachedHeight
            return
        }

        height = when (heightType) {
            Size.Auto -> getAutoHeight()
            Size.ParentPerc -> {
                val parentElement = findFirstVisibleParent()
                if (parentElement == null) {
                    screenHeight * (heightPercent / 100f)
                } else {
                    var h = parentElement.height * (heightPercent / 100f)
                    if (parentElement is Rectangle) h -= (parentElement.padding[0] + parentElement.padding[2])
                    h
                }
            }
            Size.Pixels -> height
        }

        cache.cachedHeight = height
    }

    protected open fun getAutoWidth(): Float {
        val maxWidth = children
            .filter { it.visible && !it.isFloating }
            .maxOfOrNull { (x - it.x) + it.width }
        return maxWidth?.coerceAtLeast(0f) ?: 0f
    }

    protected open fun getAutoHeight(): Float {
        val maxHeight = children
            .filter { it.visible && !it.isFloating }
            .maxOfOrNull { (y - it.y) + it.height }
        return maxHeight?.coerceAtLeast(0f) ?: 0f
    }

    fun updateX() {
        if (cache.positionCacheValid) return

        val visibleParent = findFirstVisibleParent()

        x = when (xPositionConstraint) {
            Pos.ParentPercent -> {
                if (visibleParent != null) visibleParent.x + (visibleParent.width * (xConstraint / 100f))
                else xConstraint
            }
            Pos.ScreenPercent -> screenWidth * (xConstraint / 100f)
            Pos.ParentPixels -> {
                if (visibleParent != null) visibleParent.x + xConstraint
                else xConstraint
            }
            Pos.ScreenPixels -> xConstraint
            Pos.ParentCenter -> {
                if (visibleParent != null) visibleParent.x + (visibleParent.width - width) / 2f
                else xConstraint
            }
            Pos.ScreenCenter -> (screenWidth / 2f) - (width / 2f) + xConstraint
            Pos.AfterSibling -> computeAfterSiblingX(visibleParent)
            Pos.MatchSibling -> computeMatchSiblingX()
        }
    }

    private fun computeAfterSiblingX(visibleParent: VexelElement<*>?): Float {
        val parentElement = parent as? VexelElement<*> ?: return xConstraint

        val index = parentElement.children.indexOf(this)
        if (index <= 0) {
            return if (visibleParent != null) visibleParent.x + xConstraint else xConstraint
        }

        val prev = parentElement.children[index - 1]
        val padding = if (prev is Rectangle) -(prev.padding[1] + prev.padding[3]) else 0f

        return prev.x + prev.width + xConstraint + padding
    }

    private fun computeMatchSiblingX(): Float {
        val parentElement = parent as? VexelElement<*> ?: return xConstraint

        val index = parentElement.children.indexOf(this)
        return if (index > 0) parentElement.children[index - 1].x else xConstraint
    }

    fun updateY() {
        if (cache.positionCacheValid) return

        val visibleParent = findFirstVisibleParent()

        y = when (yPositionConstraint) {
            Pos.ParentPercent -> {
                if (visibleParent != null) visibleParent.y + (visibleParent.height * (yConstraint / 100f))
                else yConstraint
            }
            Pos.ScreenPercent -> screenHeight * (yConstraint / 100f)
            Pos.ParentPixels -> {
                if (visibleParent != null) visibleParent.y + yConstraint
                else yConstraint
            }
            Pos.ScreenPixels -> yConstraint
            Pos.ParentCenter -> {
                if (visibleParent != null) visibleParent.y + visibleParent.height / 2f - height / 2f
                else yConstraint
            }
            Pos.ScreenCenter -> (screenHeight / 2f) - (height / 2f) + yConstraint
            Pos.AfterSibling -> computeAfterSiblingY(visibleParent)
            Pos.MatchSibling -> computeMatchSiblingY()
        }
    }

    private fun computeAfterSiblingY(visibleParent: VexelElement<*>?): Float {
        val parentElement = parent as? VexelElement<*> ?: return yConstraint

        val index = parentElement.children.indexOf(this)
        if (index <= 0) {
            return if (visibleParent != null) visibleParent.y + yConstraint else yConstraint
        }

        val prev = parentElement.children[index - 1]
        val padding = if (parentElement is Rectangle) -parentElement.padding[0] else 0f

        return prev.y + prev.height + yConstraint + padding
    }

    private fun computeMatchSiblingY(): Float {
        val parentElement = parent as? VexelElement<*> ?: return yConstraint

        val index = parentElement.children.indexOf(this)
        if (index <= 0) return yConstraint

        val prev = parentElement.children[index - 1]
        val padding = if (prev is Rectangle) yConstraint - (prev.padding[0] + prev.padding[2]) else yConstraint

        return prev.y + padding
    }

    fun isPointInside(mouseX: Float, mouseY: Float): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
    }

    open fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false

        val wasHovered = isHovered
        isHovered = isPointInside(mouseX, mouseY)

        when {
            isHovered && !wasHovered -> {
                for (listener in mouseEnterListeners) {
                    listener(mouseX, mouseY)
                }

                tooltipElement?.let { tooltip ->
                    tooltip.fadeIn(200, EasingType.EASE_OUT)
                    tooltip.innerText.fadeIn(200, EasingType.EASE_OUT)
                }
            }
            !isHovered && wasHovered -> {
                for (listener in mouseExitListeners) {
                    listener(mouseX, mouseY)
                }

                tooltipElement?.let { tooltip ->
                    tooltip.fadeOut(200, EasingType.EASE_OUT)
                    tooltip.innerText.fadeOut(200, EasingType.EASE_OUT)
                }
            }
        }

        if (isHovered) {
            for (listener in mouseMoveListeners) {
                listener(mouseX, mouseY)
            }
        }

        val childHandled = children.reversed().any { it.handleMouseMove(mouseX, mouseY) }
        return childHandled || isHovered
    }

    open fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any {
            it.handleMouseClick(mouseX, mouseY, button)
        }

        return when {
            childHandled -> true
            isPointInside(mouseX, mouseY) -> {
                isPressed = true
                focus()
                val listenerHandled = mouseClickListeners.any {
                    it(mouseX, mouseY, button)
                }

                listenerHandled || mouseClickListeners.isEmpty()
            }
            else -> {
                if (requiresFocus && isFocused) unfocus()
                false
            }
        }
    }

    open fun handleMouseRelease(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val wasPressed = isPressed
        isPressed = false

        val childHandled = children.reversed().any {
            it.handleMouseRelease(mouseX, mouseY, button)
        }

        if (childHandled) return true

        if (wasPressed && isPointInside(mouseX, mouseY)) {
            val listenerHandled = mouseReleaseListeners.any {
                it(mouseX, mouseY, button)
            }

            return listenerHandled || mouseReleaseListeners.isEmpty()
        }

        return false
    }

    open fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any {
            it.handleMouseScroll(mouseX, mouseY, horizontal, vertical)
        }

        if (childHandled) return true

        if (isPointInside(mouseX, mouseY)) {
            return mouseScrollListeners.any {
                it(mouseX, mouseY, horizontal, vertical)
            }
        }

        return false
    }

    open fun handleCharType(keyCode: Int, scanCode: Int, charTyped: Char): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any {
            it.handleCharType(keyCode, scanCode, charTyped)
        }

        val selfHandled = if (isFocused || ignoreFocus) charTypeListeners.any { it(keyCode, scanCode, charTyped) } else false

        return childHandled || selfHandled
    }

    fun focus() {
        getRootElement().unfocusAll()
        isFocused = true
    }

    fun unfocus() {
        isFocused = false
    }

    private fun unfocusAll() {
        if (isFocused) unfocus()
        children.forEach { it.unfocusAll() }
    }

    fun getRootElement(): VexelElement<*> {
        var current: VexelElement<*> = this
        while (current.parent is VexelElement<*>) {
            current = current.parent as VexelElement<*>
        }
        return current
    }

    open fun onWindowResize() {
        cache.invalidate()
        for (child in children) child.onWindowResize()
    }

    open fun render(mouseX: Float, mouseY: Float) {
        if (!visible) return

        checkScreenResize()

        updateHeight()
        updateWidth()
        updateX()
        updateY()

        cache.sizeCacheValid = true
        cache.positionCacheValid = true

        onRender(mouseX, mouseY)
        renderChildren(mouseX, mouseY)
        renderDebugHitbox()
    }

    protected open fun renderChildren(mouseX: Float, mouseY: Float) {
        children.forEach { it.render(mouseX, mouseY) }
    }

    protected abstract fun onRender(mouseX: Float, mouseY: Float)

    @Suppress("UNCHECKED_CAST")
    fun childOf(parent: VexelElement<*>): T {
        parent.addChild(this)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun childOf(parent: VexelWindow): T {
        parent.addChild(this)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun addChild(child: VexelElement<*>): T {
        child.parent = this
        children.add(child)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setSizing(widthType: Size, heightType: Size): T {
        this.widthType = widthType
        this.heightType = heightType
        cache.sizeCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setSizing(width: Float, widthType: Size, height: Float, heightType: Size): T {
        this.widthType = widthType
        this.heightType = heightType

        if (widthType == Size.Pixels) this.width = width else this.widthPercent = width
        if (heightType == Size.Pixels) this.height = height else this.heightPercent = height

        cache.sizeCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setPositioning(xConstraint: Pos, yConstraint: Pos): T {
        this.xPositionConstraint = xConstraint
        this.yPositionConstraint = yConstraint
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setPositioning(xVal: Float, xPos: Pos, yVal: Float, yPos: Pos): T {
        this.xConstraint = xVal
        this.xPositionConstraint = xPos
        this.yConstraint = yVal
        this.yPositionConstraint = yPos
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun addTooltip(tooltip: String): T {
        tooltipElement = Tooltip().apply {
            innerText.text = tooltip
            childOf(this@VexelElement)
        }
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onMouseEnter(callback: (Float, Float) -> Unit): T {
        mouseEnterListeners.add(callback)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onMouseExit(callback: (Float, Float) -> Unit): T {
        mouseExitListeners.add(callback)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onMouseMove(callback: (Float, Float) -> Unit): T {
        mouseMoveListeners.add(callback)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onHover(onEnter: (Float, Float) -> Unit, onExit: (Float, Float) -> Unit = { _, _ -> }): T {
        onMouseEnter(onEnter)
        onMouseExit(onExit)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onMouseClick(callback: (Float, Float, Int) -> Boolean): T {
        mouseClickListeners.add(callback)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onClick(callback: (Float, Float, Int) -> Boolean): T {
        return onMouseClick(callback)
    }

    @Suppress("UNCHECKED_CAST")
    fun onMouseRelease(callback: (Float, Float, Int) -> Boolean): T {
        mouseReleaseListeners.add(callback)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onRelease(callback: (Float, Float, Int) -> Boolean): T {
        return onMouseRelease(callback)
    }

    @Suppress("UNCHECKED_CAST")
    fun onMouseScroll(callback: (Float, Float, Double, Double) -> Boolean): T {
        mouseScrollListeners.add(callback)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onScroll(callback: (Float, Float, Double, Double) -> Boolean): T {
        return onMouseScroll(callback)
    }

    @Suppress("UNCHECKED_CAST")
    fun onCharType(callback: (Int, Int, Char) -> Boolean): T {
        charTypeListeners.add(callback)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun onValueChange(callback: (Any) -> Unit): T {
        this.onValueChange = callback
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun ignoreMouseEvents(): T {
        mouseClickListeners.add { _, _, _ -> false }
        mouseReleaseListeners.add { _, _, _ -> false }
        mouseScrollListeners.add { _, _, _, _ -> false }
        mouseMoveListeners.add { _, _ -> }
        mouseEnterListeners.add { _, _ -> }
        mouseExitListeners.add { _, _ -> }
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun ignoreFocus(): T {
        ignoreFocus = true
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setFloating(): T {
        isFloating = true
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setRequiresFocus(): T {
        requiresFocus = true
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun show(): T {
        visible = true
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun hide(): T {
        visible = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun renderHitbox(bool: Boolean): T {
        renderHitbox = bool
        return this as T
    }

    val hovered: Boolean get() = isHovered
    val pressed: Boolean get() = isPressed
    val focused: Boolean get() = isFocused
}