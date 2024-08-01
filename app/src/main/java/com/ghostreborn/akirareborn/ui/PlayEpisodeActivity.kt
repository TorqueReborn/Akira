package com.ghostreborn.akirareborn.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.fragment.AnimeFragment


class PlayEpisodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_episode)

        hideSystemBars()

        val videoView = findViewById<VideoView>(R.id.anime_video_view)
        videoView.setVideoURI(Uri.parse(AnimeFragment.animeUrl))
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.start()

    }

    private fun hideSystemBars() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController: WindowInsetsControllerCompat =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
            if (windowInsets.isVisible(WindowInsetsCompat.Type.systemBars())) {
                view.postDelayed({
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                }, 2000)
            }
            windowInsets
        }
    }
}