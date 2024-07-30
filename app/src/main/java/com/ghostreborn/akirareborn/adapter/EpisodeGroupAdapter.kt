package com.ghostreborn.akirareborn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R

class EpisodeGroupAdapter(val recycler: RecyclerView) :
    RecyclerView.Adapter<EpisodeGroupAdapter.AnimeViewHolder>() {
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodePageTextView = itemView.findViewById<TextView>(R.id.episode_group_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.episode_group_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Constants.groupedEpisodes.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val page = "${position + 1}"
        holder.episodePageTextView.text = page
        holder.itemView.setOnClickListener {
            recycler.adapter = EpisodeAdapter(Constants.groupedEpisodes[position])
            recycler.layoutManager = LinearLayoutManager(recycler.context)
        }
    }

}