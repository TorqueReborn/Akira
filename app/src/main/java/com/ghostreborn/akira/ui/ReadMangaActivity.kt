package com.ghostreborn.akira.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.adapter.MangaChapterAdapter
import com.ghostreborn.akira.allManga.AllMangaParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReadMangaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_manga)

        val viewPager = findViewById<ViewPager2>(R.id.read_manga_view_pager)
        CoroutineScope(Dispatchers.IO).launch {
            val test = AllMangaParser().chapterPages(Constants.allMangaID, Constants.mangaChapter)
            withContext(Dispatchers.Main) {
                viewPager.adapter = MangaChapterAdapter(test)
            }
        }

    }
}