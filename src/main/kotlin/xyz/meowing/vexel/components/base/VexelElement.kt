package xyz.meowing.vexel.components.base

import xyz.meowing.knit.api.input.KnitMouse
import xyz.meowing.knit.api.render.KnitResolution
import xyz.meowing.vexel.Vexel.renderEngine
import xyz.meowing.vexel.animations.AnimationManager
import xyz.meowing.vexel.core.VexelWindow
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.fadeIn
import xyz.meowing.vexel.animations.fadeOut
import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Tooltip

abstract class VexelElement<T : VexelElement<T>>(
    var widthType: Size = Size.Pixels,
    var heightType: Size = Size.Pixels
) {
    val children: MutableList<VexelElement<*>> = mutableListOf()

    var renderHitbox = false
    var xPositionConstraint = Pos.ParentPixels
    var yPositionConstraint = Pos.ParentPixels
    var xAlignment = Alignment.None
    var yAlignment = Alignment.None

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

    inner class Scaled {
        val scaleFactor get() = KnitResolution.scaleFactor.toFloat()

        val left: Float get() = x / scaleFactor
        val top: Float get() = y / scaleFactor
        val right: Float get() = (x + width) / scaleFactor
        val bottom: Float get() = (y + height) / scaleFactor
        val centerX: Float get() = (x + width / 2f) / scaleFactor
        val centerY: Float get() = (y + height / 2f) / scaleFactor
        val width: Float get() = this@VexelElement.width / scaleFactor
        val height: Float get() = this@VexelElement.height / scaleFactor
    }

    inner class Raw {
        val left get() = x
        val top get() = y
        val right get() = x + width
        val bottom get() = y + height
        val centerX get() = (left + right) / 2f
        val centerY get() = (top + bottom) / 2f
        val width get() = this@VexelElement.width
        val height get() = this@VexelElement.height
    }

    val raw = Raw()
    val scaled = Scaled()

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

    var xOffset: Float = 0f
    var yOffset: Float = 0f
    var xOffsetType: Offset = Offset.Pixels
    var yOffsetType: Offset = Offset.Pixels

    var isHovered: Boolean = false
    var isPressed: Boolean = false
    var isFocused: Boolean = false
    var isFloating: Boolean = false
    var ignoreFocus: Boolean = false
    var requiresFocus: Boolean = false

    val screenWidth: Int get() = KnitResolution.windowWidth
    val screenHeight: Int get() = KnitResolution.windowHeight

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
            if (xPositionConstraint in screenPosModes || xAlignment != Alignment.None) cache.positionCacheValid = false
            if (yPositionConstraint in screenPosModes || yAlignment != Alignment.None) cache.positionCacheValid = false
            if (widthType == Size.ParentPerc && parent !is VexelElement<*>) cache.sizeCacheValid = false
            if (heightType == Size.ParentPerc && parent !is VexelElement<*>) cache.sizeCacheValid = false
        }
        return resized
    }

    private fun renderDebugHitbox() {
        if (!renderHitbox) return
        children.forEach { it.enableDebugRendering() }

        val color = if (isFocused) 0xFFFFA500.toInt() else if (isHovered) 0xFFFFFF00.toInt() else 0xFF00FFFF.toInt()

        renderEngine.push()
        renderEngine.hollowRect(x, y, width, height, 1f, color, 0f)
        renderEngine.pop()
    }

    open fun destroy() {
        children.toList().forEach { it.destroy() }
        children.clear()
        listeners.clear()
        tooltipElement?.destroy()
        tooltipElement = null
        onValueChange = null

        (parent as? VexelElement<*>)?.children?.remove(this)
        parent = null
    }

    fun drawAsRoot() {
        renderEngine.beginFrame(screenWidth.toFloat(), screenHeight.toFloat())
        renderEngine.push()
        render(KnitMouse.Raw.x.toFloat(), KnitMouse.Raw.y.toFloat())
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

    private fun getParentPadding(): FloatArray {
        return when (val parent = findFirstVisibleParent()) {
            is Rectangle -> parent.padding
            is Container -> parent.padding
            else -> floatArrayOf(0f, 0f, 0f, 0f)
        }
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
                    val padding = getParentPadding()
                    val availableWidth = parentElement.width - (padding[1] + padding[3])
                    availableWidth * (widthPercent / 100f)
                }
            }
            Size.Pixels -> width
            Size.Fill -> {
                val parentElement = findFirstVisibleParent()
                if (parentElement == null) {
                    (screenWidth.toFloat() - x).coerceAtLeast(0f)
                } else {
                    val padding = getParentPadding()
                    val parentRightEdge = parentElement.x + parentElement.width - padding[1]
                    (parentRightEdge - x).coerceAtLeast(0f)
                }
            }
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
                    val padding = getParentPadding()
                    val availableHeight = parentElement.height - (padding[0] + padding[2])
                    availableHeight * (heightPercent / 100f)
                }
            }
            Size.Pixels -> height

            Size.Fill -> {
                val parentElement = findFirstVisibleParent()
                if (parentElement == null) {
                    (screenHeight.toFloat() - y).coerceAtLeast(0f)
                } else {
                    val padding = getParentPadding()
                    val parentBottomEdge = parentElement.y + parentElement.height - padding[2]
                    (parentBottomEdge - y).coerceAtLeast(0f)
                }
            }
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

    private fun computeOffset(offset: Float, offsetType: Offset, isWidth: Boolean): Float {
        return when (offsetType) {
            Offset.Pixels -> offset
            Offset.Percent -> {
                val parentElement = findFirstVisibleParent()
                val base = if (isWidth) {
                    parentElement?.width ?: screenWidth.toFloat()
                } else {
                    parentElement?.height ?: screenHeight.toFloat()
                }
                base * (offset / 100f)
            }
        }
    }

    fun updateX() {
        if (cache.positionCacheValid) return

        val visibleParent = findFirstVisibleParent()
        val padding = getParentPadding()
        val computedXOffset = computeOffset(xOffset, xOffsetType, true)

        x = when (xPositionConstraint) {
            Pos.ParentPercent -> {
                val base = if (visibleParent != null) {
                    visibleParent.x + padding[3] + (visibleParent.width - padding[1] - padding[3]) * (xConstraint / 100f)
                } else {
                    xConstraint
                }
                base + computedXOffset
            }
            Pos.ScreenPercent -> screenWidth * (xConstraint / 100f) + computedXOffset
            Pos.ParentPixels -> {
                val base = if (visibleParent != null) visibleParent.x + padding[3] + xConstraint else xConstraint
                base + computedXOffset
            }
            Pos.ScreenPixels -> xConstraint + computedXOffset
            Pos.ParentCenter -> {
                val base = if (visibleParent != null) {
                    val availableWidth = visibleParent.width - padding[1] - padding[3]
                    visibleParent.x + padding[3] + (availableWidth - width) / 2f
                } else {
                    xConstraint
                }
                base + computedXOffset
            }
            Pos.ScreenCenter -> (screenWidth / 2f) - (width / 2f) + xConstraint + computedXOffset
            Pos.AfterSibling -> computeAfterSiblingX(visibleParent) + computedXOffset
            Pos.MatchSibling -> computeMatchSiblingX() + computedXOffset
        }

        x = applyXAlignment(x, visibleParent, padding)
    }

    private fun computeAfterSiblingX(visibleParent: VexelElement<*>?): Float {
        val parentElement = parent as? VexelElement<*> ?: return 0f

        val padding = getParentPadding()
        val index = parentElement.children.indexOf(this)
        if (index <= 0) {
            return if (visibleParent != null) visibleParent.x + padding[3] else 0f
        }

        val prev = parentElement.children[index - 1]
        return prev.x + prev.width
    }

    private fun computeMatchSiblingX(): Float {
        val parentElement = parent as? VexelElement<*> ?: return 0f

        val index = parentElement.children.indexOf(this)
        return if (index > 0) parentElement.children[index - 1].x else 0f
    }

    fun updateY() {
        if (cache.positionCacheValid) return

        val visibleParent = findFirstVisibleParent()
        val padding = getParentPadding()
        val computedYOffset = computeOffset(yOffset, yOffsetType, false)

        y = when (yPositionConstraint) {
            Pos.ParentPercent -> {
                val base = if (visibleParent != null) {
                    visibleParent.y + padding[0] + (visibleParent.height - padding[0] - padding[2]) * (yConstraint / 100f)
                } else {
                    yConstraint
                }
                base + computedYOffset
            }
            Pos.ScreenPercent -> screenHeight * (yConstraint / 100f) + computedYOffset
            Pos.ParentPixels -> {
                val base = if (visibleParent != null) visibleParent.y + padding[0] + yConstraint else yConstraint
                base + computedYOffset
            }
            Pos.ScreenPixels -> yConstraint + computedYOffset
            Pos.ParentCenter -> {
                val base = if (visibleParent != null) {
                    val availableHeight = visibleParent.height - padding[0] - padding[2]
                    visibleParent.y + padding[0] + (availableHeight - height) / 2f
                } else {
                    yConstraint
                }
                base + computedYOffset
            }
            Pos.ScreenCenter -> (screenHeight / 2f) - (height / 2f) + yConstraint + computedYOffset
            Pos.AfterSibling -> computeAfterSiblingY(visibleParent) + computedYOffset
            Pos.MatchSibling -> computeMatchSiblingY() + computedYOffset
        }

        y = applyYAlignment(y, visibleParent, padding)
    }

    private fun computeAfterSiblingY(visibleParent: VexelElement<*>?): Float {
        val parentElement = parent as? VexelElement<*> ?: return yConstraint

        val padding = getParentPadding()
        val index = parentElement.children.indexOf(this)
        if (index <= 0) {
            return if (visibleParent != null) visibleParent.y + padding[0] + yConstraint else yConstraint
        }

        val prev = parentElement.children[index - 1]
        return prev.y + prev.height + yConstraint
    }

    private fun computeMatchSiblingY(): Float {
        val parentElement = parent as? VexelElement<*> ?: return yConstraint

        val index = parentElement.children.indexOf(this)
        return if (index > 0) parentElement.children[index - 1].y else yConstraint
    }

    private fun applyXAlignment(baseX: Float, visibleParent: VexelElement<*>?, padding: FloatArray): Float {
        return when (xAlignment) {
            Alignment.None -> baseX
            Alignment.Start -> {
                val leftEdge = if (visibleParent != null) visibleParent.x + padding[3] else 0f
                leftEdge + xConstraint
            }
            Alignment.End -> {
                val rightEdge = if (visibleParent != null) visibleParent.x + visibleParent.width - padding[1] else screenWidth.toFloat()
                rightEdge - width + xConstraint
            }
        }
    }

    private fun applyYAlignment(baseY: Float, visibleParent: VexelElement<*>?, padding: FloatArray): Float {
        return when (yAlignment) {
            Alignment.None -> baseY
            Alignment.Start -> {
                val topEdge = if (visibleParent != null) visibleParent.y + padding[0] else 0f
                topEdge + yConstraint
            }
            Alignment.End -> {
                val bottomEdge = if (visibleParent != null) visibleParent.y + visibleParent.height - padding[2] else screenHeight.toFloat()
                bottomEdge - height + yConstraint
            }
        }
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
    fun setPositioning(xVal: Any, xPos: Pos, yVal: Any, yPos: Pos): T {
        this.xConstraint = when (xVal) {
            is DimensionValue -> xVal.resolve(this, true)
            is Float -> xVal
            is Int -> xVal.toFloat()
            else -> 0f
        }
        this.xPositionConstraint = xPos

        this.yConstraint = when (yVal) {
            is DimensionValue -> yVal.resolve(this, false)
            is Float -> yVal
            is Int -> yVal.toFloat()
            else -> 0f
        }
        this.yPositionConstraint = yPos
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setSizing(width: Any, widthType: Size, height: Any, heightType: Size): T {
        this.widthType = widthType
        this.heightType = heightType

        val resolvedWidth = when (width) {
            is DimensionValue -> width.resolve(this, true)
            is Float -> width
            is Int -> width.toFloat()
            else -> 0f
        }

        val resolvedHeight = when (height) {
            is DimensionValue -> height.resolve(this, false)
            is Float -> height
            is Int -> height.toFloat()
            else -> 0f
        }

        if (widthType == Size.Pixels) this.width = resolvedWidth else this.widthPercent = resolvedWidth
        if (heightType == Size.Pixels) this.height = resolvedHeight else this.heightPercent = resolvedHeight

        cache.sizeCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setAlignment(xAlignment: Alignment, yAlignment: Alignment): T {
        this.xAlignment = xAlignment
        this.yAlignment = yAlignment
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun alignLeft(): T {
        this.xAlignment = Alignment.Start
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun alignRight(): T {
        this.xAlignment = Alignment.End
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun alignTop(): T {
        this.yAlignment = Alignment.Start
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun alignBottom(): T {
        this.yAlignment = Alignment.End
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setOffset(xOffset: Float, xOffsetType: Offset, yOffset: Float, yOffsetType: Offset): T {
        this.xOffset = xOffset
        this.xOffsetType = xOffsetType
        this.yOffset = yOffset
        this.yOffsetType = yOffsetType
        cache.positionCacheValid = false
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    fun setOffset(xOffset: Float, yOffset: Float): T {
        this.xOffset = xOffset
        this.xOffsetType = Offset.Pixels
        this.yOffset = yOffset
        this.yOffsetType = Offset.Pixels
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
    fun enableDebugRendering(): T {
        renderHitbox = true
        return this as T
    }

    val hovered: Boolean get() = isHovered
    val pressed: Boolean get() = isPressed
    val focused: Boolean get() = isFocused
}

fun Float.percent(): DimensionValue = DimensionValue.Percent(this)
fun Float.pixels(): DimensionValue = DimensionValue.Pixels(this)