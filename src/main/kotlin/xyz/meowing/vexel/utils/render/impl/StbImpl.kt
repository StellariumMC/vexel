package xyz.meowing.vexel.utils.render.impl

import org.lwjgl.stb.STBImage
import xyz.meowing.vexel.utils.render.api.StbApi
import java.nio.ByteBuffer

class StbImpl : StbApi {
    override fun loadFromMemory(
        buffer: ByteBuffer,
        widthOutput: IntArray,
        heightOutput: IntArray,
        channelsOutput: IntArray,
        desiredChannels: Int
    ): ByteBuffer {
        return STBImage.stbi_load_from_memory(buffer, widthOutput, heightOutput, channelsOutput, desiredChannels) ?: throw IllegalStateException("Failed to load image from memory")
    }
}