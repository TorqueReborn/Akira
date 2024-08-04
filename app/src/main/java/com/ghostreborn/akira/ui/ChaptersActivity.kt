package com.ghostreborn.akira.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.ChapterAdapter
import com.ghostreborn.akira.allManga.AllMangaParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChaptersActivity : AppCompatActivity() {

    lateinit var chapterRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapters)
        chapterRecycler = findViewById(R.id.chapters_recycler_view)
        fetchChapters()
    }

    private fun fetchChapters() {
        CoroutineScope(Dispatchers.IO).launch {
            val group = AllMangaParser().mangaChapters(Constants.allMangaID)
            withContext(Dispatchers.Main) {
                chapterRecycler.adapter = ChapterAdapter(group[0])
                chapterRecycler.layoutManager = LinearLayoutManager(this@ChaptersActivity)
            }
        }
    }

}