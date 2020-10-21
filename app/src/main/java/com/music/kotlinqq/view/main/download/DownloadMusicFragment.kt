package com.music.kotlinqq.view.main.download

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.apkfuns.logutils.LogUtils
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.DownloadSongAdapter
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.fragment.AttachFragment
import com.music.kotlinqq.bean.DownloadSong
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.callback.OnItemClickListener
import com.music.kotlinqq.event.DownloadEvent
import com.music.kotlinqq.event.SongDownloadedEvent
import com.music.kotlinqq.service.PlayerService
import com.music.kotlinqq.util.DownloadUtil
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.fragment_download_music.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author cyl
 * @date 2020/9/24
 */
class DownloadMusicFragment : AttachFragment(){

    private var mDownloadSongList : MutableList<DownloadSong> = ArrayList()
    private val mAdapter by lazy { DownloadSongAdapter(mDownloadSongList)}
    private var mPlayStatusBinder : PlayerService.PlayStatusBinder? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayStatusBinder = service as PlayerService.PlayStatusBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_download_music, container, false)
        EventBus.getDefault().register(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val playIntent = Intent(attachActivity, PlayerService::class.java)
        attachActivity.bindService(playIntent, connection, Context.BIND_AUTO_CREATE)
        showSongList()
    }

    private fun showSongList(){
        mDownloadSongList = orderList(DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE).toMutableList())
        songRecycle.layoutManager = layoutManager
        songRecycle.adapter = mAdapter
        mAdapter.setOnItemClickListener(object : OnItemClickListener{
            override fun onClick(position: Int) {
                val downloadSong = mDownloadSongList[position]
                val song = Song()
                song.songId = downloadSong.songId
                song.songName = downloadSong.name
                song.singer = downloadSong.singer
                song.isOnline = false
                song.url = downloadSong.url
                Log.i("thisDownload", downloadSong.url)
                song.imgUrl = downloadSong.pic
                song.position = position
                song.duration = position.toLong()
                song.listType = Constant.LIST_TYPE_DOWNLOAD
                song.mediaId = downloadSong.mediaId
                song.isDownload = true
                FileUtil.saveSong(song)
                mPlayStatusBinder!!.play(Constant.LIST_TYPE_DOWNLOAD)
            }
        })
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public fun onDownloadSuccessEvent(event : DownloadEvent){
        if (event.getDownloadStatus() == Constant.TYPE_DOWNLOAD_SUCCESS){
            mDownloadSongList.clear()
            mDownloadSongList.addAll(orderList(DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE).toMutableList()))
            mAdapter.notifyDataSetChanged()
        }
    }
    @Subscribe (threadMode = ThreadMode.MAIN)
    public fun onSongDownloadedEvent(event : SongDownloadedEvent){
        mAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        attachActivity.unbindService(connection)
        EventBus.getDefault().unregister(this)
    }

    private fun orderList(tempList : MutableList<DownloadSong>) : MutableList<DownloadSong>{
        val loveList = ArrayList<DownloadSong>()
        loveList.clear()
        for (i in tempList.indices.reversed()){
            loveList.add(tempList[i])
        }
        return loveList
    }
}