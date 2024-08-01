package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.database.AnilistUserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllAnimeDetailsFragment : Fragment() {

    private lateinit var testText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_anime_details, container, false)
        testText = view.findViewById(R.id.test_text)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAnilist()
    }

    private fun checkAnilist() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                requireContext(),
                AnilistUserDatabase::class.java,
                Constants.DATABASE_NAME
            ).build()
            val anilist = db.anilistUserDao().findByAllAnimeID(Constants.allAnimeID)
            withContext(Dispatchers.Main) {
                if (anilist != null) {
                    testText.text = anilist.progress
                }
            }
        }
    }
}