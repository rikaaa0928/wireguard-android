package dad.xiaomi.uot

import android.net.TrafficStats
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference

class Client(val srcPort: Int, val dstHost: String, val dstPort: Int, val pw: String) {
    var stop = AtomicBoolean(false)
    val bufferSize = 65535
    var lastAddr = ""
    var lastPort = 0
    var socket: DatagramSocket? = null
    val CONN_TAG = 2809

    fun start(): Socket {
        stop.set(false);
        TrafficStats.setThreadStatsTag(CONN_TAG);
        //udp监听srcPort并使用65535的buffer读取数据
        socket = DatagramSocket(srcPort)
        val buffer = ByteArray(bufferSize)
        val packet = DatagramPacket(buffer, bufferSize)
        var tcpSocket = Socket(dstHost, dstPort)

        // 获取输入输出流
        var inputStream = AtomicReference(PacketInputStream(tcpSocket.getInputStream()))
        var outputStream = PacketOutputStream(tcpSocket.getOutputStream())
        // 握手
        handShake(outputStream, pw)
        GlobalScope.launch {
            while (!stop.get()) {
                try {
                    val readed = inputStream.get().read()
                    val packet = DatagramPacket(readed, readed.size)
                    packet.address = InetAddress.getByName(lastAddr)
                    packet.port = lastPort
                    socket!!.send(packet)
                } catch (e: Exception) {
                    Log.e("uot Client", "GlobalScope", e)
                }
            }
            Log.d("uot Client","tcp read loop exit")
            tcpSocket.close()
        }
        GlobalScope.launch {
            while (!stop.get()) {
                try {
                    socket!!.receive(packet)
                    if (lastAddr.isEmpty()) {
                        lastAddr = packet.address.hostName;
                        lastPort = packet.port
                    } else if (lastAddr != packet.address.hostName || lastPort != packet.port) {
//                    tcpSocket.close()
//                    tcpSocket = Socket(dstHost, dstPort)
//                    // 获取输入输出流
//                    inputStream.set(PacketInputStream(tcpSocket.getInputStream()))
//                    outputStream = PacketOutputStream(tcpSocket.getOutputStream())
                        Log.e("uot Client", "udp id (ip or port) changed")
                        lastAddr = packet.address.hostName;
                        lastPort = packet.port
                    }
                    val receivedData = packet.data.copyOfRange(0, packet.length)
                    val str = String(receivedData)
                    Log.i("uot Client receive", str)
                    outputStream.write(receivedData)
                } catch (e: Exception) {
                    Log.e("uot Client", "main while", e)
//                throw e;
                }
            }
            Log.d("uot Client","tcp write loop exit")
            tcpSocket.close()
        }
        return tcpSocket
    }

    fun stop() {
        if (socket != null) {
            socket!!.close()
        }
        stop.set(true)
        TrafficStats.clearThreadStatsTag();
    }
}