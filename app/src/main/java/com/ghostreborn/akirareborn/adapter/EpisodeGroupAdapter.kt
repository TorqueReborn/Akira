package com.ghostreborn.akirareborn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.fragment.AnimeFragment
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodeGroupAdapter(
    val activity: AppCompatActivity,
    val episodeGroupList: ArrayList<ArrayList<String>>,
    val episodeRecycler: RecyclerView,
    val progressBar: ProgressBar
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
            progressBar.visibility = ProgressBar.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                val parsed = AllAnimeParser().episodeDetails(
                    AnimeFragment.allAnimeID,
                    episodeGroupList[position]
                )
                withContext(Dispatchers.Main) {
                    progressBar.visibility = ProgressBar.GONE
                    episodeRecycler.adapter = EpisodeAdapter(parsed, activity)
                    episodeRecycler.layoutManager = LinearLayoutManager(activity)
                }
            }
        }
    }
}