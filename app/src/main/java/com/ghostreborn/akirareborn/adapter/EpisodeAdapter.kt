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

class EpisodeAdapter() :
    RecyclerView.Adapter<EpisodeAdapter.AnimeViewHolder>() {
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeTitleTextView: TextView = itemView.findViewById(R.id.episode_title_text_view)
        val episodeNumberTextView: TextView = itemView.findViewById(R.id.episode_number_text_view)
        val episodeThumbnailImageView: ImageView = itemView.findViewById(R.id.episode_thumbnail_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Constants.parsedEpisodes.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val episode = Constants.parsedEpisodes[position]
        holder.episodeTitleTextView.text = episode.episodeTitle
        holder.episodeNumberTextView.text = episode.episodeNumber
        Picasso.get()
            .load(episode.episodeThumbnail)
            .into(holder.episodeThumbnailImageView)
    }

}