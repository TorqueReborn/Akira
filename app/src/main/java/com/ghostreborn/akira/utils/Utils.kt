package com.ghostreborn.akira.utils

import com.ghostreborn.akira.data.model.Anime
import java.util.Calendar

class Utils {

    fun calculateQuarter(dec: Int): Pair<String, String> {
        val currentDate = Calendar.getInstance()
        currentDate.add(Calendar.MONTH, -(3 * dec))

        val month = currentDate.get(Calendar.MONTH)
        val year = currentDate.get(Calendar.YEAR)

        val parsedMonth = getMonth(month)
        return Pair(parsedMonth, "$year")
    }

    private fun getMonth(month: Int): String {
        return when (month) {
            1, 2, 12 -> "Winter"
            3, 4, 5 -> "Spring"
            6, 7, 8 -> "Summer"
            9, 10, 11 -> "Fall"
            else -> "Winter"
        }
    }

    fun decrypt(decrypt: String): String {
        val decryptedString = StringBuilder()
        var i = 2
        while (i < decrypt.length) {
            decryptedString.append((decrypt.substring(i, i + 2).toInt(16) xor 56).toChar())
            i += 2
        }
        return decryptedString.toString()
    }

    fun getSampleAnimeList(): List<Anime> {
        return List(20) { index ->
            Anime(
                id = index,
                name = "Anime ${index + 1}",
                thumbnail = "https://placehold.co/150x220/png?text=Anime+${index + 1}"
            )
        }
    }

}