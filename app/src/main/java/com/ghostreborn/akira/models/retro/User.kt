package com.ghostreborn.akira.models.retro

data class User(
    val data: List<Data>
){
    data class Data(
        val id: String
    )
}
