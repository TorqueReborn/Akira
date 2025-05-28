package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.model.SourceName

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
            Toast.makeText(holder.itemView.context, server[position].url, Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount() = server.size
}