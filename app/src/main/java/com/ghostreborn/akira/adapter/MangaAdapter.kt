package com.ghostreborn.akira.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.ui.MangaDetailsActivity
import com.squareup.picasso.Picasso

class MangaAdapter(private val mangas: ArrayList<Anime>) :
    RecyclerView.Adapter<MangaAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mangaImageView: ImageView = itemView.findViewById(R.id.animeImageView)
        val mangaNameTextView: TextView = itemView.findViewById(R.id.anime_name_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anime_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mangas.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val manga = mangas[position]
        holder.mangaNameTextView.text = manga.name
        Picasso.get().load(manga.thumbnail).into(holder.mangaImageView)
        holder.itemView.setOnClickListener {
            Constants.allMangaID = manga.id
            Constants.mangaThumbnail = manga.thumbnail
            holder.itemView.context.startActivity(
                Intent(
                    holder.itemView.context,
                    MangaDetailsActivity::class.java
                )
            )
        }
    }

}