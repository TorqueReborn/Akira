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
import com.ghostreborn.akirareborn.model.Episode
import com.squareup.picasso.Picasso

class EpisodeAdapter(
    private val episodeList: List<Episode>,
    private val activity: AppCompatActivity
) :
    RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {
    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeTitleTextView: TextView = itemView.findViewById(R.id.episode_title_text_view)
        val episodeNumberTextView: TextView = itemView.findViewById(R.id.episode_number_text_view)
        val episodeThumbnailImageView: ImageView =
            itemView.findViewById(R.id.episode_thumbnail_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_list, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return episodeList.size
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodeList[position]
        holder.episodeTitleTextView.text = episode.episodeTitle
        holder.episodeNumberTextView.text = episode.episodeNumber
        Picasso.get()
            .load(episode.episodeThumbnail)
            .into(holder.episodeThumbnailImageView)
        holder.itemView.setOnClickListener {
            Constants.animeEpisode = episode.episodeNumber
            ServerFragment()
                .show(activity.supportFragmentManager, "Select Server")
        }
    }
}