package com.ghostreborn.akirareborn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import com.squareup.picasso.Picasso

class AnimeAdapter():RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>(){
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImageView = itemView.findViewById<ImageView>(R.id.animeImageView)
        val animeNameTextView = itemView.findViewById<TextView>(R.id.anime_name_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anime_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Constants.animeList.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = Constants.animeList[position]
        holder.animeNameTextView.text = anime.name
        Picasso.get().load(anime.thumbnail).into(holder.animeImageView)
    }

}