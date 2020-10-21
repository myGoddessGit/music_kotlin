package com.music.kotlinqq.view.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.TabAdapter
import com.music.kotlinqq.base.fragment.AttachFragment
import kotlinx.android.synthetic.main.fragment_content.*

/**
 * @author cyl
 * @date 2020/9/25
 */
class ContentFragment : AttachFragment(){

    private var mTitleList : MutableList<String> = ArrayList()
    private var mFragments : MutableList<Fragment> = ArrayList()
    private val mAdapter by lazy { TabAdapter(childFragmentManager, mFragments, mTitleList) }
    private val mTitles  = arrayOf("歌曲", "专辑")
    private val mTypes = arrayOf("song", "album")
    private var mSeek : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_content, container, false)
        if (arguments != null){
            mSeek = arguments?.getString(SearchContentFragment.SEEK_KEY)
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initTab()
    }

    private fun initTab(){
        for (i in 0 until mTitles.size){
            mTitleList.add(mTitles[i])
            mFragments.add(SearchContentFragment.newInstance(mSeek!!, mTypes[i]))
        }
        page.adapter = mAdapter
        tab_layout.setupWithViewPager(page)
    }

    companion object {
        fun newInstance(seek : String) : Fragment{
            val fragment = ContentFragment()
            val bundle = Bundle()
            bundle.putString(SearchContentFragment.SEEK_KEY, seek)
            fragment.arguments = bundle
            return fragment
        }
    }
}