package com.ghostreborn.akirareborn.allanime

import org.json.JSONObject

class AllAnimeParser {

    fun scrapeQueryPopular(){
        val recommendationsArray = JSONObject(AllAnimeNetwork().queryPopular())
            .getJSONObject("data")
            .getJSONObject("queryPopular")
            .getJSONArray("recommendations")
        for (i in 0 until recommendationsArray.length()){
            val recommendation = recommendationsArray.getJSONObject(i)
                .getJSONObject("anyCard")
            val id = recommendation.getString("_id")
            val name = recommendation.getString("name")
            val englishName = recommendation.getString("englishName")
            val thumbnail = recommendation.getString("thumbnail")
        }
    }

}