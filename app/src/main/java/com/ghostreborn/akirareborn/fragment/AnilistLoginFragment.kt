package com.ghostreborn.akirareborn.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.MainActivity
import com.ghostreborn.akirareborn.R

class AnilistLoginFragment : Fragment() {

    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_anilist_login, container, false).apply {
        loginButton = findViewById(R.id.anilist_login_button)
        progressBar = findViewById(R.id.login_progress_bar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!Constants.preferences.getBoolean(Constants.PREF_LOGGED_IN, false)) {
            loginButton.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.AUTH_URL)))
            }
        } else {
            startActivity(Intent(context, MainActivity::class.java))
        }
    }
}