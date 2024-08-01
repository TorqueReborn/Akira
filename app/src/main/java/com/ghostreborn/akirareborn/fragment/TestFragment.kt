package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ghostreborn.akirareborn.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TestFragment : Fragment() {

    lateinit var testEditText: EditText
    lateinit var testUpdateButton: Button
    lateinit var testText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        testEditText = view.findViewById(R.id.test_edit_text)
        testUpdateButton = view.findViewById(R.id.test_update_button)
        testText = view.findViewById(R.id.test_text)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testUpdateButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                }
            }
        }
    }
}