package com.ghostreborn.akirareborn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allanime.AllAnimeParser
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodeGroupAdapter(val recycler: RecyclerView, val activity: AppCompatActivity, val progressBar: ProgressBar) :
    RecyclerView.Adapter<EpisodeGroupAdapter.AnimeViewHolder>() {
    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodePageTextView =
            itemView.findViewById<MaterialButton>(R.id.episode_group_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.episode_group_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 0
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {

    }

}