package com.eazypaytech.pos.features.activation.ui

/**
 * Represents different stages of device/terminal activation process.
 *
 * Flow:
 * - SIGN_ON → Initial registration with server
 * - KEY_EXCHANGE → Exchange of cryptographic keys
 * - KEY_CHANGE → Update/rotation of keys
 * - HAND_SHAKE → Final validation/connection establishment
 */
enum class ActivationState {
    SIGN_ON,
    KEY_EXCHANGE,
    KEY_CHANGE,
    HAND_SHAKE
}