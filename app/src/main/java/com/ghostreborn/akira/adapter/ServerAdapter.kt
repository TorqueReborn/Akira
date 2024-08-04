package com.ghostreborn.akira.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.ui.PlayEpisodeActivity

class ServerAdapter(private val sourceUrls: List<String>) :
    RecyclerView.Adapter<ServerAdapter.AnimeViewHolder>() {
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serverTextView: TextView = itemView.findViewById(R.id.server_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.server_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sourceUrls.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val server = sourceUrls[position]
        holder.serverTextView.text = server
        holder.itemView.setOnClickListener {
            Constants.animeUrl = server
            holder.itemView.context.startActivity(
                Intent(holder.itemView.context, PlayEpisodeActivity::class.java)
            )
        }
    }

}