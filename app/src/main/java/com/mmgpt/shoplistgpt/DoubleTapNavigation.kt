package com.mmgpt.shoplistgpt

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

/**
 * Calls [onRowDoubleTapped] with the adapter position of the row that was
 * double-tapped. The callback returns true if it handled the double-tap.
 */
fun RecyclerView.setOnRowDoubleTapListener(onRowDoubleTapped: (position: Int) -> Boolean) {
    val gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Touch events reach this listener in the RecyclerView's own coordinates
            val child = findChildViewUnder(e.x, e.y) ?: return false
            val position = getChildAdapterPosition(child)
            return position != RecyclerView.NO_POSITION && onRowDoubleTapped(position)
        }
    })

    setOnTouchListener { _, event ->
        gestureDetector.onTouchEvent(event)
        false
    }
}

/** Shows the given page of the main ViewPager. */
fun Fragment.openPage(page: Int) {
    requireActivity().findViewById<ViewPager2>(R.id.viewPager).currentItem = page
}
