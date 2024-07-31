package com.ghostreborn.akirareborn.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.database.AnilistUser
import com.ghostreborn.akirareborn.database.AnilistUserDatabase
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
            val instance = Room.databaseBuilder(
                requireContext(),
                AnilistUserDatabase::class.java,
                Constants.DATABASE_NAME
            ).build()
            for (i in 0 until Constants.anilistTest.size) {
                instance.anilistUserDao().insertAll(
                    AnilistUser(
                        Constants.anilistTest.get(i).mediaId,
                        Constants.anilistTest.get(i).malId,
                        Constants.anilistTest.get(i).allAnimeID,
                        Constants.anilistTest.get(i).title,
                        Constants.anilistTest.get(i).progress
                    )
                )
            }
            withContext(Dispatchers.Main) {
                testText.text = Constants.anilistTest.get(0).title
            }
        }
    }
}