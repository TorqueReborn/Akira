package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.AnimeItem
import com.ghostreborn.akira.R
import com.ghostreborn.akira.ui.SeasonalActivity
import com.ghostreborn.akira.Utils
import com.ghostreborn.akira.allAnime.AnimeSeason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeasonalAdapter(
    private val animeItems: ArrayList<Anime>
) : RecyclerView.Adapter<SeasonalAdapter.AnimeViewHolder>() {

    fun addItem(anime: Anime) {
        animeItems.add(anime)
        notifyItemInserted(animeItems.size - 1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnimeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.anime_item, parent, false)
        itemView.findViewById<TextView>(R.id.anime_season).visibility = View.GONE
        return AnimeViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: AnimeViewHolder,
        position: Int
    ) {
        if (position == itemCount - 1) {
            val season = Utils().calculateQuarter(MainActivity.count)
            CoroutineScope(Dispatchers.IO).launch {
                val anime = AnimeSeason().animeBySeasonYear(season.first, season.second,
                    SeasonalActivity.page)
                SeasonalActivity.page++
                withContext(Dispatchers.Main) {
                    if(anime.animeList.isNotEmpty()) {
                        addItem(anime)
                    }
                }
            }
        }
        val adapter = AnimeItemAdapter(animeItems[position].animeList)
        holder.animeRecycler.adapter = adapter
        holder.animeRecycler.layoutManager = GridLayoutManager(
            holder.animeRecycler.context,
            3,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun getItemCount(): Int {
        return animeItems.size
    }

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeRecycler: RecyclerView = itemView.findViewById(R.id.anime_recycler)
    }

    class AnimeItemAdapter(
        private val animeList: List<AnimeItem>
    ) : RecyclerView.Adapter<AnimeItemAdapter.AnimeViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AnimeViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.anime_list, parent, false)
            return AnimeViewHolder(itemView)
        }

        override fun onBindViewHolder(
            holder: AnimeViewHolder,
            position: Int
        ) {
            val animeItem = animeList[position]
            holder.animeImage.load(animeItem.thumbnail)
        }

        override fun getItemCount(): Int {
            return animeList.size
        }

        class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val animeImage: ImageView = itemView.findViewById(R.id.anime_image)
        }
    }
}