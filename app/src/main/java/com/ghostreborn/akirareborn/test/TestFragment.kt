package com.ghostreborn.akirareborn.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestFragment : Fragment() {

    private lateinit var testText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        testText = view.findViewById(R.id.test_text)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            Constants.anilistUserID =
                Constants.preferences.getString(Constants.AKIRA_USER_ID, "").toString()
            Constants.anilistToken =
                Constants.preferences.getString(Constants.AKIRA_TOKEN, "").toString()
            TestAPI().getAnilist()
            withContext(Dispatchers.Main) {
                testText.text = Constants.anilistTest.get(0).title
            }
        }
    }
}