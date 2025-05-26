package com.ghostreborn.akira

class Utils {

    fun calculateQuarter(month: Int, year: Int): Pair<String, String> {
        var m = month - 3
        var y = year
        if(m <= 0) {
            m = 12
            y -= 1
        }
        var quarter = (m / 3) + 1
        return Pair(getMonth(quarter), year.toString())
    }

    private fun getMonth(month: Int): String {
        return when(month) {
            1 -> "Spring"
            2 -> "Summer"
            3 -> "Fall"
            else -> "Winter"
        }
    }

}