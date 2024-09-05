package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R

class LoginFragment : Fragment() {

    lateinit var userNameEdit: EditText
    lateinit var userPassEdit: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userNameEdit = view.findViewById(R.id.login_user_edit)
        userPassEdit = view.findViewById(R.id.login_pass_edit)
        view.findViewById<Button>(R.id.login_button).setOnClickListener {
            checkLogin()
        }
    }

    private fun checkLogin(){
        val userName = userNameEdit.text.toString()
        val userPass = userPassEdit.text.toString()
        if (userName.isNotEmpty() && userPass.isNotEmpty()){
            Toast.makeText(requireContext(), "Logged In", Toast.LENGTH_SHORT).show()
        }
    }

}