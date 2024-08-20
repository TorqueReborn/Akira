package com.ghostreborn.akira.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allManga.AllMangaParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReadMangaActivity : AppCompatActivity() {
    private var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_manga)

        val mangaImageView = findViewById<ImageView>(R.id.manga_chapter_image)
        CoroutineScope(Dispatchers.IO).launch {
            val pages = AllMangaParser().chapterPages(Constants.allMangaID, Constants.mangaChapter)
            withContext(Dispatchers.Main) {
                fun updateImage() = Glide.with(baseContext).load(pages[page]).into(mangaImageView)
                updateImage()
                val buttons = mapOf(
                    R.id.manga_previous_button to -1,
                    R.id.manga_next_button to 1
                )
                buttons.forEach { (id, delta) ->
                    findViewById<Button>(id).setOnClickListener {
                        page = (page + delta).coerceIn(0, pages.size - 1)
                        updateImage()
                    }
                }
            }
        }
    }
}