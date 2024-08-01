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
import com.ghostreborn.akirareborn.anilist.AnilistNetwork
import com.ghostreborn.akirareborn.anilist.AnilistParser
import com.ghostreborn.akirareborn.database.AnilistDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.anime_status,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        progressAddButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val allAnimeID = "ReooPAxPMsHM4KPMY"
                val db = Room.databaseBuilder(
                    requireContext(),
                    AnilistDatabase::class.java,
                    "Akira"
                ).build()
                val anilist = db.anilistDao().findByAllAnimeID(allAnimeID)
                AnilistParser().saveAnime(
                    anilist.malID,
                    spinner.selectedItem.toString(),
                    progressEditText.text.toString(),
                    requireContext()
                )
            }
        }

        progressDeleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val allAnimeID = "ReooPAxPMsHM4KPMY"
                val db = Room.databaseBuilder(
                    requireContext(),
                    AnilistDatabase::class.java,
                    "Akira"
                ).build()
                val anilist = db.anilistDao().findByAllAnimeID(allAnimeID)
                AnilistNetwork().deleteAnime(anilist.id)
            }
        }
    }

}