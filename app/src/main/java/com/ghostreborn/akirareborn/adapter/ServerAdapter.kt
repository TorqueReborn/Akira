package com.ghostreborn.akirareborn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R

class ServerAdapter() :
    RecyclerView.Adapter<ServerAdapter.AnimeViewHolder>() {
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serverTextView: TextView = itemView.findViewById(R.id.server_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.server_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Constants.parsedEpisodes.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val server = Constants.episodeUrls[position]
        holder.serverTextView.text = server
    }

}