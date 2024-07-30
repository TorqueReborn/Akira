package com.ghostreborn.akirareborn.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.PlayEpisodeActivity
import com.ghostreborn.akirareborn.R

class ServerAdapter(val context: Context) :
    RecyclerView.Adapter<ServerAdapter.AnimeViewHolder>() {
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serverTextView: TextView = itemView.findViewById(R.id.server_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.server_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Constants.episodeUrls.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val server = Constants.episodeUrls[position]
        holder.serverTextView.text = server
        holder.itemView.setOnClickListener{
            Constants.episodeUrl = server
            context.startActivity(Intent(context, PlayEpisodeActivity::class.java))
        }
    }

}