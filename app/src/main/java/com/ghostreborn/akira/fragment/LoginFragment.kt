package com.ghostreborn.akira.fragment

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
import androidx.lifecycle.lifecycleScope
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.KitsuAPI
import com.ghostreborn.akira.R
import kotlinx.coroutines.launch

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
                    Toast.makeText(requireContext(), "Login failed, Signed Up?", Toast.LENGTH_SHORT).show()
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
                val totalList = KitsuAPI().ids(userResponse!!.data[0].id,0)?.meta?.count
                loginInfoText.text = getString(R.string.you_have_anime, totalList)
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