package dad.xiaomi.uot

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class Client(val srcPort: Int, val dstHost: String, val dstPort: Int, val pw: String) {
    var stop = AtomicBoolean(false)
    val bufferSize = 65535
    var lastAddr = ""
    fun start(): Int {
        stop.set(false);
        //udp监听srcPort并使用65535的buffer读取数据
        val socket = DatagramSocket(srcPort)
        val buffer = ByteArray(bufferSize)
        val packet = DatagramPacket(buffer, bufferSize)
        var tcpSocket = Socket(dstHost, dstPort)

        // 获取输入输出流
        var inputStream = AtomicReference(PacketInputStream(tcpSocket.getInputStream()))
        var outputStream = PacketOutputStream(tcpSocket.getOutputStream())
        while (!stop.get()) {
            try {
                socket.receive(packet)
                if (lastAddr.isEmpty()) {
                    lastAddr = packet.address.hostName;
                } else if (lastAddr != packet.address.hostName) {
                    tcpSocket.close()
                    tcpSocket = Socket(dstHost, dstPort)
                    // 获取输入输出流
                    inputStream.set(PacketInputStream(tcpSocket.getInputStream()))
                    outputStream = PacketOutputStream(tcpSocket.getOutputStream())
                }
                GlobalScope.launch {
                    while (!stop.get()) {
                        val readed = inputStream.get().read()
                        socket.send(D)
                    }
                }
                val receivedData = packet.data.copyOfRange(0, packet.length)
                Log.i("uot Client receive", String(receivedData))
            } catch (e: Exception) {
                Log.e("uot Client", "main while", e)
                tcpSocket.close()
                tcpSocket = Socket(dstHost, dstPort)
                // 获取输入输出流
                inputStream.set(PacketInputStream(tcpSocket.getInputStream()))
                outputStream = PacketOutputStream(tcpSocket.getOutputStream())
            }
        }
        return 0
    }

    fun stop() {
        stop.set(true)
    }
}