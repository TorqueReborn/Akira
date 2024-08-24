package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.adapter.AllAnimeAdapter
import com.ghostreborn.akira.parsers.AllAnime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recycler = findViewById<RecyclerView>(R.id.main_recycler)

        CoroutineScope(Dispatchers.IO).launch {
            val anime = AllAnime().recent()
            withContext(Dispatchers.Main) {
                recycler.adapter = AllAnimeAdapter(anime)
                recycler.layoutManager = GridLayoutManager(this@MainActivity, 3)
            }
        }

    }
}