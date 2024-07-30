package com.ghostreborn.akirareborn

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // TODO show progress based on episode titles parsed
    // TODO login to Anilist only on Anilist Fragment
    // TODO show a button to login to Anilist in AnilistFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loginAnilist()
    }

    fun loginAnilist(){
        Constants.akiraSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE)
        startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.AUTH_URL)))
    }

}