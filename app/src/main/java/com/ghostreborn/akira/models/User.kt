package com.ghostreborn.kitsumodified.models

data class User(
    val data: List<Data>
){
    data class Data(
        val id: String
    )
}
