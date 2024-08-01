package com.ghostreborn.akirareborn.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.MainActivity
import com.ghostreborn.akirareborn.R

class AnilistLoginFragment : Fragment() {

    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anilist_login, container, false)
        loginButton = view.findViewById(R.id.anilist_login_button)
        return view
    }

    override fun onResume() {
        super.onResume()
        if (!Constants.preferences.getBoolean(Constants.PREF_LOGGED_IN, false)){
            loginButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.AUTH_URL)))
            }
        }else{
            startActivity(Intent(context, MainActivity::class.java))
        }
    }
}