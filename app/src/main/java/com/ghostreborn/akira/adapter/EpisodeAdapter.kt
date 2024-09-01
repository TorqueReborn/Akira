package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.fragments.ServerFragment
import com.ghostreborn.akira.models.Episode

class EpisodeAdapter(val episodeList: ArrayList<Episode>, val activity: AppCompatActivity) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {
    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeTitleText = itemView.findViewById<TextView>(R.id.episode_title_text_view)
        val episodeThumbnail = itemView.findViewById<ImageView>(R.id.episode_thumbnail_image_view)
        val episodeNumber = itemView.findViewById<TextView>(R.id.episode_number_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_list, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return episodeList.size
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.episodeThumbnail.load(Constants.animeThumbnail)
        holder.episodeNumber.text = episodeList[position].epNum
        holder.episodeTitleText.text = episodeList[position].title
        holder.itemView.setOnClickListener {
            Constants.animeEpisode = episodeList[position].epNum
            Constants.episodeWatchId = episodeList[position].id
            ServerFragment().show(activity.supportFragmentManager, "Select Server")
        }
    }
}