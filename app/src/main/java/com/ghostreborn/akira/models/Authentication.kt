package com.ghostreborn.akira.models.kitsu

data class Authentication(
    val access_token: String,
    val created_at: Long,
    val expires_in: Long,
    val refresh_token: String
)