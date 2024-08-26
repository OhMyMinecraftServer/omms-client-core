package icu.takeneko.omms.client.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

@Suppress("Unused")
object Util {
    @JvmStatic
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @OptIn(ExperimentalEncodingApi::class)
    @JvmStatic
    fun base64Encode(content: String): String =
        Base64.encode(content.toByteArray(Charsets.UTF_8))

    @JvmStatic
    fun randomStringGen(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789"
        var result = ""
        for (i in 1..length) {
            val rng = Random(System.nanoTime())
            result += chars[rng.nextInt(chars.length)]
        }
        return result
    }

    @JvmStatic
    fun joinToString(strings: Collection<String>): String {
        val list = strings.toList()
        var result = "["
        for (i in list.indices) {
            if (i == list.lastIndex) {
                result += list[i]
                continue
            }
            result += "${list[i]} , "
        }
        result += "]"
        return result
    }

    @OptIn(ExperimentalEncodingApi::class)
    @JvmStatic
    fun getChecksumMD5(original: String): String =
        Base64.encode(
            MessageDigest.getInstance("SHA-256")
                .digest(original.toByteArray(Charsets.UTF_8))
        )
}