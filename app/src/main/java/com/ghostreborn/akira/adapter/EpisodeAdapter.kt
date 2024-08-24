package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.fragment.ServersFragment

class EpisodeAdapter(val episodeList: ArrayList<String>, val activity: AppCompatActivity) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {
    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeTitleText = itemView.findViewById<TextView>(R.id.episode_title_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_list, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return episodeList.size
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.episodeTitleText.text = episodeList[position]
        holder.itemView.setOnClickListener {
            Constants.animeEpisode = episodeList[position]
            ServersFragment().show(activity.supportFragmentManager, "Select Server")
        }
    }
}