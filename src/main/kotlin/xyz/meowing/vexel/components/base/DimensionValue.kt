package xyz.meowing.vexel.components.base

import xyz.meowing.vexel.components.core.Container
import xyz.meowing.vexel.components.core.Rectangle

sealed class DimensionValue {
    abstract fun resolve(element: VexelElement<*>, isWidth: Boolean): Float
    
    data class Pixels(val value: Float) : DimensionValue() {
        override fun resolve(element: VexelElement<*>, isWidth: Boolean): Float = value
    }
    
    data class Percent(val value: Float) : DimensionValue() {
        override fun resolve(element: VexelElement<*>, isWidth: Boolean): Float {
            val parentElement = element.findFirstVisibleParent()
            return if (parentElement != null) {
                val padding = when (parentElement) {
                    is Rectangle -> parentElement.padding
                    is Container -> parentElement.padding
                    else -> floatArrayOf(0f, 0f, 0f, 0f)
                }
                if (isWidth) {
                    val availableWidth = parentElement.width - (padding[1] + padding[3])
                    availableWidth * (value / 100f)
                } else {
                    val availableHeight = parentElement.height - (padding[0] + padding[2])
                    availableHeight * (value / 100f)
                }
            } else {
                if (isWidth) {
                    element.screenWidth * (value / 100f)
                } else {
                    element.screenHeight * (value / 100f)
                }
            }
        }
    }
    
    data class Minus(val left: DimensionValue, val right: DimensionValue) : DimensionValue() {
        override fun resolve(element: VexelElement<*>, isWidth: Boolean): Float {
            return left.resolve(element, isWidth) - right.resolve(element, isWidth)
        }
    }
    
    data class Plus(val left: DimensionValue, val right: DimensionValue) : DimensionValue() {
        override fun resolve(element: VexelElement<*>, isWidth: Boolean): Float {
            return left.resolve(element, isWidth) + right.resolve(element, isWidth)
        }
    }
    
    operator fun minus(other: DimensionValue): DimensionValue = Minus(this, other)
    operator fun plus(other: DimensionValue): DimensionValue = Plus(this, other)
}