package com.music.kotlinqq.view.main.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.TabAdapter
import com.music.kotlinqq.base.fragment.AttachFragment
import com.music.kotlinqq.bean.DownloadInfo
import kotlinx.android.synthetic.main.fragment_download.*
import kotlin.collections.ArrayList

/**
 * @author cyl
 * @date 2020/9/24
 */
class DownloadFragment : AttachFragment(){

    private var mTitleList : MutableList<String> = ArrayList()
    private var mFragments : MutableList<Fragment> = ArrayList()
    private val mAdapter by lazy {TabAdapter(childFragmentManager, mFragments, mTitleList)}
    private val mTitles = arrayOf("已下载", "正在下载")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_download, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initTab()
        onClick()
    }

    private fun initTab(){
        mTitleList = ArrayList()
        mFragments = ArrayList()
        mTitleList.addAll(mTitles)
        mFragments.add(DownloadMusicFragment())
        mFragments.add(DownloadingFragment())
        page.adapter = mAdapter
        tabLayout.setupWithViewPager(page)
    }

    private fun onClick(){
        backIv.setOnClickListener {
            attachActivity.supportFragmentManager.popBackStack()
        }
    }
}