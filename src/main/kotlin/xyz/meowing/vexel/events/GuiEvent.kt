package xyz.meowing.vexel.events

import xyz.meowing.knit.api.events.Event

abstract class GuiEvent : Event() {
    class Render : GuiEvent()
}