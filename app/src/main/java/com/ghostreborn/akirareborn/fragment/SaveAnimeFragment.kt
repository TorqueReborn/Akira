package com.ghostreborn.akirareborn.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.ghostreborn.akirareborn.Constants
import com.ghostreborn.akirareborn.Constants.allAnimeID
import com.ghostreborn.akirareborn.R
import com.ghostreborn.akirareborn.allAnime.AllAnimeParser
import com.ghostreborn.akirareborn.anilist.AnilistParser
import com.ghostreborn.akirareborn.database.AnilistDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SaveAnimeFragment : Fragment() {

    private lateinit var spinner: Spinner
    private lateinit var progressEditText: EditText
    private lateinit var progressAddButton: Button
    private lateinit var progressDeleteButton: Button
    private lateinit var plusButton: Button
    private lateinit var minusButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_save_anime, container, false).apply {
            spinner = findViewById(R.id.anime_status_spinner)
            progressEditText = findViewById(R.id.anime_progress_edit_text)
            progressAddButton = findViewById(R.id.anime_progress_add_button)
            progressDeleteButton = findViewById(R.id.anime_progress_delete_button)
            plusButton = findViewById(R.id.anime_plus_button)
            minusButton = findViewById(R.id.anime_minus_button)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        getProgress()

        plusButton.setOnClickListener { updateValue(1) }
        minusButton.setOnClickListener { updateValue(-1) }

        progressAddButton.setOnClickListener {
            handleAddProgress()
        }

        progressDeleteButton.setOnClickListener {
            handleDeleteProgress()
        }
    }

    fun updateValue(increment: Int) {
        val currentValue = progressEditText.text.toString().toIntOrNull() ?: 0
        val newValue = (currentValue + increment).coerceAtLeast(0)
        progressEditText.setText(newValue.toString())
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.anime_status,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun handleAddProgress() {
        CoroutineScope(Dispatchers.IO).launch {
            val anilistID = AllAnimeParser().anilistWithAllAnimeID(allAnimeID)
            AnilistParser().saveAnime(
                anilistID,
                spinner.selectedItem.toString(),
                progressEditText.text.toString(),
                requireContext()
            )
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
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
            instance.anilistDao().findByAllAnimeID(allAnimeID).let {
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
            instance.anilistDao().findByAllAnimeID(allAnimeID).let { anilist ->
                withContext(Dispatchers.Main) {
                    if (anilist!=null){
                        progressEditText.setText(anilist.progress)
                        Constants.animeEpisode = anilist.progress
                    }
                }
            }
        }
    }
}