package com.mmgpt.shoplistgpt

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.mmgpt.shoplistgpt.PlanCategoryFragment
import com.mmgpt.shoplistgpt.PlanCategoriesFragment

class CategoryPagerAdapter(
    activity: FragmentActivity,
    private val viewModel: ShoppingListViewModel,
    private val planMode: Boolean = false
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int =
        (viewModel.categories.value?.size ?: 0) + 1

    override fun createFragment(position: Int): Fragment =
        if (planMode) {
            if (position == 0) {
                PlanCategoriesFragment()
            } else {
                PlanCategoryFragment.newInstance(position - 1)
            }
        } else {
            val size = viewModel.categories.value?.size ?: 0
            if (position == size) {
                ShopCategoriesFragment()
            } else {
                CategoryFragment.newInstance(position)
            }
        }

    // FragmentStateAdapter caches fragments by item ID (the position by default).
    // In Shop mode the summary moves to the end when categories are added, so it
    // needs its own ID or the old position keeps showing a stale summary.
    override fun getItemId(position: Int): Long {
        val size = viewModel.categories.value?.size ?: 0
        return if (!planMode && position == size) SUMMARY_ID else position.toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        if (planMode) return super.containsItem(itemId)
        val size = viewModel.categories.value?.size ?: 0
        return itemId == SUMMARY_ID || itemId in 0 until size
    }

    companion object {
        private const val SUMMARY_ID = -1L
    }
}
