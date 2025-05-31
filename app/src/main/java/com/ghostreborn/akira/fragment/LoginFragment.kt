package com.ghostreborn.akira.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R
import com.ghostreborn.akira.api.anilList.AniListAuthorize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment: Fragment() {

    private lateinit var progress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<Button>(R.id.login_button)
        progress = view.findViewById(R.id.aniList_progress)
        button.setOnClickListener {
            val url = "https://anilist.co/api/v2/oauth/authorize?client_id=25543&redirect_uri=akira://ghostreborn.in&response_type=code"
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        }
    }

    override fun onResume() {
        super.onResume()
        parseToken()
    }

    fun parseToken() {
        val activity = activity
        if(activity != null) {
            val intent = activity.intent
            if(intent != null) {
                val uri = intent.data
                if(uri != null) {
                    val code = uri.getQueryParameter("code")
                    progress.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).launch {
                        val token = AniListAuthorize().getToken(code.toString())
                        val userID = AniListAuthorize().getUserID(token)
                        withContext(Dispatchers.Main) {
                            val pref = activity.getSharedPreferences("AKIRA", 0)
                            pref.edit {
                                putBoolean("LOGIN", true)
                                putString("TOKEN", token)
                                putString("USER", userID)
                            }
                            activity.finish()
                        }
                    }
                }
            }
        }
    }
}