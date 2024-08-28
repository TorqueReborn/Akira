package com.ghostreborn.akira.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R
import com.ghostreborn.akira.anilist.AnilistParser
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = LayoutInflater.from(context).inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                val userID = AkiraUtils().getUserID(requireContext())
                val anime = AnilistParser().userAnime(userID)
                val manga = AnilistParser().userManga(userID)
                childFragmentManager.beginTransaction()
                    .replace(R.id.anime_frame, CommonFragment("Anime", anime))
                    .replace(R.id.manga_frame, CommonFragment("Manga", manga))
                    .commit()
            }
        }
    }
}