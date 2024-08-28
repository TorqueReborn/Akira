package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.R
import com.ghostreborn.akira.model.Anime

class CommonAdapter (private val animes: ArrayList<Anime>, private val isManga:Boolean) :
    RecyclerView.Adapter<CommonAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImageView: ImageView = itemView.findViewById(R.id.anime_image)
        val animeNameTextView: TextView = itemView.findViewById(R.id.anime_name)
        val animeProgressTextView: TextView = itemView.findViewById(R.id.anime_progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anime_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return animes.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = animes[position]
        var progress = "Watched ${anime.progress} episodes"
        if (isManga) progress = "Read ${anime.progress} chapters"
        holder.animeNameTextView.text = anime.title
        holder.animeImageView.load(anime.thumbnail)
        holder.animeProgressTextView.text = progress
    }

}