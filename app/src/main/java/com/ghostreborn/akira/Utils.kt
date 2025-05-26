package com.ghostreborn.akira

import java.util.Calendar

class Utils {

    fun calculateQuarter(dec: Int): Pair<String, String> {
        val currentDate = Calendar.getInstance()
        currentDate.add(Calendar.MONTH, -(3 * (dec + 1)))

        val month = currentDate.get(Calendar.MONTH)
        val year = currentDate.get(Calendar.YEAR)
        val parsedMonth = getMonth(month % 4)
        return Pair(parsedMonth, "$year")
    }

    private fun getMonth(month: Int): String {
        return when (month) {
            1 -> "Spring"
            2 -> "Summer"
            3 -> "Fall"
            else -> "Winter"
        }
    }

}