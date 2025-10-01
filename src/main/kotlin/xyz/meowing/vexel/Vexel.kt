package xyz.meowing.vexel

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.MinecraftClient

object Vexel : ClientModInitializer {
    val mc = MinecraftClient.getInstance()

    override fun onInitializeClient() {
        println("Vexel initialized, No listeners.")
    }
}