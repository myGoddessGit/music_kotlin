package com.music.kotlinqq.view.main.history

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
import com.music.kotlinqq.adapter.HistoryAdapter
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.fragment.AttachFragment
import com.music.kotlinqq.bean.HistorySong
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.event.SongHistoryEvent
import com.music.kotlinqq.service.PlayerService
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.fragment_love.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal

/**
 * @author cyl
 * @date 2020/9/25
 */
class HistoryFragment : AttachFragment(){

    private var mHistoryList : MutableList<HistorySong> = ArrayList()
    private val mAdapter by lazy { HistoryAdapter(mHistoryList, attachActivity) }
    private var mPlayStatusBinder : PlayerService.PlayStatusBinder? = null
    private val connection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayStatusBinder = service as PlayerService.PlayStatusBinder
            mPlayStatusBinder!!.getHistoryList()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_love, container, false)
        EventBus.getDefault().register(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // start service
        val playIntent = Intent(attachActivity, PlayerService::class.java)
        attachActivity.bindService(playIntent, connection, Context.BIND_AUTO_CREATE)
        tv_title.text = "最近播放"
        showSongList()
        onClick()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event : SongHistoryEvent){
        mAdapter.notifyDataSetChanged()
    }

    private fun showSongList(){
        mHistoryList = orderList(LitePal.findAll(HistorySong::class.java))
        recycler_love_songs.layoutManager = layoutManager
        recycler_love_songs.adapter = mAdapter
    }
    private fun onClick(){
        mAdapter.setOnItemClickListener {
            val history = mHistoryList[it]
            val song = Song()
            song.songId = history.songId
            song.songName = history.name
            song.singer = history.singer
            song.isOnline = history.isOnline
            song.url = history.url
            song.imgUrl = history.pic
            song.position = it
            song.duration = history.duration
            song.mediaId = history.mediaId
            song.listType = Constant.LIST_TYPE_HISTORY
            FileUtil.saveSong(song)
            mPlayStatusBinder!!.play(Constant.LIST_TYPE_HISTORY)
        }
        iv_back.setOnClickListener {
            attachActivity.supportFragmentManager.popBackStack()
        }
    }

    private fun orderList(tempList : MutableList<HistorySong>) : MutableList<HistorySong>{
        val historyList = ArrayList<HistorySong>()
        historyList.clear()
        for (i in tempList.indices.reversed()){
            historyList.add(tempList[i])
        }
        return historyList
    }

    override fun onDestroy() {
        super.onDestroy()
        attachActivity.unbindService(connection)
        EventBus.getDefault().unregister(this)
    }
}