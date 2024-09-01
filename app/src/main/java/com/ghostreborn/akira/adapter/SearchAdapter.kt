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
import com.ghostreborn.akira.parser.kitsu.KitsuAPI
import com.ghostreborn.akira.models.Search
import com.ghostreborn.akira.ui.EpisodesActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchAdapter (private val animes: Search
) : RecyclerView.Adapter<SearchAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.anime_image)
        val name: TextView = itemView.findViewById(R.id.anime_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anime_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return animes.data.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = animes.data[position]
        holder.name.text = anime.attributes.canonicalTitle
        holder.thumbnail.load(anime.attributes.posterImage.medium)
        holder.itemView.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                Constants.animeID = KitsuAPI().anilistID(anime.id)?.data?.get(0)?.attributes?.externalId!!
                withContext(Dispatchers.Main){
                    holder.itemView.context.startActivity(Intent(holder.itemView.context, EpisodesActivity::class.java))
                }
            }
        }
    }

}