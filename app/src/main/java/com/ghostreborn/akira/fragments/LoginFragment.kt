package com.ghostreborn.akira.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R
import com.ghostreborn.akira.database.SavedEntry
import com.ghostreborn.akira.parser.kitsu.KitsuAPI
import com.ghostreborn.akira.utils.AkiraUtils
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
        view.findViewById<Button>(R.id.login_button).setOnClickListener {
            val user = view.findViewById<EditText>(R.id.login_user).text.toString()
            val pass = view.findViewById<EditText>(R.id.login_pass).text.toString()
            if (user.isNotEmpty() || pass.isNotEmpty()) {
                getData(user, pass)
            }
        }
        view.findViewById<Button>(R.id.register_button).setOnClickListener {
            Toast.makeText(requireContext(), "SignUp using browser", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://kitsu.app/explore/anime")))
        }
    }

    fun getData(userName: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val authentication = AkiraUtils().login(userName, pass)
            withContext(Dispatchers.Main) {
                if (authentication == null) {
                    Toast.makeText(
                        requireContext(),
                        "Username or password incorrect!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    view?.findViewById<ConstraintLayout>(R.id.login_constraint)?.visibility =
                        View.GONE
                    view?.findViewById<ConstraintLayout>(R.id.login_info_constraint)?.visibility =
                        View.VISIBLE
                    view?.findViewById<TextView>(R.id.login_info_text)?.text = "Getting anime list..."
                    getAnimeList(authentication.access_token)
                }
            }
        }
    }

    fun getAnimeList(accessToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val userID = KitsuAPI().user(accessToken)
            val libraryEntries = AkiraUtils().ids(userID.toString())
            val db = AkiraUtils().getDB(requireContext())

            for(entry in libraryEntries){
                val saved = SavedEntry(
                    entry.id,
                    entry.progress,
                    entry.status
                )
                db.savedEntryDao().insert(saved)
            }
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(), "Anime number: ${libraryEntries.size}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}