package com.ghostreborn.akirareborn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

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
}