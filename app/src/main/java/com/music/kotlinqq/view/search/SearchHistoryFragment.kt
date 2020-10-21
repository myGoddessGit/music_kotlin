package com.music.kotlinqq.view.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.SpeedDialog.dialog.SpeedDialog
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.SearchHistoryAdapter
import com.music.kotlinqq.base.fragment.AttachFragment
import com.music.kotlinqq.bean.SearchHistory
import com.music.kotlinqq.callback.OnDeleteClickListener
import com.music.kotlinqq.callback.OnFooterClickListener
import com.music.kotlinqq.callback.OnItemClickListener
import kotlinx.android.synthetic.main.fragment_search_history.*
import org.litepal.LitePal

/**
 * @author cyl
 * @date 2020/9/25
 */
class SearchHistoryFragment : AttachFragment(){

    private var mSearchHistoryList : MutableList<SearchHistory>? = null
    private var mTempList : MutableList<SearchHistory>? = null
    private val mAdapter by lazy { SearchHistoryAdapter(mSearchHistoryList!!) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       return inflater.inflate(R.layout.fragment_search_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showHistory()
        onClick()
    }

    private fun showHistory(){
        mSearchHistoryList = ArrayList()
        mTempList = ArrayList()
        changeList()
        recycler_seek_history.layoutManager = layoutManager
        recycler_seek_history.adapter = mAdapter
    }

    private fun onClick(){
        mAdapter.setFooterClickListener(object : OnFooterClickListener{
            override fun onClick() {
                val speedDialog = SpeedDialog(attachActivity)
                speedDialog.setTitle("删除")
                    .setMessage("确定清空所有的搜索历史吗?")
                    .setSureClickListener {
                        LitePal.deleteAll(SearchHistory::class.java)
                        recycler_seek_history.visibility = View.GONE
                    }
                    .show()
            }
        })
        mAdapter.setOnDeleteClickListener(object : OnDeleteClickListener {
            override fun onClick(position: Int) {
                val searchHistory = mSearchHistoryList!![position]
                if (searchHistory.isSaved){
                    searchHistory.delete()
                }
                mTempList = LitePal.findAll(SearchHistory::class.java)
                changeList()
                mAdapter.notifyDataSetChanged()
            }
        })
        mAdapter.setOnItemClickListener(object : OnItemClickListener{
            override fun onClick(position: Int) {
                (parentFragment as AlbumContentFragment.SearchFragment).setSeekEdit(mSearchHistoryList!![position].history)
            }
        })
    }

    private fun changeList(){
        mSearchHistoryList?.clear()
        mTempList = LitePal.findAll(SearchHistory::class.java)
        if (mTempList?.size == 0){
            recycler_seek_history.visibility = View.INVISIBLE
        } else {
            recycler_seek_history.visibility = View.VISIBLE
        }
        for (i in mTempList?.indices!!.reversed()){
            mSearchHistoryList?.add(mTempList!![i])
        }
    }
}