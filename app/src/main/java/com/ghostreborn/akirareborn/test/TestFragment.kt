package com.ghostreborn.akirareborn.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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
            val url =
                "https://allanime.day/apivtwo/vidcdn.json?id=7d2473746a243c246e727276753c2929636b6472676d73287674692975727463676b6f686128766e76396f623b4b6c4f354952573420726f726a633b4968632d566f6365632d43766f756962632d3737373420727f76637573643b555344242a2475727463676b63744f62243c24706f62636975727463676b6f69242a2462677263243c24343634322b36312b35375237353c37373c3636283636365c247b&referer="
            val test = TestAPI().getJSON(url)
            withContext(Dispatchers.Main) {
                testText.text = test
            }
        }
    }
}