package com.yigit033.matematikmacarasi.models

import kotlinx.serialization.Serializable

/**
 * Oturum bilgilerini tutan model
 */
@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userID: String
) 