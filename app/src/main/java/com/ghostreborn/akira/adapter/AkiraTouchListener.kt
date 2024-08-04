package com.ghostreborn.akira.adapter

import android.view.MotionEvent
import android.view.View
import androidx.viewpager2.widget.ViewPager2

class AkiraTouchListener (viewPager2: ViewPager2) : View.OnTouchListener {

    private var isSwipeEnabled = true

    init {
        viewPager2.setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return if (isSwipeEnabled) {
            false // Let the ViewPager2 handle the touch event
        } else {
            true // Block touch events to prevent swiping
        }
    }

    fun setSwipeEnabled(enabled: Boolean) {
        isSwipeEnabled = enabled
    }
}