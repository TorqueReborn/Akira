package com.ghostreborn.akira

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class AnimeListAdapter(
    private val animeImageList: List<String>
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
        val animeImage = animeImageList[position]
        holder.animeImage.load(animeImage)
    }

    override fun getItemCount(): Int {
        return animeImageList.size
    }

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImage = itemView.findViewById<ImageView>(R.id.anime_image)
    }

}