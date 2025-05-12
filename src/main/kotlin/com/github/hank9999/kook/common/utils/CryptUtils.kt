package com.github.hank9999.kook.common.utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.util.Base64

object CryptUtils {
    /**
     * 使用 AES/CBC/PKCS5Padding 解密给定的加密数据。
     *
     * @param data 要解密的 Base64 编码内容，包含 IV 和密文。
     * @param key 用于解密的密钥，如果长度不足 32 字节，将以空字符填充。
     * @return 返回解密后的明文字符串。
     * @throws RuntimeException 如果解密过程中出现错误，则抛出异常。
     */
    fun decrypt(data: String, key: String): String {
        // Base64 解码
        val decodedDataBytes = Base64.getDecoder().decode(data)
        val src = decodedDataBytes.decodeToString()

        // 截取 IV
        val ivString = src.substring(0, 16)

        val ciphertextBase64 = src.substring(16)
        // 待解密的密文
        val encryptedBytes = Base64.getDecoder().decode(ciphertextBase64)

        // Padding
        val finalKeyString = key.padEnd(32, '\u0000')

        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(finalKeyString.toByteArray(StandardCharsets.UTF_8), "AES")
            val ivParameterSpec = IvParameterSpec(ivString.toByteArray(StandardCharsets.UTF_8))
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                ivParameterSpec
            )
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return decryptedBytes.decodeToString()

        } catch (e: Exception) {
            throw RuntimeException("Decryption failed. Details: ${e.message}", e)
        }
    }
}