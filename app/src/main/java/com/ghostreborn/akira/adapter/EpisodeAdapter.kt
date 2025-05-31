package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.api.allAnime.AnimeServers
import com.ghostreborn.akira.fragment.ServerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EpisodeAdapter(
    private val id: String,
    private val episodes: List<String>,
    private val support: FragmentManager
): RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.episode_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.episodeNumber.text = episodes[position]
        holder.itemView.setOnClickListener {
            if(MainActivity.internetAvailable) {
                CoroutineScope(Dispatchers.IO).launch {
                    val servers = AnimeServers().servers(id, episodes[position])
                    withContext(Dispatchers.Main) {
                        ServerFragment(servers).show(support, "server")
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return episodes.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val episodeNumber: TextView = itemView.findViewById(R.id.episodeNumber)
    }
}