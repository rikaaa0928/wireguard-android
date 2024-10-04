package dad.xiaomi.uot

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PacketOutputStream(private val outputStream: OutputStream) {
    fun write(data: ByteArray): Int {
        val len = data.size
        val lenByte = ByteBuffer.allocate(8)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(len.toLong())
            .array()
        outputStream.write(lenByte)
        val payload = encrypt(data, len.toByte())
        outputStream.write(payload)
        return len;
    }

    fun close() {
        outputStream.close()
    }
}