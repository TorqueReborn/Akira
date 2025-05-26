package com.ghostreborn.akira

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghostreborn.akira.adapter.AnimeAdapter
import com.ghostreborn.akira.allAnime.AnimeBySeasonYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.test_recycler)

        CoroutineScope(Dispatchers.IO).launch {
            val month = Calendar.getInstance().get(Calendar.MONTH)
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val season = Utils().calculateQuarter(month, year)
            val animes = AnimeBySeasonYear().animeBySeasonYear(season.first, season.second)
            withContext(Dispatchers.Main) {
                val adapter = AnimeAdapter(listOf(animes))
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(applicationContext)
            }
        }

    }
}