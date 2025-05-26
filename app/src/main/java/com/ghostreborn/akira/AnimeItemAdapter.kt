package com.ghostreborn.akira

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AnimeItemAdapter(
    private val anime: List<Anime>
) : RecyclerView.Adapter<AnimeItemAdapter.AnimeItemViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AnimeItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.anime_item, parent, false)
        return AnimeItemViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: AnimeItemViewHolder,
        position: Int
    ) {
        val adapter = AnimeListAdapter(anime[position].animeImageList)
        holder.animeSeason.text = anime[position].animeSeason
        holder.animeRecycler.adapter = adapter
        holder.animeRecycler.layoutManager = GridLayoutManager(
            holder.animeRecycler.context,
            3,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    override fun getItemCount(): Int {
        return anime.size
    }

    class AnimeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val animeSeason = itemView.findViewById<TextView>(R.id.anime_season)
        val animeRecycler = itemView.findViewById<RecyclerView>(R.id.anime_recycler)
    }
}