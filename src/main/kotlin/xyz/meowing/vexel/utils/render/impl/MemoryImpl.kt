package xyz.meowing.vexel.utils.render.impl

import org.lwjgl.system.MemoryUtil
import xyz.meowing.vexel.utils.render.api.MemoryApi
import java.nio.ByteBuffer

class MemoryImpl : MemoryApi {
    override fun memAlloc(size: Int): ByteBuffer = MemoryUtil.memAlloc(size)
    override fun memFree(buffer: ByteBuffer) = MemoryUtil.memFree(buffer)
}