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
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.model.AnimeItem
import com.ghostreborn.akira.R

class AnimeAdapter(
    private val animeItems: List<Anime>
): RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnimeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.anime_item, parent, false)
        return AnimeViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: AnimeViewHolder,
        position: Int
    ) {
        val adapter = AnimeItemAdapter(animeItems[position].animeList)
        holder.animeSeason.text = animeItems[position].animeSeason
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
        val animeSeason: TextView = itemView.findViewById(R.id.anime_season)
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