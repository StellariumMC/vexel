package xyz.meowing.vexel.components.base

internal class ElementCache {
    var cachedParent: VexelElement<*>? = null
    var parentCacheValid = false
    var positionCacheValid = false
    var sizeCacheValid = false
    var cachedWidth: Float = 0f
    var cachedHeight: Float = 0f
    var lastScreenWidth = 0
    var lastScreenHeight = 0

    fun invalidate() {
        parentCacheValid = false
        positionCacheValid = false
        sizeCacheValid = false
    }

    fun invalidatePosition() {
        positionCacheValid = false
    }

    fun invalidateSize() {
        sizeCacheValid = false
    }
}