package xyz.meowing.vexel.events

import xyz.meowing.knit.api.events.Event

//#if MC >= 1.21.5 && FABRIC
import net.minecraft.client.gui.DrawContext
//#endif

abstract class GuiEvent : Event() {
    class Render(
        //#if MC >= 1.21.5 && FABRIC
        val context: DrawContext
        //#endif
    ) : GuiEvent()
}