package xyz.meowing.vexel

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient
import xyz.meowing.vexel.events.EventBus

object Vexel : ClientModInitializer {
    val mc = MinecraftClient.getInstance()

    override fun onInitializeClient() {
        println("Vexel initialized, ${EventBus.listeners.size} listeners.")
    }
}