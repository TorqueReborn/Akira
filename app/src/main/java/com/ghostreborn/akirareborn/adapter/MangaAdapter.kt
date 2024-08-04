package com.ghostreborn.akirareborn.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.model.Anime
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

        }
    }

}