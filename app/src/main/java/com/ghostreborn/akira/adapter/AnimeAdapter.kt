package com.ghostreborn.akira.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.ui.EpisodesActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.parser.kitsu.KitsuAPI
import com.ghostreborn.akira.models.Anime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeAdapter (private val animes: ArrayList<Anime>
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.anime_image)
        val progress: TextView = itemView.findViewById(R.id.anime_progress)
        val name: TextView = itemView.findViewById(R.id.anime_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.anime_list, parent, false)
        return AnimeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return animes.size
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        val anime = animes[position]

        val progress = "Watched ${anime.progress} Episodes"

        holder.name.text = anime.title
        holder.progress.text = progress
        holder.thumbnail.load(anime.thumbnail)

        holder.itemView.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                Constants.animeID = KitsuAPI().anilistID(anime.kitsuID)?.data?.get(0)?.attributes?.externalId!!
                withContext(Dispatchers.Main){
                    Toast.makeText(holder.itemView.context, Constants.animeID, Toast.LENGTH_SHORT).show()
                    holder.itemView.context.startActivity(Intent(holder.itemView.context, EpisodesActivity::class.java))
                }
            }
        }

    }

}