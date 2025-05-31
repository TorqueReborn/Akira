package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.api.allAnime.AnimeUrls
import com.ghostreborn.akira.model.Server
import com.ghostreborn.akira.ui.LinkFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerAdapter(
    private val server: ArrayList<Server>,
    private val support: FragmentManager
) : RecyclerView.Adapter<ServerAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serverName: TextView =
            itemView.findViewById(R.id.server_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.server_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentServer = server[position]
        holder.serverName.text = currentServer.name
        holder.itemView.setOnClickListener {
            if(MainActivity.internetAvailable) {
                CoroutineScope(Dispatchers.IO).launch {
                    val servers = AnimeUrls().urls(currentServer.url)
                    withContext(Dispatchers.Main) {
                        LinkFragment(servers).show(support, "link")
                    }
                }
            }
        }
    }

    override fun getItemCount() = server.size
}