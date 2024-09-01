package com.ghostreborn.akira.models

data class Authentication(
    val access_token: String,
    val refresh_token: String
)
