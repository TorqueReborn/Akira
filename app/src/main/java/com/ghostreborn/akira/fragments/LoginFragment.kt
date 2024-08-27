package com.ghostreborn.akira.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


    private lateinit var loginConstraint: ConstraintLayout
    private lateinit var loginConstraintIntermediate: ConstraintLayout
    private lateinit var loginButton: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         loginConstraint = view.findViewById(R.id.login_constraint)
         loginConstraintIntermediate = view.findViewById(R.id.login_intermediate_constraint)
         loginButton = view.findViewById(R.id.login_button)
    }

    override fun onResume() {
        super.onResume()
        AkiraUtils().checkLogin(requireContext()).takeIf { it }?.run {
            startActivity(Intent(requireContext(), MainActivity::class.java)).also { requireActivity().finish() }
        } ?: run {
            loginConstraint.visibility = if (Constants.hasClickedLoginButton) View.GONE else View.VISIBLE
            loginConstraintIntermediate.visibility = if (Constants.hasClickedLoginButton) View.VISIBLE else View.GONE
            loginButton.takeUnless { Constants.hasClickedLoginButton }?.setOnClickListener {
                Constants.hasClickedLoginButton = true
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.AUTH_URL)))
            }
        }
    }

}