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
import com.ghostreborn.akira.Constants.allAnimeID
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.ghostreborn.akira.anilist.AnilistParser
import com.ghostreborn.akira.database.AnilistDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SaveAnimeFragment : Fragment() {

    private lateinit var progressEditText: EditText
    private lateinit var progressAddButton: Button
    private lateinit var progressDeleteButton: Button
    private lateinit var plusButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_save, container, false).apply {
            progressEditText = findViewById(R.id.anime_progress_edit_text)
            progressAddButton = findViewById(R.id.anime_progress_add_button)
            progressDeleteButton = findViewById(R.id.anime_progress_delete_button)
            plusButton = findViewById(R.id.anime_plus_button)
        }
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

            val anilistID = AllAnimeParser().anilistWithAllAnimeID(allAnimeID)
            val saved = AnilistParser().saveAnime(
                anilistID,
                "CURRENT",
                progressEditText.text.toString(),
                requireContext(),
                false,
                requireActivity()
            )
            withContext(Dispatchers.Main) {
                if (saved){
                    Toast.makeText(requireContext(), "Saved!", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(), "Login to Anilist by swiping from home screen to add or update progress!", Toast.LENGTH_SHORT).show()
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
            instance.anilistDao().findByAllAnimeID(allAnimeID).let {
                if (it != null) {
                    AnilistParser().deleteAnime(it.id, requireContext(), requireActivity())
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