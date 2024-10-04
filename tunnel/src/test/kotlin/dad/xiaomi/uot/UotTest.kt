package dad.xiaomi.uot

import org.junit.Test

class UotTest {
    @Test
    fun testClient(){
        val client= Client(8980,"127.0.0.1",8989,"test")
        client.start()
    }
}