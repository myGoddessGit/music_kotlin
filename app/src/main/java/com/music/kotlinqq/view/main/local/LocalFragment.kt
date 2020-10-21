package com.music.kotlinqq.view.main.local

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.SongAdapter
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.fragment.BaseMvpFragment
import com.music.kotlinqq.bean.LocalSong
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.callback.OnItemClickListener
import com.music.kotlinqq.contract.ILocalContract
import com.music.kotlinqq.event.SongLocalEvent
import com.music.kotlinqq.presenter.LocalPresenter
import com.music.kotlinqq.service.PlayerService
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.fragment_local.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal

/**
 * @author cyl
 * @date 2020/9/24
 */
class LocalFragment : BaseMvpFragment<LocalPresenter>(), ILocalContract.View {

    private var mLocalSongList : MutableList<LocalSong> = ArrayList()
    private val mPresenters by lazy { LocalPresenter() }
    private val songAdapter by lazy { SongAdapter(mLocalSongList)}
    private var mPlayStatusBinder : PlayerService.PlayStatusBinder? = null
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayStatusBinder = service as PlayerService.PlayStatusBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onClick()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessageEvent(event : SongLocalEvent){
        songAdapter.notifyDataSetChanged()
        if (FileUtil.getSong() != null){
            layoutManager.scrollToPositionWithOffset(FileUtil.getSong()!!.position + 4, normalView.height)
        }
    }

    override fun loadData() {

    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_local
    }

    override fun getPresenter(): LocalPresenter {
        return mPresenters
    }

    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
//        normalView.layoutManager = LinearLayoutManager(attachActivity)
        registerAndBindReceive()
    }

    override fun initOtherView() {
        initLocalRecycler()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        attachActivity.unbindService(connection)
    }

    override fun showMusic(mp3InfoList: MutableList<LocalSong>) {
        mLocalSongList.clear()
        mLocalSongList.addAll(mp3InfoList)
        normalView.visibility = View.VISIBLE
        linear_empty.visibility = View.GONE
        normalView.layoutManager = layoutManager
        normalView.adapter = songAdapter
        songAdapter.setOnItemClickListener(object : OnItemClickListener{
            override fun onClick(position: Int) {
                val mp3Info = mLocalSongList[position]
                val song = Song()
                song.songName = mp3Info.name
                song.singer = mp3Info.singer
                song.url = mp3Info.url
                song.duration = mp3Info.duration
                song.position = position
                song.isOnline = false
                song.songId = mp3Info.songId
                song.listType = Constant.LIST_TYPE_LOCAL
                FileUtil.saveSong(song)
                mPlayStatusBinder?.play(Constant.LIST_TYPE_LOCAL)
            }
        })
    }

    override fun showErrorView() {
        showToast("本地音乐为空")
        normalView.visibility = View.GONE
        linear_empty.visibility = View.VISIBLE
    }

    private fun registerAndBindReceive(){
        val playIntent = Intent(attachActivity, PlayerService::class.java)
        attachActivity.bindService(playIntent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun initLocalRecycler(){
        mLocalSongList.clear()
        mLocalSongList.addAll(LitePal.findAll(LocalSong::class.java))
        if (mLocalSongList.size == 0){
            normalView.visibility = View.GONE
            linear_empty.visibility = View.VISIBLE
        } else {
            linear_empty.visibility = View.GONE
            normalView.visibility = View.VISIBLE
            normalView.layoutManager = layoutManager
            normalView.adapter = songAdapter
            if (FileUtil.getSong() != null){
                layoutManager.scrollToPositionWithOffset(FileUtil.getSong()!!.position - 4, normalView.height)
            }
            songAdapter.setOnItemClickListener(object : OnItemClickListener{
                override fun onClick(position: Int) {
                    val mp3Info = mLocalSongList!![position]
                    val song = Song()
                    song.songName = mp3Info.name
                    song.singer = mp3Info.singer
                    song.url = mp3Info.url
                    song.duration = mp3Info.duration
                    song.position = position
                    song.isOnline = false
                    song.songId = mp3Info.songId
                    song.listType = Constant.LIST_TYPE_LOCAL
                    FileUtil.saveSong(song)
                    mPlayStatusBinder?.play(Constant.LIST_TYPE_LOCAL)
                }
            })
        }
    }
    private fun onClick(){
        iv_find_local_song.setOnClickListener { mPresenters.getLocalMp3Info() }
        iv_back.setOnClickListener { attachActivity.supportFragmentManager.popBackStack() }
    }
}