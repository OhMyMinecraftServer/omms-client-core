package icu.takeneko.omms.client.util

import java.io.BufferedReader
import java.io.IOException
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

class EncryptedConnector(val `in`: BufferedReader, val out: PrintWriter, key: String) {
    val key: ByteArray = key.toByteArray(StandardCharsets.UTF_8)

    @Throws(
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        InvalidKeyException::class
    )
    fun println(content: String) {
        this.send(content)
    }

    @Throws(
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        InvalidKeyException::class
    )
    fun send(content: String) {
        val data = encryptECB(
            content.toByteArray(StandardCharsets.UTF_8),
            this.key
        )
        out.println(String(data, StandardCharsets.UTF_8))
        out.flush()
    }

    @Throws(
        IOException::class,
        NoSuchPaddingException::class,
        IllegalBlockSizeException::class,
        NoSuchAlgorithmException::class,
        BadPaddingException::class,
        InvalidKeyException::class
    )
    fun readLine(): String? {
        val line = `in`.readLine() ?: return null
        val data = decryptECB(
            line.toByteArray(StandardCharsets.UTF_8),
            this.key
        )
        return String(data, StandardCharsets.UTF_8)
    }

    companion object {
        @Throws(
            NoSuchPaddingException::class,
            NoSuchAlgorithmException::class,
            InvalidKeyException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class
        )
        private fun encryptECB(data: ByteArray, key: ByteArray): ByteArray {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
            val result = cipher.doFinal(data)
            return Base64.getEncoder().encode(result)
        }

        @Throws(
            NoSuchPaddingException::class,
            NoSuchAlgorithmException::class,
            InvalidKeyException::class,
            BadPaddingException::class,
            IllegalBlockSizeException::class
        )
        private fun decryptECB(data: ByteArray, key: ByteArray): ByteArray {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"))
            val base64 = Base64.getDecoder().decode(data)
            return cipher.doFinal(base64)
        }
    }
}
