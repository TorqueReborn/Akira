package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.allAnime.TestAllAnime
import com.ghostreborn.akira.allManga.AllMangaNetwork
import com.ghostreborn.akira.allManga.AllMangaParser
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TestFragment : Fragment() {

    lateinit var testText: TextView
    lateinit var testImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        testText = view.findViewById(R.id.test_text)
        testImage = view.findViewById(R.id.imageView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            val test = AllMangaParser().chapterPages("ex9vXC6gWYY9bGkSo", "500")
            withContext(Dispatchers.Main) {
                testText.text = test[0]
                Picasso.get()
                    .load(test[0])
                    .into(testImage)
            }
        }
    }

}