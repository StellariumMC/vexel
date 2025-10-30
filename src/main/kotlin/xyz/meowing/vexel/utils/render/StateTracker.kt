package xyz.meowing.vexel.utils.render

object StateTracker {
    @JvmStatic
    var previousBoundTexture = -1

    @JvmStatic
    var previousActiveTexture = -1

    @JvmStatic
    var previousProgram = -1

    @JvmStatic
    var drawing = false
}