package xyz.meowing.vexel.utils.render.impl

import org.lwjgl.stb.STBImage
import xyz.meowing.vexel.utils.render.api.StbApi
import java.nio.ByteBuffer

/**
 * This code was inspired by OneConfig and PolyUI's NanoVG impl.
 * Modified code, some parts of it are from OneConfig/PolyUI.
 */
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