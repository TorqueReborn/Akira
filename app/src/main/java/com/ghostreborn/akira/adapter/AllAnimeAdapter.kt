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
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.ui.EpisodesActivity

class AllAnimeAdapter (private val animes: ArrayList<Anime>) :
    RecyclerView.Adapter<AllAnimeAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImageView: ImageView = itemView.findViewById(R.id.animeImageView)
        val animeNameTextView: TextView = itemView.findViewById(R.id.anime_name_text_view)
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
        holder.animeNameTextView.text = anime.name
        holder.animeImageView.load(anime.thumbnail)
        holder.itemView.setOnClickListener {
            Constants.animeId = anime.id
            holder.itemView.context.startActivity(
                Intent(holder.itemView.context, EpisodesActivity::class.java)
            )
        }
    }

}