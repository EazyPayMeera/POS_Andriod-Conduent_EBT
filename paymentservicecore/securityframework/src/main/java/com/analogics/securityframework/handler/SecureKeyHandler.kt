package com.analogics.securityframework.handler

import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object SecureKeyHandler {
    fun generateRsaKey(): KeyPair {
        var generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
        var keyPair = generator.generateKeyPair()
        return keyPair
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decryptRSA(encryptedData: String?, privateKeyPEM: String?): String {
        // Remove the first and last lines of the private key and decode it from Base64
        val privateKeyPEMFormatted = privateKeyPEM
            ?.replace("-----BEGIN PRIVATE KEY-----", "")
            ?.replace("-----END PRIVATE KEY-----", "")
            ?.replace("\\s".toRegex(), "")

        val keyBytes = Base64.decode(privateKeyPEMFormatted?:"")

        // Convert the decoded bytes to a PrivateKey object
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(keySpec)

        // Decrypt the data
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData?:""))

        return String(decryptedBytes, Charsets.UTF_8)
    }
}