package xyz.meowing.vexel.utils.render.impl

import me.odin.lwjgl.Lwjgl3Wrapper
import xyz.meowing.vexel.utils.render.api.StbApi
import java.nio.ByteBuffer

class OdinStbAdapter(private val wrapper: Lwjgl3Wrapper) : StbApi {
    override fun loadFromMemory(buffer: ByteBuffer, widthOutput: IntArray, heightOutput: IntArray, channelsOutput: IntArray, desiredChannels: Int): ByteBuffer {
        return wrapper.stbi_load_from_memory(buffer, widthOutput, heightOutput, channelsOutput, desiredChannels)
            ?: throw IllegalStateException("Failed to load image from memory")
    }
}