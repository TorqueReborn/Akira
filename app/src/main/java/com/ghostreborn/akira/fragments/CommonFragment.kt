package com.ghostreborn.akira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.CommonAdapter
import com.ghostreborn.akira.model.Anime

class CommonFragment(
    private val name: String,
    private val data: ArrayList<Anime>,
    private val isManga:Boolean = false
) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = LayoutInflater.from(context).inflate(R.layout.common_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.common_recycler).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        view.findViewById<TextView>(R.id.main_head_text).text = name
        if (data.isNotEmpty()) {
            recycler.adapter = CommonAdapter(isManga, data)
        } else {
            view.findViewById<CardView>(R.id.empty_card).visibility = View.VISIBLE
        }
    }


}