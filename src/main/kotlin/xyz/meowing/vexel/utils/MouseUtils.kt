package xyz.meowing.vexel.utils

import xyz.meowing.vexel.Vexel.mc

object MouseUtils {
    inline val window get() = mc.window

    inline val rawX: Double get() = mc.mouse.x
    inline val rawY: Double get() = mc.mouse.y

    inline val scaledX: Double get() = mc.mouse.x * window.scaledWidth / window.width
    inline val scaledY: Double get() = mc.mouse.y * window.scaledWidth / window.width
}