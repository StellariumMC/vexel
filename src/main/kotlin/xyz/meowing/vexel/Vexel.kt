package xyz.meowing.vexel

import dev.deftu.lwjgl.isolatedloader.Lwjgl3Manager
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GLContext
import xyz.meowing.vexel.utils.render.api.MemoryApi
import xyz.meowing.vexel.utils.render.api.NanoVgApi
import xyz.meowing.vexel.utils.render.api.RenderApi
import xyz.meowing.vexel.utils.render.api.StbApi
import xyz.meowing.vexel.utils.render.impl.*

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
            val odinLoaderClass = Class.forName("me.odin.lwjgl.Lwjgl3Loader")
            val loadMethod = odinLoaderClass.getDeclaredMethod("load")
            val odinWrapper = loadMethod.invoke(null) as me.odin.lwjgl.Lwjgl3Wrapper

            val nanoVg = OdinNanoVgAdapter(odinWrapper)
            val stb = OdinStbAdapter(odinWrapper)
            val memory = OdinMemoryAdapter(odinWrapper)

            nanoVg.maybeSetup()
            renderEngineInstance = NVGRendererImpl(nanoVg, stb, memory, true)
            return
        } catch (_: ClassNotFoundException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            Lwjgl3Manager.initialize(this::class.java.classLoader, arrayOf("nanovg", "stb"))
            Lwjgl3Manager.getClassLoader().addLoadingException(API_PACKAGE)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to initialize LWJGL3 Manager", e)
        }
    }

    fun initLate() {
        if (this::renderEngineInstance.isInitialized) return

        try {
            val nanoVg = Lwjgl3Manager.getIsolated(NanoVgApi::class.java, NANOVG_IMPL, GLContext.getCapabilities().OpenGL30)
            val stb = Lwjgl3Manager.getIsolated(StbApi::class.java, STB_IMPL)
            val memory = Lwjgl3Manager.getIsolated(MemoryApi::class.java, MEMORY_IMPL)

            renderEngineInstance = NVGRendererImpl(nanoVg, stb, memory, false)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to create render engine", e)
        }
    }
}