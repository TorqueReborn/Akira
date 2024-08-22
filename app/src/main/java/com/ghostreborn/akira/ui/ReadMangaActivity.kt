package com.ghostreborn.akira.ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allManga.AllMangaParser
import com.github.panpf.sketch.loadImage
import com.github.panpf.zoomimage.CoilZoomImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReadMangaActivity : AppCompatActivity() {
    private var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_manga)

        val mangaImageView = findViewById<CoilZoomImageView>(R.id.manga_chapter_image)
        CoroutineScope(Dispatchers.IO).launch {
            val pages = AllMangaParser().chapterPages(Constants.allMangaID, Constants.mangaChapter)
            withContext(Dispatchers.Main) {
                fun updateImage(){
                    mangaImageView.loadImage(pages[page])
                }
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