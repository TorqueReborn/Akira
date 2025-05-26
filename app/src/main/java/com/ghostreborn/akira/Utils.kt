package com.ghostreborn.akira

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

}