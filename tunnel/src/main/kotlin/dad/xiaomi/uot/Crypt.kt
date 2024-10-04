package dad.xiaomi.uot

import com.google.gson.Gson
import java.security.SecureRandom
import kotlin.random.Random

fun handShake(stream: PacketOutputStream, pw: String) {
    val auth_info = generateAuth(pw);
    stream.write(auth_info.toByteArray(charset = Charsets.UTF_8));
}

data class Auth(
    val pw: String,
    val rand: String
)

fun generateAuth(pw: String): String {
    val rand = generateRandomString(255)
    val auth = Auth(pw, rand)
    return Gson().toJson(auth)
}

fun parseAuth(data: String): Auth {
    return try {
        Gson().fromJson(data, Auth::class.java)
    } catch (e: Exception) {
        throw Error("Failed to parse auth data")
    }
}

fun encrypt(data: ByteArray, key: Byte): ByteArray {
    val actualKey = if (key == 0.toByte()) 128.toByte() else key
    return data.map { it.plus(actualKey).toByte() }.toByteArray()
}

fun decrypt(data: ByteArray, key: Byte): ByteArray {
    val actualKey = if (key == 0.toByte()) 128.toByte() else key
    return data.map { it.minus(actualKey).toByte() }.toByteArray()
}

fun generateRandomString(maxLength: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val length = Random.nextInt(1, maxLength + 1)
    return (1..length)
        .map { SecureRandom().nextInt(charPool.size) }
        .map(charPool::get)
        .joinToString("")
}