package com.ghostreborn.akira.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.ghostreborn.akira.R
import com.ghostreborn.akira.ui.ZoomableImageView

class MangaChapterAdapter(
    private val images: ArrayList<String>, private val viewPager2: ViewPager2
) : RecyclerView.Adapter<MangaChapterAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ZoomableImageView = itemView.findViewById(R.id.manga_chapter_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.manga_chapter_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setCustomTouchListener(AkiraTouchListener(viewPager2))
        Glide.with(holder.itemView.context).load(images[position]).into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size
}