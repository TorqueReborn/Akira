package com.ghostreborn.akira.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.api.allAnime.AnimeSearch
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.ui.DetailsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchAdapter(
    private val query: String,
    private val anime: ArrayList<Anime>
) : RecyclerView.Adapter<SearchAdapter.AnimeViewHolder>() {

    private fun addItem(anime: ArrayList<Anime>) {
        this.anime.addAll(anime)
        notifyItemInserted(this.anime.size - 1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnimeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.anime_list, parent, false)
        return AnimeViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: AnimeViewHolder,
        position: Int
    ) {
        if (position == itemCount - 1 && MainActivity.internetAvailable) {
            CoroutineScope(Dispatchers.IO).launch {
                val anime = AnimeSearch().animeSearch(query)
                if(anime != null) {
                    withContext(Dispatchers.Main) {
                        addItem(anime)
                    }
                }
            }
        }
        val animeItem = anime[position]
        holder.animeImage.load(animeItem.thumbnail)
        holder.itemView.setOnClickListener {
            holder.itemView.context.startActivity(
                Intent(holder.animeImage.context, DetailsActivity::class.java).apply {
                    putExtra("animeID", animeItem.id)
                }
            )
        }
    }

    override fun getItemCount(): Int {
        return anime.size
    }

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeImage: ImageView = itemView.findViewById(R.id.anime_image)
    }

}