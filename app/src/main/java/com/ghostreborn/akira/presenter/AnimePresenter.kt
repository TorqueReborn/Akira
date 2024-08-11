package com.ghostreborn.akira.presenter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.Presenter
import com.ghostreborn.akira.R
import com.ghostreborn.akira.model.Anime
import com.squareup.picasso.Picasso

class AnimePresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.anime_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val anime = item as Anime
        val name = viewHolder.view.findViewById<TextView>(R.id.anime_name_text_view)
        val thumbnail = viewHolder.view.findViewById<ImageView>(R.id.animeImageView)
        Picasso.get().load(anime.thumbnail).into(thumbnail)
        name.text = anime.name
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {

    }
}