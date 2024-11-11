package com.analogics.securityframework.handler

import java.security.KeyPair
import java.security.KeyPairGenerator

object SecureKeyHandler {
    fun generateRsaKey(): KeyPair {
        var keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
        return keyPair
    }
}