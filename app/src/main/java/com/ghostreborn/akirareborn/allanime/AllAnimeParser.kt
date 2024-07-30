package com.ghostreborn.akirareborn.allanime

import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.model.Anime
import org.json.JSONObject

class AllAnimeParser {

    fun queryPopular(){
        Constants.animeList = ArrayList()
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
            Constants.animeList.add(Anime(id, name, englishName, thumbnail))
        }
    }

}