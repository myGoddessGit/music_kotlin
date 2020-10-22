package com.music.kotlinqq.view.main.collection

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.LoveSongAdapter
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.fragment.AttachFragment
import com.music.kotlinqq.bean.Love
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.callback.OnItemClickListener
import com.music.kotlinqq.event.SongCollectionEvent
import com.music.kotlinqq.service.PlayerService
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.fragment_love.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal

/**
 * @author cyl
 * @date 2020/9/24
 */
class CollectionFragment : AttachFragment(){

    private var mLoveList : MutableList<Love>? = null
    private val mAdapter by lazy { LoveSongAdapter(mLoveList!!, attachActivity)}
    private var mPlayStatusBinder : PlayerService.PlayStatusBinder? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayStatusBinder = service as PlayerService.PlayStatusBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mView = inflater.inflate(R.layout.fragment_love, container, false)
        EventBus.getDefault().register(this)
        mLoveList = ArrayList()
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val playIntent = Intent(attachActivity, PlayerService::class.java)
        attachActivity.bindService(playIntent, connection, Context.BIND_AUTO_CREATE)
        showSongList()
        onClick()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        attachActivity.unbindService(connection)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(songCollectionEvent : SongCollectionEvent){
        mLoveList?.clear()
        mLoveList?.addAll(orderList(LitePal.findAll(Love::class.java)))
        mAdapter.notifyDataSetChanged()
        if (songCollectionEvent.isLove){
            if (FileUtil.getSong() != null){
                layoutManager.scrollToPositionWithOffset(FileUtil.getSong()?.position!! + 4, recycler_love_songs.height)
            }
        }
    }

    private fun showSongList(){
        recycler_love_songs.setHasFixedSize(true)
        mLoveList?.addAll(orderList(LitePal.findAll(Love::class.java)))
        recycler_love_songs.layoutManager = layoutManager
        mAdapter.setOnItemClickListener{
            tag: Int ->
            val love = mLoveList!![tag]
            val song = Song()
            song.songId = love.songId
            song.qqId = love.qqId
            song.songName = love.name
            song.singer = love.singer
            song.isOnline = love.isOnline
            song.url = love.url
            song.imgUrl = love.pic
            song.position = tag
            song.duration = love.duration
            song.mediaId = love.mediaId
            song.listType = Constant.LIST_TYPE_LOVE
            FileUtil.saveSong(song)
            mPlayStatusBinder?.play(Constant.LIST_TYPE_LOVE)
        }
    }

    private fun onClick(){
        iv_back.setOnClickListener {
            attachActivity.supportFragmentManager.popBackStack()
        }
    }

    private fun orderList(tempList : MutableList<Love>) : MutableList<Love>{
        val loveList = ArrayList<Love>()
        loveList.clear()
        for (i in tempList.indices.reversed()){
            loveList.add(tempList[i])
        }
        return loveList
    }
}