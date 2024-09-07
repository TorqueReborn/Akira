package com.ghostreborn.akira.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.KitsuAPI
import com.ghostreborn.akira.MainActivity
import com.ghostreborn.akira.R
import com.ghostreborn.akira.database.SavedEntry
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private lateinit var userNameEdit: EditText
    private lateinit var userPassEdit: EditText
    private lateinit var loginConstraint: ConstraintLayout
    private lateinit var loginInfoConstraint: ConstraintLayout
    private lateinit var loginInfoText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userNameEdit = view.findViewById(R.id.login_user_edit)
        userPassEdit = view.findViewById(R.id.login_pass_edit)
        loginInfoText = view.findViewById(R.id.login_info_text)
        loginConstraint = view.findViewById(R.id.login_constraint)
        loginInfoConstraint = view.findViewById(R.id.login_info_constraint)
        view.findViewById<ImageView>(R.id.login_right_image).setOnClickListener {
            view.findViewById<ConstraintLayout>(R.id.login_details_constraint).visibility =
                View.GONE
            loginConstraint.visibility = View.VISIBLE
        }
        view.findViewById<Button>(R.id.login_button).setOnClickListener {
            checkLogin()
        }
    }

    private fun checkLogin() {
        val userName = userNameEdit.text.toString()
        val userPass = userPassEdit.text.toString()
        if (userName.isNotEmpty() && userPass.isNotEmpty()) {
            hideViews()
            lifecycleScope.launch {
                loginInfoText.text = getString(R.string.login_info_one)
                val loginResponse = KitsuAPI().login(userName, userPass)
                if (loginResponse == null) {
                    showViews()
                    Toast.makeText(requireContext(), "Login failed, Signed Up?", Toast.LENGTH_SHORT)
                        .show()
                    return@launch
                }
                loginInfoText.text = getString(R.string.login_info_two)
                val userResponse = KitsuAPI().user(loginResponse.access_token)
                if (userResponse != null) {
                    requireContext().getSharedPreferences(Constants.SHARED_PREF, 0)
                        .edit()
                        .putString(Constants.PREF_USER_NAME, userResponse.data[0].attributes.name)
                        .putString(Constants.PREF_USER_ID, userResponse.data[0].id)
                        .putString(Constants.PREF_TOKEN, loginResponse.access_token)
                        .putString(Constants.PREF_REFRESH_TOKEN, loginResponse.refresh_token)
                        .apply()
                }
                val totalList = KitsuAPI().ids(userResponse!!.data[0].id, 0)
                loginInfoText.text = getString(R.string.you_have_anime, totalList?.meta?.count)

                val db = AkiraUtils().getDB(requireContext())

                withContext(Dispatchers.IO) {
                    for (i in 0 until totalList!!.data.size) {
                        val anime = KitsuAPI().anime(totalList.data[i].id)
                        withContext(Dispatchers.Main) {
                            loginInfoText.text =
                                getString(
                                    R.string.inserting_to_database,
                                    anime?.data?.attributes?.canonicalTitle
                                )
                        }
                        val entry = SavedEntry(
                            totalList.data[i].id,
                            totalList.data[i].attributes.progress,
                            anime?.data?.attributes?.canonicalTitle.toString(),
                            anime?.data?.attributes?.posterImage?.large.toString()
                        )
                        db.savedEntryDao().insert(entry)
                    }
                }

                requireContext().getSharedPreferences(Constants.SHARED_PREF, 0)
                    .edit()
                    .putBoolean(Constants.PREF_LOGGED_IN, true)
                    .apply()

                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()

            }
        }
    }

    private fun hideViews() {
        loginConstraint.visibility = View.GONE
        loginInfoConstraint.visibility = View.VISIBLE
    }

    private fun showViews() {
        loginConstraint.visibility = View.VISIBLE
        loginInfoConstraint.visibility = View.GONE
    }

}