package xyz.meowing.vexel.components.base

import net.minecraft.client.util.Window
import xyz.meowing.vexel.Vexel.mc
import xyz.meowing.vexel.core.VexelWindow
import xyz.meowing.vexel.animations.EasingType
import xyz.meowing.vexel.animations.fadeIn
import xyz.meowing.vexel.animations.fadeOut
import xyz.meowing.vexel.components.core.Rectangle
import xyz.meowing.vexel.components.core.Tooltip

enum class Size {
    Auto,
    ParentPerc,
    Pixels
}

enum class Pos {
    ParentPercent,
    ScreenPercent,
    ParentPixels,
    ScreenPixels,
    ParentCenter,
    ScreenCenter,
    AfterSibling,
    MatchSibling
}

sealed class Constraint {
    data class SizeConstraint(val size: Size, val value: Float) : Constraint()
    data class PosConstraint(val pos: Pos, val offset: Float) : Constraint()
    data class RawPixels(val value: Float) : Constraint()
}

abstract class VexelElement<T : VexelElement<T>>(
    var widthType: Size = Size.Pixels,
    var heightType: Size = Size.Pixels
) {
    val children: MutableList<VexelElement<*>> = mutableListOf()

    var xPositionConstraint = Pos.ParentPixels
    var yPositionConstraint = Pos.ParentPixels
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f
    var widthPercent: Float = 100f
    var heightPercent: Float = 100f
    var visible: Boolean = true

    var xConstraint: Float = 0f
    var yConstraint: Float = 0f

    var isHovered: Boolean = false
    var isPressed: Boolean = false
    var isFocused: Boolean = false
    var isFloating: Boolean = false
    var ignoreFocus: Boolean = false
    var requiresFocus: Boolean = false

    val window: Window get() = mc.window
    val screenWidth: Int get() = window.width
    val screenHeight: Int get() = window.height

    var parent: Any? = null
    var tooltipElement: Tooltip? = null

    val mouseEnterListeners = mutableListOf<(Float, Float) -> Unit>()
    val mouseExitListeners = mutableListOf<(Float, Float) -> Unit>()
    val mouseMoveListeners = mutableListOf<(Float, Float) -> Unit>()
    val mouseScrollListeners = mutableListOf<(Float, Float, Double, Double) -> Boolean>()
    val mouseClickListeners = mutableListOf<(Float, Float, Int) -> Boolean>()
    val mouseReleaseListeners = mutableListOf<(Float, Float, Int) -> Boolean>()
    val charTypeListeners = mutableListOf<(Int, Int, Char) -> Boolean>()

    var onValueChange: ((Any) -> Unit)? = null

    open fun destroy() {
        children.forEach { it.destroy() }
        children.clear()
        mouseEnterListeners.clear()
        mouseExitListeners.clear()
        mouseMoveListeners.clear()
        mouseScrollListeners.clear()
        mouseClickListeners.clear()
        mouseReleaseListeners.clear()
        charTypeListeners.clear()
    }

    fun findFirstVisibleParent(): VexelElement<*>? {
        var current = parent
        while (current != null) {
            if (current is VexelElement<*> && current.visible) return current
            if (current is VexelWindow) return null
            current = if (current is VexelElement<*>) current.parent else null
        }
        return null
    }

    open fun updateWidth() {
        width = when (widthType) {
            Size.Auto -> getAutoWidth()
            Size.ParentPerc -> {
                val parentElement = findFirstVisibleParent()
                if (parentElement == null) {
                    screenWidth * (widthPercent / 100f)
                } else {
                    var modifiedWidth = parentElement.width * (widthPercent / 100f)
                    if (parentElement is Rectangle) modifiedWidth -= (parentElement.padding[1] + parentElement.padding[3])
                    modifiedWidth
                }
            }
            Size.Pixels -> width
        }
    }

    open fun updateHeight() {
        height = when (heightType) {
            Size.Auto -> getAutoHeight()
            Size.ParentPerc -> {
                val parentElement = findFirstVisibleParent()
                if (parentElement == null) {
                    screenHeight * (heightPercent / 100f)
                } else {
                    var modifiedHeight = parentElement.height * (heightPercent / 100f)
                    if (parentElement is Rectangle) modifiedHeight -= (parentElement.padding[0] + parentElement.padding[2])
                    modifiedHeight
                }
            }
            Size.Pixels -> height
        }
    }

    protected open fun getAutoWidth(): Float =
        children.filter { it.visible && !it.isFloating }.maxOfOrNull {
            (this.x - it.x) + it.width
        }?.coerceAtLeast(0f) ?: 0f

    protected open fun getAutoHeight(): Float =
        children.filter { it.visible && !it.isFloating }.maxOfOrNull {
            (this.y - it.y) + it.height
        }?.coerceAtLeast(0f) ?: 0f

    fun updateX() {
        val visibleParent = findFirstVisibleParent()

        x = when (xPositionConstraint) {
            Pos.ParentPercent -> if (visibleParent != null) visibleParent.x + (visibleParent.width * (xConstraint / 100f)) else xConstraint
            Pos.ScreenPercent -> screenWidth * (xConstraint / 100f)
            Pos.ParentPixels -> if (visibleParent != null) visibleParent.x + xConstraint else xConstraint
            Pos.ScreenPixels -> xConstraint
            Pos.ParentCenter -> {
                if (visibleParent != null) {
                    visibleParent.x + (visibleParent.width - width) / 2f
                } else xConstraint
            }
            Pos.ScreenCenter -> (screenWidth / 2f) - (width / 2f) + xConstraint
            Pos.AfterSibling -> {
                val parentElement = parent
                if (parentElement is VexelElement<*>) {
                    val index = parentElement.children.indexOf(this)
                    if (index > 0) {
                        val prev = parentElement.children[index - 1]
                        (prev.x + prev.width + xConstraint) + if (prev is Rectangle) - (prev.padding[1] + prev.padding[3]) else 0f
                    } else {
                        if (visibleParent != null) visibleParent.x + xConstraint else xConstraint
                    }
                } else xConstraint
            }
            Pos.MatchSibling -> {
                val parentElement = parent
                if (parentElement is VexelElement<*>) {
                    val index = parentElement.children.indexOf(this)
                    if (index > 0) {
                        val prev = parentElement.children[index - 1]
                        prev.x
                    } else xConstraint
                } else xConstraint
            }
        }
    }

    fun updateY() {
        val visibleParent = findFirstVisibleParent()

        y = when (yPositionConstraint) {
            Pos.ParentPercent -> if (visibleParent != null) visibleParent.y + (visibleParent.height * (yConstraint / 100f)) else yConstraint
            Pos.ScreenPercent -> screenHeight * (yConstraint / 100f)
            Pos.ParentPixels -> if (visibleParent != null) visibleParent.y + yConstraint else yConstraint
            Pos.ScreenPixels -> yConstraint
            Pos.ParentCenter -> {
                if (visibleParent != null) {
                    visibleParent.y + visibleParent.height / 2f - height / 2f
                } else yConstraint
            }
            Pos.ScreenCenter -> (screenHeight / 2f) - (height / 2f) + yConstraint
            Pos.AfterSibling -> {
                val parentElement = parent
                if (parentElement is VexelElement<*>) {
                    val index = parentElement.children.indexOf(this)
                    if (index > 0) {
                        val prev = parentElement.children[index - 1]
                        (prev.y + prev.height + yConstraint) + if (parentElement is Rectangle) - (parentElement.padding[0]) else 0f
                    } else {
                        if (visibleParent != null) visibleParent.y + yConstraint else yConstraint
                    }
                } else yConstraint
            }
            Pos.MatchSibling -> {
                val parentElement = parent
                if (parentElement is VexelElement<*>) {
                    val index = parentElement.children.indexOf(this)
                    if (index > 0) {
                        val prev = parentElement.children[index - 1]
                        if (prev is Rectangle) {
                            prev.y + yConstraint - (prev.padding[0] + prev.padding[2])
                        } else prev.y + yConstraint
                    } else yConstraint
                } else yConstraint
            }
        }
    }

    fun isPointInside(mouseX: Float, mouseY: Float): Boolean =
        mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

    open fun handleMouseMove(mouseX: Float, mouseY: Float): Boolean {
        if (!visible) return false

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

        return children.reversed().any { it.handleMouseMove(mouseX, mouseY) } || isHovered
    }

    open fun handleMouseClick(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleMouseClick(mouseX, mouseY, button) }

        return when {
            childHandled -> true
            isPointInside(mouseX, mouseY) -> {
                isPressed = true
                focus()
                mouseClickListeners.any { it(mouseX, mouseY, button) } || mouseClickListeners.isEmpty()
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

        val childHandled = children.reversed().any { it.handleMouseRelease(mouseX, mouseY, button) }
        return childHandled || (wasPressed && isPointInside(mouseX, mouseY) && (mouseReleaseListeners.any { it(mouseX, mouseY, button) } || mouseReleaseListeners.isEmpty()))
    }

    open fun handleMouseScroll(mouseX: Float, mouseY: Float, horizontal: Double, vertical: Double): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleMouseScroll(mouseX, mouseY, horizontal, vertical) }
        return childHandled || (isPointInside(mouseX, mouseY) && mouseScrollListeners.any { it(mouseX, mouseY, horizontal, vertical) })
    }

    open fun handleCharType(keyCode: Int, scanCode: Int, charTyped: Char): Boolean {
        if (!visible) return false

        val childHandled = children.reversed().any { it.handleCharType(keyCode, scanCode, charTyped) }
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
        children.forEach { it.onWindowResize() }
    }

    open fun render(mouseX: Float, mouseY: Float) {
        if (!visible) return

        updateHeight()
        updateWidth()
        updateX()
        updateY()

        onRender(mouseX, mouseY)
        renderChildren(mouseX, mouseY)
    }

    protected open fun renderChildren(mouseX: Float, mouseY: Float) {
        children.forEach { it.render(mouseX, mouseY) }
    }

    protected abstract fun onRender(mouseX: Float, mouseY: Float)

    fun childOf(parent: VexelElement<*>): T = apply {
        parent.addChild(this)
    } as T

    fun childOf(parent: VexelWindow): T = apply {
        parent.addChild(this)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun addChild(child: VexelElement<*>): T = apply {
        child.parent = this
        children.add(child)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setSizing(widthType: Size, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setSizing(width: Float, widthType: Size, height: Float, heightType: Size): T = apply {
        this.widthType = widthType
        this.heightType = heightType
        if (widthType == Size.Pixels) this.width = width else this.widthPercent = width
        if (heightType == Size.Pixels) this.height = height else this.heightPercent = height
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setPositioning(xConstraint: Pos, yConstraint: Pos): T = apply {
        this.xPositionConstraint = xConstraint
        this.yPositionConstraint = yConstraint
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setPositioning(xVal: Float, xPos: Pos, yVal: Float, yPos: Pos): T {
        this.xConstraint = xVal
        this.xPositionConstraint = xPos
        this.yConstraint = yVal
        this.yPositionConstraint = yPos
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
    fun onMouseEnter(callback: (Float, Float) -> Unit): T = apply {
        mouseEnterListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseExit(callback: (Float, Float) -> Unit): T = apply {
        mouseExitListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseMove(callback: (Float, Float) -> Unit): T = apply {
        mouseMoveListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onHover(onEnter: (Float, Float) -> Unit, onExit: (Float, Float) -> Unit = { _, _ -> }): T = apply {
        onMouseEnter(onEnter)
        onMouseExit(onExit)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseClick(callback: (Float, Float, Int) -> Boolean): T = apply {
        mouseClickListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onClick(callback: (Float, Float, Int) -> Boolean): T = apply {
        onMouseClick(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseRelease(callback: (Float, Float, Int) -> Boolean): T = apply {
        mouseReleaseListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onRelease(callback: (Float, Float, Int) -> Boolean): T = apply {
        onMouseRelease(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onMouseScroll(callback: (Float, Float, Double, Double) -> Boolean): T = apply {
        mouseScrollListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onScroll(callback: (Float, Float, Double, Double) -> Boolean): T = apply {
        onMouseScroll(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onCharType(callback: (Int, Int, Char) -> Boolean): T = apply {
        charTypeListeners.add(callback)
    } as T

    @Suppress("UNCHECKED_CAST")
    fun onValueChange(callback: (Any) -> Unit): T = apply {
        this.onValueChange = callback
    } as T

    @Suppress("UNCHECKED_CAST")
    fun ignoreMouseEvents(): T = apply {
        mouseClickListeners.add { _, _, _ -> false }
        mouseReleaseListeners.add { _, _, _ -> false }
        mouseScrollListeners.add { _, _, _, _ -> false }
        mouseMoveListeners.add { _, _ -> }
        mouseEnterListeners.add { _, _ -> }
        mouseExitListeners.add { _, _ -> }
    } as T

    @Suppress("UNCHECKED_CAST")
    fun ignoreFocus(): T = apply {
        ignoreFocus = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setFloating(): T = apply {
        isFloating = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun setRequiresFocus(): T = apply {
        requiresFocus = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun show(): T = apply {
        visible = true
    } as T

    @Suppress("UNCHECKED_CAST")
    fun hide(): T = apply {
        visible = false
    } as T

    @Suppress("UNCHECKED_CAST")
    fun constrain(block: ConstraintBuilder.() -> Unit): T {
        val builder = ConstraintBuilder().apply(block)

        fun applyPosition(con: Constraint?, posSetter: (Pos) -> Unit, valSetter: (Float) -> Unit) {
            con?.let {
                when (con) {
                    is Constraint.PosConstraint -> {
                        posSetter(con.pos)
                        valSetter(con.offset)
                    }
                    is Constraint.RawPixels -> {
                        posSetter(Pos.ParentPixels)
                        valSetter(con.value)
                    }
                    is Constraint.SizeConstraint -> throw IllegalArgumentException("Cannot apply a size constraint to a position property (x or y).")
                }
            }
        }

        fun applySize(con: Constraint?, typeSetter: (Size) -> Unit, valSetter: (Float) -> Unit, percSetter: (Float) -> Unit) {
            con?.let {
                when (con) {
                    is Constraint.SizeConstraint -> {
                        typeSetter(con.size)
                        when (con.size) {
                            Size.Pixels -> valSetter(con.value)
                            Size.ParentPerc -> percSetter(con.value)
                            Size.Auto -> { /* Value is ignored for Auto size */ }
                        }
                    }
                    is Constraint.RawPixels -> {
                        typeSetter(Size.Pixels)
                        valSetter(con.value)
                    }
                    is Constraint.PosConstraint -> throw IllegalArgumentException("Cannot apply a position constraint to a size property (width or height).")
                }
            }
        }

        applyPosition(builder.x, ::xPositionConstraint::set, ::xConstraint::set)
        applyPosition(builder.y, ::yPositionConstraint::set, ::yConstraint::set)
        applySize(builder.width, ::widthType::set, ::width::set, ::widthPercent::set)
        applySize(builder.height, ::heightType::set, ::height::set, ::heightPercent::set)

        return this as T
    }

    class ConstraintBuilder {
        var x: Constraint? = null
        var y: Constraint? = null
        var width: Constraint? = null
        var height: Constraint? = null
    }

    val hovered: Boolean get() = isHovered
    val pressed: Boolean get() = isPressed
    val focused: Boolean get() = isFocused
}

// Constraint DSL helpers
val Float.px: Constraint get() = Constraint.RawPixels(this)
val Int.px: Constraint get() = Constraint.RawPixels(this.toFloat())

fun percentParent(value: Float): Constraint = Constraint.SizeConstraint(Size.ParentPerc, value)
val auto: Constraint get() = Constraint.SizeConstraint(Size.Auto, 0f)

operator fun Pos.plus(offset: Float): Constraint = Constraint.PosConstraint(this, offset)
operator fun Size.plus(offset: Float): Constraint = Constraint.SizeConstraint(this, offset)
