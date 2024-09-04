package com.ghostreborn.akira.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.login_button).setOnClickListener {
            view.findViewById<ConstraintLayout>(R.id.login_constraint).visibility = View.GONE
            view.findViewById<ConstraintLayout>(R.id.login_info_constraint).visibility = View.VISIBLE
        }
        view.findViewById<Button>(R.id.register_button).setOnClickListener {
            Toast.makeText(requireContext(), "SignUp using browser", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://kitsu.app/explore/anime")))
        }
    }
}