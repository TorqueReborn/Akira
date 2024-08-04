package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allManga.AllMangaNetwork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class TestFragment : Fragment() {

    lateinit var testText: TextView

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
            val test = JSONObject(AllMangaNetwork().searchManga("").toString())
                .getJSONObject("data")
                .getJSONObject("mangas")
                .getJSONArray("edges")
            var out = ""
            for (i in 0 until test.length()) {
                out += test.getJSONObject(i).getString("aniListId") + "\n\n"
            }
            withContext(Dispatchers.Main) {
                testText.text = out
            }

        }
    }

}