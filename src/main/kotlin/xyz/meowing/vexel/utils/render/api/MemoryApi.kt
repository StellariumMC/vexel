package xyz.meowing.vexel.utils.render.api

import java.nio.ByteBuffer

interface MemoryApi {
    fun memAlloc(size: Int): ByteBuffer
    fun memFree(buffer: ByteBuffer)
}