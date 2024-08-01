package com.ghostreborn.akirareborn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.fragment.ServerFragment
import com.squareup.picasso.Picasso

class EpisodeAdapter(val activity: AppCompatActivity) :
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
        return 0
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {

    }

}