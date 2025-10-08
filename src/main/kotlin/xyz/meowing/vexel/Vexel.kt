package xyz.meowing.vexel

import dev.deftu.lwjgl.isolatedloader.Lwjgl3Manager
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GLContext
import xyz.meowing.vexel.utils.render.api.MemoryApi
import xyz.meowing.vexel.utils.render.api.NanoVgApi
import xyz.meowing.vexel.utils.render.api.RenderApi
import xyz.meowing.vexel.utils.render.api.StbApi
import xyz.meowing.vexel.utils.render.impl.NVGRendererImpl

object Vexel {
    val client: Minecraft = Minecraft.getMinecraft()

    private const val API_PACKAGE = "xyz.meowing.vexel.utils.render.api."
    private const val NANOVG_IMPL = "xyz.meowing.vexel.utils.render.impl.NanoVgImpl"
    private const val STB_IMPL = "xyz.meowing.vexel.utils.render.impl.StbImpl"
    private const val MEMORY_IMPL = "xyz.meowing.vexel.utils.render.impl.MemoryImpl"

    private lateinit var renderEngineInstance: RenderApi

    val renderEngine: RenderApi get() = renderEngineInstance

    fun init() {
        initEarly()
        initLate()
    }

    fun initEarly() {
        try {
            Lwjgl3Manager.initialize(this::class.java.classLoader, arrayOf("nanovg", "stb"))
            Lwjgl3Manager.getClassLoader().addLoadingException(API_PACKAGE)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to initialize LWJGL3 Manager", e)
        }
    }

    fun initLate() {
        try {
            val nanoVg = Lwjgl3Manager.getIsolated(NanoVgApi::class.java, NANOVG_IMPL, GLContext.getCapabilities().OpenGL30)
            val stb = Lwjgl3Manager.getIsolated(StbApi::class.java, STB_IMPL)
            val memory = Lwjgl3Manager.getIsolated(MemoryApi::class.java, MEMORY_IMPL)

            renderEngineInstance = NVGRendererImpl(nanoVg, stb, memory)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to create render engine", e)
        }
    }
}