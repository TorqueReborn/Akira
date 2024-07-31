package com.ghostreborn.akirareborn.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.adapter.AnilistAdapter
import com.ghostreborn.akirareborn.anilist.AnilistParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnilistFragment : Fragment() {

    private lateinit var anilistLoginButton: Button
    private lateinit var anilistRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anilist, container, false)
        anilistLoginButton = view.findViewById(R.id.anilist_login_button)
        anilistRecyclerView = view.findViewById(R.id.anilist_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Constants.preferences.getBoolean(Constants.AKIRA_LOGGED_IN, false)) {
            CoroutineScope(Dispatchers.IO).launch {
                AnilistParser().getAnimeList("ANIME", "CURRENT")
                withContext(Dispatchers.Main) {
                    anilistRecyclerView.adapter = AnilistAdapter(requireContext())
                    anilistRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setAnilistLoginButton()
    }

    private fun setAnilistLoginButton() {
        if (Constants.preferences.getBoolean(Constants.AKIRA_LOGGED_IN, false)) {
            anilistLoginButton.visibility = View.GONE
        } else {
            anilistLoginButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.AUTH_URL)))
            }
        }
    }

}