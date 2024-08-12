package com.ghostreborn.akira.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R
import com.ghostreborn.akira.allAnime.AllAnimeParser
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsFragment : Fragment() {

    private lateinit var animeBannerImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_details, container, false)
        animeBannerImage = view.findViewById(R.id.anime_banner_image)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            val details = AllAnimeParser().animeDetails(Constants.allAnimeID)
            withContext(Dispatchers.Main) {
                val banner = details.thumbnail

                Log.e("TAG", banner)

                if (banner.isNotEmpty() && banner != "null") {
                    Picasso.get().load(banner).into(animeBannerImage)
                } else {
                    Toast.makeText(context, "No banner found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}