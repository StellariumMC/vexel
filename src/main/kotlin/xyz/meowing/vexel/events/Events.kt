package xyz.meowing.vexel.events

import net.minecraft.client.gui.screen.Screen

abstract class Event

abstract class CancellableEvent : Event() {
    private var cancelled = false
    fun cancel() { cancelled = true }
    fun isCancelled() = cancelled
}

abstract class GuiEvent {
    class Key(val keyName: String?, val key: Int, val character: Char, val scanCode: Int, val screen: Screen) : CancellableEvent()
}