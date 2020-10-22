package com.music.kotlinqq.view.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.ExpandableListViewAdapter
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.fragment.AttachFragment
import com.music.kotlinqq.bean.AlbumCollection
import com.music.kotlinqq.bean.HistorySong
import com.music.kotlinqq.bean.LocalSong
import com.music.kotlinqq.bean.Love
import com.music.kotlinqq.callback.OnChildItemClickListener
import com.music.kotlinqq.event.AlbumCollectionEvent
import com.music.kotlinqq.event.SongListNumEvent
import com.music.kotlinqq.util.DownloadUtil
import com.music.kotlinqq.view.main.collection.CollectionFragment
import com.music.kotlinqq.view.main.download.DownloadFragment
import com.music.kotlinqq.view.main.history.HistoryFragment
import com.music.kotlinqq.view.main.local.LocalFragment
import com.music.kotlinqq.view.search.AlbumContentFragment
import kotlinx.android.synthetic.main.function.*
import kotlinx.android.synthetic.main.new_song_list.*
import kotlinx.android.synthetic.main.seek.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal

/**
 * @author cyl
 * @date 2020/9/25
 */
class MainFragment : AttachFragment(){

    private var mAlbumCollectionList : MutableList<MutableList<AlbumCollection>> = ArrayList()
    private var mLoveAlbumList : MutableList<AlbumCollection> = ArrayList()
    private val mGroupStrings = arrayOf("自建歌单", "收藏歌单")
    private var twoExpand = false
    private var mAdapter : ExpandableListViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linear_function.isFocusableInTouchMode = true
        showAlbumList()
        onClick()
    }

    private fun showAlbumList(){
        val aCollection = AlbumCollection()
        aCollection.albumName = "I Like"
        aCollection.singerName = "YearlingLight"
        mLoveAlbumList.add(aCollection)
        mAlbumCollectionList.add(mLoveAlbumList)
        mAlbumCollectionList.add(orderCollection(LitePal.findAll(AlbumCollection::class.java)))
        mAdapter = ExpandableListViewAdapter(attachActivity, mGroupStrings, mAlbumCollectionList)
        expand_lv_song_list.setAdapter(mAdapter)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onStart() {
        super.onStart()
        showMusicNum()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event : AlbumCollectionEvent){
        mAlbumCollectionList.clear()
        mAlbumCollectionList.add(mLoveAlbumList)
        mAlbumCollectionList.add(orderCollection(LitePal.findAll(AlbumCollection::class.java)))
        if (twoExpand){
            expand_lv_song_list.collapseGroup(1)
            expand_lv_song_list.expandGroup(1)
        } else {
            expand_lv_song_list.expandGroup(1)
            expand_lv_song_list.collapseGroup(1)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onSongListEvent(event : SongListNumEvent){
        when (event.type){
            Constant.LIST_TYPE_HISTORY -> {
                tv_history_num.text = LitePal.findAll(HistorySong::class.java).size.toString()
            }
            Constant.LIST_TYPE_LOCAL -> {
                tv_local_music_num.text = LitePal.findAll(LocalSong::class.java).size.toString()
            }
            Constant.LIST_TYPE_DOWNLOAD -> {
                tv_download_num.text = DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE).size.toString()
            }
        }
    }

    private fun onClick(){
        Log.i("MainList1", mAlbumCollectionList[0][0].albumPic + "1")
        linear_local_music.setOnClickListener { replaceFragment(LocalFragment()) }
        tv_seek.setOnClickListener { replaceFragment(AlbumContentFragment.SearchFragment()) }
        linear_collection.setOnClickListener { replaceFragment(CollectionFragment()) }
        downloadLinear.setOnClickListener { replaceFragment(DownloadFragment()) }
        linear_history.setOnClickListener { replaceFragment(HistoryFragment()) }
        expand_lv_song_list.setOnGroupExpandListener {if (it == 1)  twoExpand = true} // 展开
        expand_lv_song_list.setOnGroupCollapseListener { if (it == 1) twoExpand = false } // 收缩
        expand_lv_song_list.setOnGroupClickListener { _, _, _, _ ->  false}
        // 二级列表的点击效果
        mAdapter!!.setOnChildItemClickListener{
            tag1: Int, tag2: Int ->
            if (tag1 == 0 && tag2 == 0){
                replaceFragment(CollectionFragment())
            } else if (tag1 == 1){
                val albumCollection = mAlbumCollectionList[tag1][tag2]
                replaceFragment(AlbumContentFragment.newInstance(
                    albumCollection.albumId,
                    albumCollection.albumName,
                    albumCollection.albumPic,
                    albumCollection.singerName,
                    albumCollection.publicTime
                ))
            }
        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = attachActivity.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out, R.anim.slide_in_right, R.anim.slide_out_right)
        transaction.add(R.id.fragment_container, fragment)
        transaction.hide(this)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    // 显示数目
    private fun showMusicNum(){
        tv_local_music_num.text = LitePal.findAll(LocalSong::class.java).size.toString()
        tv_love_num.text = LitePal.findAll(Love::class.java).size.toString()
        tv_history_num.text = LitePal.findAll(HistorySong::class.java).size.toString()
        tv_download_num.text = DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE).size.toString()
    }

    // 倒序
    private fun orderCollection(tempList : MutableList<AlbumCollection>) : MutableList<AlbumCollection>{
        val mAlbumCollectionList = ArrayList<AlbumCollection>()
        for (i in tempList.indices.reversed()){
            mAlbumCollectionList.add(tempList[i])
        }
        return mAlbumCollectionList
    }
}