package com.ghostreborn.akirareborn.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.ui.AnimeDetailsActivity

class AnilistAdapter(private val context: Context) :
    RecyclerView.Adapter<AnilistAdapter.AnimeViewHolder>() {
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImageView: ImageView = itemView.findViewById(R.id.animeImageView)
        val animeNameTextView: TextView = itemView.findViewById(R.id.anime_name_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anime_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Constants.anilistAnimeList.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = Constants.anilistAnimeList[position]
        holder.animeNameTextView.text = anime.title
        holder.itemView.setOnClickListener {
            Constants.anilistAnime = anime
            Constants.isAnilist = true
            Constants.getAllAnimeID = true
            context.startActivity(Intent(context, AnimeDetailsActivity::class.java))
        }
    }

}