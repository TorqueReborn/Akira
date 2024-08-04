package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.model.Episode
import com.squareup.picasso.Picasso

class ChapterAdapter(
    private val chapterList: List<Episode>,
    private val activity: AppCompatActivity
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
        holder.chapterTitleTextView.text = chapter.episodeTitle
        holder.chapterNumberTextView.text = chapter.episodeNumber
        Picasso.get()
            .load(chapter.episodeThumbnail)
            .into(holder.chapterThumbnailImageView)
        holder.itemView.setOnClickListener {
            Constants.mangaChapter = chapter.episodeNumber
        }
    }
}