package com.ghostreborn.akira.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.utils.AkiraUtils

class LoginFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_login, container, false)

    private lateinit var constraint: ConstraintLayout
    private lateinit var frame: FrameLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginButton = view.findViewById<ImageView>(R.id.login_button)
        frame = view.findViewById(R.id.login_intermediate_frame)
        constraint = view.findViewById(R.id.login_constraint)

        loginButton.setOnClickListener {
            Constants.hasClickedLoginButton = true
            constraint.removeAllViews()
            LayoutInflater.from(requireContext()).inflate(R.layout.fragment_login_intermediate, frame, true)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.AUTH_URL)))
        }
    }

    override fun onResume() {
        super.onResume()
        if(AkiraUtils().checkLogin(requireContext())){
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
        if(Constants.hasClickedLoginButton){
            constraint.removeAllViews()
            LayoutInflater.from(requireContext()).inflate(R.layout.fragment_login_intermediate, frame, true)
        }
    }

}