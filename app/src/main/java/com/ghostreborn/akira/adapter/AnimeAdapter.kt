package com.ghostreborn.akira.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ghostreborn.akira.R
import com.ghostreborn.akira.Utils
import com.ghostreborn.akira.api.allAnime.AnimeSeason
import com.ghostreborn.akira.fragment.SeasonalFragment
import com.ghostreborn.akira.model.AnimeItem
import com.ghostreborn.akira.model.Anime
import com.ghostreborn.akira.ui.DetailsActivity
import com.ghostreborn.akira.ui.SeasonalActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnimeAdapter(
    private val animeItemItems: ArrayList<AnimeItem>
) : RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>() {

    private fun addItem(animeItem: AnimeItem) {
        animeItemItems.add(animeItem)
        notifyItemInserted(animeItemItems.size - 1)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnimeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.anime_item, parent, false)
        return AnimeViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: AnimeViewHolder,
        position: Int
    ) {
        if (position == itemCount - 1) {
            SeasonalFragment.count++
            val season = Utils().calculateQuarter(SeasonalFragment.count)
            CoroutineScope(Dispatchers.IO).launch {
                val anime = AnimeSeason().animeBySeasonYear(season.first, season.second)
                withContext(Dispatchers.Main) {
                    addItem(anime)
                }
            }
        }
        val adapter = AnimeItemAdapter(animeItemItems[position].animeList)
        holder.animeSeason.text = animeItemItems[position].animeSeason
        holder.animeRecycler.adapter = adapter
        holder.animeRecycler.layoutManager = GridLayoutManager(
            holder.animeRecycler.context,
            3,
            LinearLayoutManager.VERTICAL,
            false
        )
        holder.animeSeason.setOnClickListener {
            val intent = Intent(holder.animeSeason.context, SeasonalActivity::class.java)
            intent.putExtra("animeSeason", animeItemItems[position].animeSeason)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return animeItemItems.size
    }

    class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeSeason: TextView = itemView.findViewById(R.id.anime_season)
        val animeRecycler: RecyclerView = itemView.findViewById(R.id.anime_recycler)
    }

    class AnimeItemAdapter(
        private val animeList: List<Anime>
    ) : RecyclerView.Adapter<AnimeItemAdapter.AnimeViewHolder>() {
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
            val animeItem = animeList[position]
            holder.animeImage.load(animeItem.thumbnail)
            holder.animeImage.setOnClickListener {
                holder.itemView.context.startActivity(
                    Intent(holder.animeImage.context, DetailsActivity::class.java).apply {
                        putExtra("animeID", animeItem.id)
                    }
                )
            }
        }

        override fun getItemCount(): Int {
            return animeList.size
        }

        class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val animeImage: ImageView = itemView.findViewById(R.id.anime_image)
        }
    }
}