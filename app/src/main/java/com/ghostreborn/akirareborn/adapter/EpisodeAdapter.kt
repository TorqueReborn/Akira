package com.ghostreborn.akirareborn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R

class EpisodeAdapter() : RecyclerView.Adapter<EpisodeAdapter.AnimeViewHolder>() {
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeTitleTextView = itemView.findViewById<TextView>(R.id.episode_title_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Constants.episodeList.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val episode = Constants.episodeList[position]
        holder.episodeTitleTextView.text = episode
    }

}