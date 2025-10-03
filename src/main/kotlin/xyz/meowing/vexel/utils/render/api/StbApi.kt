package xyz.meowing.vexel.utils.render.api

import java.nio.ByteBuffer

/**
 * This code was inspired by OneConfig and PolyUI's NanoVG impl.
 * Modified code, some parts of it are from OneConfig/PolyUI.
 */
interface StbApi {
    fun loadFromMemory(buffer: ByteBuffer, widthOutput: IntArray, heightOutput: IntArray, channelsOutput: IntArray, desiredChannels: Int): ByteBuffer
}