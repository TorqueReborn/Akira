package com.ghostreborn.akira.models.kitsu

data class User(
    val data: List<Data>
){
    data class Data(
        val id: String,
        val attributes: Attributes
    ) {
        data class Attributes(
            val name: String
        )
    }
}