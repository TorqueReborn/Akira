package com.ghostreborn.akira.fragments

import android.content.Context
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

class LoginFragment:Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<Button>(R.id.login_button)
        button.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val token = KitsuAPI().login(
                    view.findViewById<EditText>(R.id.login_user).text.toString(),
                    view.findViewById<EditText>(R.id.login_pass).text.toString()
                )
                withContext(Dispatchers.Main){
                    if (token != null) {
                        requireContext().getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
                            .edit()
                            .putString(Constants.PREF_TOKEN, token)
                            .apply()
                        Toast.makeText(requireContext(), "token: $token", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}