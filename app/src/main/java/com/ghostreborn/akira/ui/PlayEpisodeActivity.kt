package com.ghostreborn.akira.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R


class PlayEpisodeActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private val videoUrl: Uri by lazy { Uri.parse(Constants.animeUrl) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_episode)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val playerView = findViewById<PlayerView>(R.id.anime_video_view)
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

    }

    override fun onStop() {
        super.onStop()
        exoPlayer.release()
    }

}