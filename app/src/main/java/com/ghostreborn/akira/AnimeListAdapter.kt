package com.ghostreborn.akira

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class AnimeListAdapter(
    private val animeList: List<Anime>
) : RecyclerView.Adapter<AnimeListAdapter.AnimeViewHolder>() {
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
        val anime = animeList[position]
        holder.animeImage.load(anime.thumbnail)
    }

    override fun getItemCount(): Int {
        return animeList.size
    }

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImage = itemView.findViewById<ImageView>(R.id.anime_image)
    }

}