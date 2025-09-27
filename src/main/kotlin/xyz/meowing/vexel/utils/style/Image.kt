package xyz.meowing.vexel.utils.style

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

/**
 * Implementation adapted from Odin by odtheking
 * Original work: https://github.com/odtheking/Odin
 * Modified to support Zen
 *
 * @author Odin Contributors
 */
class Image(
    val identifier: String,
    var isSVG: Boolean = false,
    var stream: InputStream = getStream(identifier),
    private var buffer: ByteBuffer? = null
) {

    constructor(inputStream: InputStream, identifier: String) : this(identifier, false, inputStream) { stream = inputStream }

    init {
        isSVG = identifier.endsWith(".svg", true)
    }

    fun buffer(): ByteBuffer {
        if (buffer == null) {
            val bytes = stream.readBytes()
            buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes).also { it.flip() }
            stream.close()
        }
        return buffer ?: throw IllegalStateException("Image has no data")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Image) return false
        return identifier == other.identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    companion object {

        private fun getStream(path: String): InputStream {
            val trimmedPath = path.trim()
            val file = File(trimmedPath)
            return if (file.exists() && file.isFile) Files.newInputStream(file.toPath())
            else this::class.java.getResourceAsStream(trimmedPath) ?: throw FileNotFoundException(trimmedPath)
        }
    }
}