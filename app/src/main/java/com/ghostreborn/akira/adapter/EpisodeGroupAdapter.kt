package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.models.Episode
import com.google.android.material.button.MaterialButton

class EpisodeGroupAdapter(
    val activity: AppCompatActivity,
    val episodeGroupList: ArrayList<ArrayList<Episode>>,
    val episodeRecycler: RecyclerView,
) : RecyclerView.Adapter<EpisodeGroupAdapter.EpisodeGroupViewHolder>() {
    class EpisodeGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodePageTextView =
            itemView.findViewById<MaterialButton>(R.id.episode_group_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeGroupViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.episode_group_list, parent, false)
        return EpisodeGroupViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (episodeGroupList.size <= 1) {
            0
        } else {
            episodeGroupList.size
        }
    }

    override fun onBindViewHolder(holder: EpisodeGroupViewHolder, position: Int) {
        val page = "${position + 1}"
        holder.episodePageTextView.text = page
        holder.episodePageTextView.setOnClickListener {
            episodeRecycler.adapter = EpisodeAdapter(episodeGroupList[position], activity)
            episodeRecycler.layoutManager = LinearLayoutManager(activity)
        }
    }
}