package com.ghostreborn.akira.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.fragment.AnimeFragment
import com.ghostreborn.akira.fragment.MangaFragment

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        if(Constants.isManga){
            supportFragmentManager.beginTransaction()
                .replace(R.id.home_frame, MangaFragment(true))
                .commit()
        }else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.home_frame, AnimeFragment(true))
                .commit()
        }
    }
}