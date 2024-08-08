package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.Constants.allMangaID
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allManga.AllMangaParser
import com.ghostreborn.akira.anilist.AnilistParser
import com.ghostreborn.akira.database.AnilistDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SaveMangaFragment : Fragment() {

    private lateinit var progressEditText: EditText
    private lateinit var progressAddButton: Button
    private lateinit var progressDeleteButton: Button
    private lateinit var plusButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_manga, container, false)
        progressEditText = view.findViewById(R.id.manga_progress_edit_text)
        progressAddButton = view.findViewById(R.id.manga_progress_add_button)
        progressDeleteButton = view.findViewById(R.id.manga_progress_delete_button)
        plusButton = view.findViewById(R.id.manga_plus_button)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getProgress()

        plusButton.setOnClickListener { updateValue(1) }

        progressAddButton.setOnClickListener {
            handleAddProgress()
        }

        progressDeleteButton.setOnClickListener {
            handleDeleteProgress()
        }
    }

    fun updateValue(increment: Int) {
        val currentValue = progressEditText.text.toString().toIntOrNull() ?: 1
        val newValue = (currentValue + increment).coerceAtLeast(1)
        progressEditText.setText(newValue.toString())
    }

    private fun handleAddProgress() {
        CoroutineScope(Dispatchers.IO).launch {

            val anilistID = AllMangaParser().anilistIdWithAllMangaID(allMangaID)
            var saved = false
            if (anilistID != "null") {
                saved = AnilistParser().saveAnime(
                    anilistID,
                    "CURRENT",
                    progressEditText.text.toString(),
                    requireContext(),
                    true
                )
            }
            withContext(Dispatchers.Main) {
                if (saved) {
                    Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Unable to save!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleDeleteProgress() {
        CoroutineScope(Dispatchers.IO).launch {
            val instance = Room.databaseBuilder(
                requireContext(),
                AnilistDatabase::class.java,
                "Akira"
            ).build()
            instance.anilistDao().findByAllAnimeID(allMangaID).let {
                if (it != null) {
                    AnilistParser().deleteAnime(it.id, requireContext())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_SHORT).show()
                    }
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
            instance.anilistDao().findByAllAnimeID(allMangaID).let { anilist ->
                withContext(Dispatchers.Main) {
                    if (anilist != null) {
                        val text = "Update"
                        progressAddButton.text = text
                        progressEditText.setText(anilist.progress)
                        Constants.animeEpisode = anilist.progress
                    }
                }
            }
        }
    }

}