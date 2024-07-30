package com.ghostreborn.akirareborn.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R

class AnilistFragment : Fragment() {

    private lateinit var anilistLoginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anilist, container, false)
        anilistLoginButton = view.findViewById(R.id.anilist_login_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        anilistLoginButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.AUTH_URL)))
        }
    }
}