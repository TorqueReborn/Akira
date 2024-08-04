package com.ghostreborn.akira.ui

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import com.ghostreborn.akira.adapter.AkiraTouchListener


class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val scaleGestureDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private var scaleFactor = 1f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private var isZooming = false
    private var customTouchListener: AkiraTouchListener? = null

    init {
        scaleGestureDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    isZooming = true
                    val scale = detector.scaleFactor
                    scaleFactor *= scale
                    scaleFactor =
                        0.1f.coerceAtLeast(scaleFactor.coerceAtMost(5.0f))

                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    val focusX = detector.focusX
                    val focusY = detector.focusY
                    val dx = (focusX - width / 2) * (1 - scale)
                    val dy = (focusY - height / 2) * (1 - scale)
                    translationX += dx
                    translationY += dy

                    return true
                }
            })

        gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (scaleFactor < 1.5f) {
                        zoomIn(e.x, e.y)
                    } else {
                        zoomOut(e.x, e.y)
                    }
                    return true
                }
            })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = !isZooming
                isZooming = false
                customTouchListener?.setSwipeEnabled(!isDragging)
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY

                    translationX += dx
                    translationY += dy

                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                customTouchListener?.setSwipeEnabled(true)
            }
        }

        return true
    }

    private fun zoomIn(x: Float, y: Float) {
        scaleFactor = 1.5f
        scaleX = scaleFactor
        scaleY = scaleFactor
        val dx = (x - width / 2) * (1 - 1.5f)
        val dy = (y - height / 2) * (1 - 1.5f)
        translationX += dx
        translationY += dy
    }

    private fun zoomOut(x: Float, y: Float) {
        scaleFactor = 1f // Reset to default scale
        scaleX = scaleFactor
        scaleY = scaleFactor
        val dx = (x - width / 2) * (1 - 1f)
        val dy = (y - height / 2) * (1 - 1f)
        translationX += dx
        translationY += dy
    }

}