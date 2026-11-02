package com.mmgpt.shoplistgpt

import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager2.widget.ViewPager2
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

/**
 * Double-tapping a category on the category list (Plan) or summary (Shop)
 * should open that category's page in the ViewPager.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w411dp-h914dp")   // roughly the Medium Phone AVD
class CategoryNavigationTest {

    private lateinit var activity: MainActivity
    private lateinit var viewModel: ShoppingListViewModel
    private lateinit var pager: ViewPager2

    private val fruit = ShoppingCategory(10, "Fruit", emptyList())
    private val veg = ShoppingCategory(11, "Veg", emptyList())
    private val dairy = ShoppingCategory(12, "Dairy", emptyList())
    private val bakery = ShoppingCategory(13, "Bakery", emptyList())

    @Before
    fun setUp() {
        // AndroidViewModelFactory caches the first test's Application in a static, so
        // without this every test would load the previous test's saved shopping list.
        ViewModelProvider.AndroidViewModelFactory::class.java
            .getDeclaredField("_instance")   // field name in lifecycle 2.8.x
            .apply { isAccessible = true }
            .set(null, null)

        activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        viewModel = ViewModelProvider(activity)[ShoppingListViewModel::class.java]
        pager = activity.findViewById(R.id.viewPager)
    }

    // ---- Shop mode ----

    @Test
    fun shop_afterImportIntoEmptyList_firstPageShowsFirstCategory() {
        openShop()                                  // summary created at page 0
        viewModel.setCategories(listOf(fruit, veg, dairy))   // same as Import
        idle()

        assertEquals(0, pager.currentItem)
        assertEquals("Fruit", displayedHeading())
    }

    @Test
    fun shop_afterImportIntoEmptyList_doubleTapFirstCategoryOpensIt() {
        openShop()
        viewModel.setCategories(listOf(fruit, veg, dairy))
        goToPage(3)                                 // summary is the last page
        assertEquals("Summary", displayedHeading())

        doubleTapRow(0)

        assertEquals("Fruit", displayedHeading())
    }

    @Test
    fun shop_afterImportIntoEmptyList_doubleTapSecondCategoryOpensIt() {
        // Contrast case: categories whose page never held the summary work.
        openShop()
        viewModel.setCategories(listOf(fruit, veg, dairy))
        goToPage(3)

        doubleTapRow(1)

        assertEquals("Veg", displayedHeading())
    }

    @Test
    fun shop_afterImportOfMoreCategories_doubleTapCategoryAtOldSummaryPositionOpensIt() {
        viewModel.setCategories(listOf(fruit, veg))
        openShop()
        goToPage(2)                                 // summary visited at page 2
        viewModel.setCategories(listOf(fruit, veg, dairy, bakery))
        goToPage(4)                                 // new last page
        assertEquals("Summary", displayedHeading())

        doubleTapRow(2)

        assertEquals("Dairy", displayedHeading())
    }

    // ---- Plan mode ----

    @Test
    fun plan_afterAddingCategory_doubleTapOpensItsPage() {
        viewModel.setCategories(listOf(fruit, veg))
        openPlan()
        viewModel.addCategory(2, "Bakery")
        idle()

        doubleTapRow(3)                             // row 0 is the "Add" placeholder

        assertEquals(3, pager.currentItem)
        assertEquals("Bakery", displayedHeading())
    }


    // ---- helpers ----

    private fun openShop() {
        activity.findViewById<View>(R.id.btnShop).performClick()
        idle()
    }

    private fun openPlan() {
        activity.findViewById<View>(R.id.btnPlan).performClick()
        idle()
    }

    private fun goToPage(page: Int) {
        idle()
        pager.setCurrentItem(page, false)
        idle()
    }

    private fun idle() {
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
    }

    private fun currentPageView(): View {
        val pagerRv = pager.getChildAt(0) as RecyclerView
        val holder = pagerRv.findViewHolderForAdapterPosition(pager.currentItem)
            ?: error("Page ${pager.currentItem} is not laid out")
        return holder.itemView
    }

    private fun displayedHeading(): String =
        currentPageView().findViewById<TextView>(R.id.tvHeading).text.toString()

    /** Double-taps the name of a row in the current page's list, using screen coordinates. */
    private fun doubleTapRow(row: Int) {
        val list = currentPageView().findViewById<RecyclerView>(R.id.recyclerView)
        val target = list.findViewHolderForAdapterPosition(row)?.itemView
            ?.findViewById<View>(R.id.tvName)
            ?: error("Row $row is not laid out")
        val loc = IntArray(2)
        target.getLocationOnScreen(loc)
        val x = loc[0] + target.width / 2f
        val y = loc[1] + target.height / 2f

        val t = SystemClock.uptimeMillis()
        dispatch(t, t, MotionEvent.ACTION_DOWN, x, y)
        dispatch(t, t + 50, MotionEvent.ACTION_UP, x, y)
        dispatch(t + 150, t + 150, MotionEvent.ACTION_DOWN, x, y)
        dispatch(t + 150, t + 200, MotionEvent.ACTION_UP, x, y)
        idle()
    }

    private fun dispatch(downTime: Long, eventTime: Long, action: Int, x: Float, y: Float) {
        val event = MotionEvent.obtain(downTime, eventTime, action, x, y, 0)
        activity.dispatchTouchEvent(event)
        event.recycle()
    }
}
