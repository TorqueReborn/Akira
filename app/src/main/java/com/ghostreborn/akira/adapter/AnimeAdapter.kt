package com.ghostreborn.akira.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.models.kitsu.Anime

class AnimeAdapter (private val animes: ArrayList<Anime>
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.anime_image)
        val name: TextView = itemView.findViewById(R.id.anime_name)
        val progress: TextView = itemView.findViewById(R.id.anime_progress)
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
        val progress = "Watched ${anime.progress}/${anime.episodeCount} Episodes"
        holder.name.text = anime.name
        holder.progress.text = progress
        holder.thumbnail.load(anime.thumbnail)
        holder.itemView.setOnClickListener {
            Constants.animeID = anime.id
            holder.itemView.context.startActivity(Intent(holder.itemView.context, holder.itemView.context::class.java))
        }
    }

}