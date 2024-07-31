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
import com.ghostreborn.akirareborn.MainActivity
import com.ghostreborn.akirareborn.R

class MainFragment : Fragment() {

    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        loginButton = view.findViewById(R.id.main_fragment_login_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!Constants.akiraSharedPreferences.getBoolean(Constants.AKIRA_LOGGED_IN, false)) {
            loginButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.AUTH_URL)))
            }
        }else{
            startActivity(Intent(context, MainActivity::class.java))
        }
    }

}