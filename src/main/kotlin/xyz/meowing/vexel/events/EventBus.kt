package xyz.meowing.vexel.events

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import org.lwjgl.glfw.GLFW
import xyz.meowing.vexel.Vexel.mc
import java.util.concurrent.ConcurrentHashMap

object EventBus {
    val listeners = ConcurrentHashMap<Class<*>, MutableSet<PrioritizedCallback<*>>>()
    data class PrioritizedCallback<T>(val priority: Int, val callback: (T) -> Unit)

    init {
        ScreenEvents.BEFORE_INIT.register { _, screen, _, _ ->
            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, scancode, modifiers ->
                val charTyped = GLFW.glfwGetKeyName(key, scancode)?.firstOrNull() ?: '\u0000'
                !post(GuiEvent.Key(GLFW.glfwGetKeyName(key, scancode), key, charTyped, scancode, screen))
            }

            GLFW.glfwSetCharCallback(mc.window.handle) { window, codepoint ->
                val charTyped = codepoint.toChar()

                !post(GuiEvent.Key(null, GLFW.GLFW_KEY_UNKNOWN, charTyped, 0, screen))
            }
        }
    }

    inline fun <reified T : Event> register(priority: Int = 0, noinline callback: (T) -> Unit, add: Boolean = true): EventCall {
        val eventClass = T::class.java
        val handlers = listeners.getOrPut(eventClass) { ConcurrentHashMap.newKeySet() }
        val prioritizedCallback = PrioritizedCallback(priority, callback)
        if (add) handlers.add(prioritizedCallback)
        return EventCallImpl(prioritizedCallback, handlers)
    }

    fun <T : Event> post(event: T): Boolean {
        val eventClass = event::class.java
        val handlers = listeners[eventClass] ?: return false
        if (handlers.isEmpty()) return false

        val sortedHandlers = handlers.sortedBy { it.priority }

        for (handler in sortedHandlers) {
            try {
                @Suppress("UNCHECKED_CAST")
                (handler.callback as (T) -> Unit)(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (event is CancellableEvent) event.isCancelled() else false
    }

    class EventCallImpl(
        private val callback: PrioritizedCallback<*>,
        private val handlers: MutableSet<PrioritizedCallback<*>>
    ) : EventCall {
        override fun unregister(): Boolean = handlers.remove(callback)
        override fun register(): Boolean = handlers.add(callback)
    }

    interface EventCall {
        fun unregister(): Boolean
        fun register(): Boolean
    }
}