package com.ghostreborn.akira.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.ui.ReadMangaActivity

class ChapterAdapter(
    private val chapterList: ArrayList<String>,
) :
    RecyclerView.Adapter<ChapterAdapter.EpisodeViewHolder>() {
    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chapterTitleTextView: TextView = itemView.findViewById(R.id.episode_title_text_view)
        val chapterNumberTextView: TextView = itemView.findViewById(R.id.episode_number_text_view)
        val chapterThumbnailImageView: ImageView =
            itemView.findViewById(R.id.episode_thumbnail_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_list, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chapterList.size
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val chapter = chapterList[position]
        val chapterName = "Chapter $chapter"
        holder.chapterTitleTextView.text = chapterName
        holder.chapterNumberTextView.text = chapter
        holder.chapterThumbnailImageView.load(Constants.mangaThumbnail)
        holder.itemView.setOnClickListener {
            Constants.mangaChapter = chapter
            holder.itemView.context.startActivity(
                Intent(holder.itemView.context, ReadMangaActivity::class.java)
            )
        }
    }
}