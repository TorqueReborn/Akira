package com.ghostreborn.akira.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.kitsu.KitsuAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            val user = view.findViewById<EditText>(R.id.login_user).text.toString()
            val pass = view.findViewById<EditText>(R.id.login_pass).text.toString()
            loginAndStoreToken(user, pass)
        }
        val registerButton = view.findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            Toast.makeText(requireContext(), "SignUp using browser", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://kitsu.app/explore/anime")))
        }
    }

    private fun loginAndStoreToken(userName:String, pass:String){
        CoroutineScope(Dispatchers.IO).launch {
            val login = KitsuAPI().login(userName, pass)
            withContext(Dispatchers.Main){
                if (login != null) {
                    requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE).edit()
                        .putString(Constants.PREF_TOKEN, login.access_token)
                        .putString(Constants.PREF_REFRESH_TOKEN, login.refresh_token)
                        .apply()
                }
            }
        }
    }
}