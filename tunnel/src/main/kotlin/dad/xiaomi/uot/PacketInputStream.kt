package dad.xiaomi.uot

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PacketInputStream(private val inputStream: InputStream) {
    fun read(): ByteArray {
        val lenByte = inputStream.readNBytes(8)
        val len = ByteBuffer.wrap(lenByte)
            .order(ByteOrder.BIG_ENDIAN) // 指定大端序
            .long
        val bodyByte = inputStream.readNBytes(len.toInt())
        return decrypt(bodyByte, len.toByte())
    }
}