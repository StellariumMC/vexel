package xyz.meowing.vexel.events

//#if FABRIC
import net.minecraft.client.gui.DrawContext
//#endif

import xyz.meowing.knit.api.events.Event

abstract class GuiEvent : Event() {
    class Render(
        //#if FABRIC
        val context: DrawContext
        //#endif
    ): GuiEvent()
}