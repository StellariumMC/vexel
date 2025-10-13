package xyz.meowing.vexel.utils.render.impl.odin

import me.odin.lwjgl.Lwjgl3Wrapper
import xyz.meowing.vexel.utils.render.api.MemoryApi
import java.nio.ByteBuffer

class OdinMemoryAdapter(private val wrapper: Lwjgl3Wrapper) : MemoryApi {
    override fun memAlloc(size: Int): ByteBuffer = wrapper.memAlloc(size)
    
    override fun memFree(buffer: ByteBuffer) {}
}