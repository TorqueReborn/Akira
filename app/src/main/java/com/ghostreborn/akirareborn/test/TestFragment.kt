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
                "https://api.allanime.day/api?variables={%22pageSearch%22:{%22type%22:%22all%22,%22pageId%22:%2266a5aa26cee2deeaa733520e%22,%22pageType%22:%22ep_cp%22,%22allowSameShow%22:true,%22allowAdult%22:false,%22allowUnknown%22:false}}&extensions={%22persistedQuery%22:{%22version%22:1,%22sha256Hash%22:%2245167ede14941284b6ffe7c1b8dd81f56a197f600e48c2c92e256c489f1563d5%22}}"
            val test = TestAPI().connectAllAnime(url)
            withContext(Dispatchers.Main) {
                testText.text = test
            }
        }
    }
}