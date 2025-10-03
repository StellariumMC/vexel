package xyz.meowing.vexel.utils.render.api

import java.nio.ByteBuffer

interface StbApi {
    fun loadFromMemory(buffer: ByteBuffer, widthOutput: IntArray, heightOutput: IntArray, channelsOutput: IntArray, desiredChannels: Int): ByteBuffer
}