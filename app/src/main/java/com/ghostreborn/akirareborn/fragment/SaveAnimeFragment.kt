package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.anilist.AnilistParser
import com.ghostreborn.akirareborn.database.AnilistDatabase
import com.ghostreborn.akirareborn.fragment.AnimeFragment.Companion.allAnimeID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SaveAnimeFragment : Fragment() {

    lateinit var spinner: Spinner
    lateinit var progressEditText: EditText
    lateinit var progressAddButton: Button
    lateinit var progressDeleteButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_anime, container, false)
        spinner = view.findViewById(R.id.anime_status_spinner)
        progressEditText = view.findViewById(R.id.anime_progress_edit_text)
        progressAddButton = view.findViewById(R.id.anime_progress_add_button)
        progressDeleteButton = view.findViewById(R.id.anime_progress_delete_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getProgress()

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.anime_status,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        progressAddButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val anilistID = AllAnimeParser().anilistWithAllAnimeID(allAnimeID)
                AnilistParser().saveAnime(
                    anilistID,
                    spinner.selectedItem.toString(),
                    progressEditText.text.toString(),
                    requireContext()
                )
            }
        }

        progressDeleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val instance = Room.databaseBuilder(
                    requireContext(),
                    AnilistDatabase::class.java,
                    "Akira"
                ).build()
                val anilist = instance.anilistDao().findByAllAnimeID(allAnimeID)
                if (anilist != null) {
                    AnilistParser().deleteAnime(
                        anilist.id,
                        requireContext()
                    )
                }
            }
        }
    }

    private fun getProgress() {
        CoroutineScope(Dispatchers.IO).launch {
            val instance = Room.databaseBuilder(
                requireContext(),
                AnilistDatabase::class.java,
                "Akira"
            ).build()
            val anilist = instance.anilistDao().findByAllAnimeID(allAnimeID)
            withContext(Dispatchers.Main) {
                if (anilist != null) {
                    progressEditText.setText(anilist.progress)
                }
            }
        }
    }

}