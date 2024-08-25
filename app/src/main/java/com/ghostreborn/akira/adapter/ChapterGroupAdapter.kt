package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.google.android.material.button.MaterialButton

class ChapterGroupAdapter(
    val chaptersGroupList: ArrayList<ArrayList<String>>,
    val chaptersRecycler: RecyclerView,
) : RecyclerView.Adapter<ChapterGroupAdapter.EpisodeGroupViewHolder>() {
    class EpisodeGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chapterPageTextView =
            itemView.findViewById<MaterialButton>(R.id.episode_group_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeGroupViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.episode_group_list, parent, false)
        return EpisodeGroupViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (chaptersGroupList.size <= 1) {
            0
        } else {
            chaptersGroupList.size
        }
    }

    override fun onBindViewHolder(holder: EpisodeGroupViewHolder, position: Int) {
        val page = "${position + 1}"
        holder.chapterPageTextView.text = page
        holder.chapterPageTextView.setOnClickListener {
            chaptersRecycler.adapter = ChapterAdapter(chaptersGroupList[position])
        }
    }
}