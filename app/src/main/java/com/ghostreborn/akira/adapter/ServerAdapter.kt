package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.model.SourceName

class ServerAdapter(
    val servers: ArrayList<SourceName>
) : RecyclerView.Adapter<ServerAdapter.ViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.server_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.serverName.text = servers[position].name
    }

    override fun getItemCount(): Int {
        return servers.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serverName: TextView = itemView.findViewById(R.id.server_name)
    }

}