package com.music.kotlinqq.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 * @author cyl
 * @date 2020/9/24
 */
class TabAdapter(fragmentManager: FragmentManager, private val mFragments : MutableList<Fragment>, private val mTitle : MutableList<String>) : FragmentPagerAdapter(fragmentManager){

    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }

    override fun getCount(): Int {
       return mFragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTitle[position]
    }

}