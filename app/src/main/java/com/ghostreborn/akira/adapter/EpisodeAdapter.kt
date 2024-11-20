package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R

class EpisodeAdapter(
    private val episodes: ArrayList<String>
): RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episode = itemView.findViewById<TextView>(R.id.episode_text)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_list, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]
        holder.episode.text = episode
    }

    override fun getItemCount(): Int {
        return episodes.size
    }
}