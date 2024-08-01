package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.database.AnilistDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TestFragment : Fragment() {

    lateinit var testText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        testText = view.findViewById(R.id.test_text)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            val instance = Room.databaseBuilder(
                requireContext(),
                AnilistDatabase::class.java,
                "Akira"
            ).build()
            val anilists = instance.anilistDao().getAll()
            var ids = ""
            for (i in 0 until anilists.size) {
                ids += "\"" + anilists[i].allAnimeID + "\","
            }
            ids = ids.substring(0, ids.length - 1)
            val test = AllAnimeParser().getDetailsByIds(ids)
            withContext(Dispatchers.Main) {
                testText.text = test.get(0).thumbnail
            }
        }
    }

}