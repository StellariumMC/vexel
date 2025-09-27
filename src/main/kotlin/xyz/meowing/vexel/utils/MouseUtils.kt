package xyz.meowing.vexel.utils

import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import xyz.meowing.vexel.Vexel.mc

object MouseUtils {
    inline val sr get() = ScaledResolution(mc)

    inline val rawX: Int get() = Mouse.getX()
    inline val rawY: Int get() = mc.displayHeight - Mouse.getY() - 1

    inline val scaledX: Float get() = (Mouse.getX() * sr.scaledWidth / mc.displayWidth).toFloat()
    inline val scaledY: Float get() = (sr.scaledHeight - Mouse.getY() * sr.scaledHeight / mc.displayHeight).toFloat()
}