package xyz.meowing.vexel.lwjgl.plugin

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

/**
 * Implementation adapted from Odin by odtheking
 * Original work: https://github.com/odtheking/Odin
 * Modified to support Zen
 *
 * @author Odin Contributors
 */
class LWJGLClassTransformer : IClassTransformer {
    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (name == "org.lwjgl.nanovg.NanoVGGLConfig" && basicClass != null) {
            val reader = ClassReader(basicClass)
            val node = ClassNode()
            reader.accept(node, ClassReader.EXPAND_FRAMES)

            for (method in node.methods) {
                if (method.name == "configGL") {
                    val list = InsnList()

                    list.add(VarInsnNode(Opcodes.LLOAD, 0))
                    list.add(TypeInsnNode(Opcodes.NEW, "me/odin/lwjgl/LWJGLFunctionProvider"))
                    list.add(InsnNode(Opcodes.DUP))
                    list.add(
                        MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            "me/odin/lwjgl/LWJGLFunctionProvider",
                            "<init>",
                            "()V",
                            false
                        )
                    )
                    list.add(
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "org/lwjgl/nanovg/NanoVGGLConfig",
                            "config",
                            "(JLorg/lwjgl/system/FunctionProvider;)V",
                            false
                        )
                    )
                    list.add(InsnNode(Opcodes.RETURN))

                    method.instructions.clear()
                    method.instructions.insert(list)
                }
            }

            val cw = ClassWriter(reader, ClassWriter.COMPUTE_FRAMES)
            node.accept(cw)
            return cw.toByteArray()
        }

        return basicClass
    }
}