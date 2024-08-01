package com.ghostreborn.akirareborn.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.ui.AnimeDetailsActivity
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.fragment.AnimeFragment
import com.ghostreborn.akirareborn.model.Anime
import com.squareup.picasso.Picasso

class AnimeAdapter(private val animes: ArrayList<Anime>) :
    RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

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
        Picasso.get().load(anime.thumbnail).into(holder.animeImageView)
        holder.itemView.setOnClickListener {
            AnimeFragment.allAnimeID = anime.id
            AnimeFragment.animeThumbnail = anime.thumbnail
            AnimeFragment.animeEpisode = ""
            holder.itemView.context.startActivity(Intent(holder.itemView.context, AnimeDetailsActivity::class.java))
        }
    }

}