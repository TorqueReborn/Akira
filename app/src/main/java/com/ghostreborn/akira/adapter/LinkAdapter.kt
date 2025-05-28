package com.ghostreborn.akira.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.model.SourceName
import com.ghostreborn.akira.ui.PlayerActivity

class LinkAdapter(
    private val server: ArrayList<SourceName>,
) : RecyclerView.Adapter<LinkAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serverName: TextView =
            itemView.findViewById(R.id.server_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.server_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.serverName.text = server[position].name
        holder.itemView.setOnClickListener {
            holder.itemView.context.startActivity(Intent(holder.itemView.context, PlayerActivity::class.java).apply {
                putExtra("url", server[position].url)
            })
        }
    }

    override fun getItemCount() = server.size
}