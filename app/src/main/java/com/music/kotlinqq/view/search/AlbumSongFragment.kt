package com.music.kotlinqq.view.search

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.florent37.materialviewpager.MaterialViewPagerHelper
import com.github.florent37.materialviewpager.header.MaterialViewPagerHeaderDecorator
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.AlbumSongAdapter
import com.music.kotlinqq.adapter.SongAdapter
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.fragment.BaseMvpFragment
import com.music.kotlinqq.bean.AlbumSong
import com.music.kotlinqq.bean.DownloadSong
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.contract.IAlbumSongContract
import com.music.kotlinqq.event.SongAlbumEvent
import com.music.kotlinqq.presenter.AlbumSongPresenter
import com.music.kotlinqq.service.PlayerService
import com.music.kotlinqq.util.CommonUtil
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.fragment_album_recycler.*
import kotlinx.android.synthetic.main.fragment_album_song.*
import kotlinx.android.synthetic.main.fragment_album_song.avi
import kotlinx.android.synthetic.main.fragment_album_song.tv_loading
import kotlinx.android.synthetic.main.fragment_album_song.iv_network_error
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import java.lang.StringBuilder

/**
 * @author cyl
 * @date 2020/9/22
 */
class AlbumSongFragment : BaseMvpFragment<AlbumSongPresenter>(), IAlbumSongContract.View {

    companion object {
        const val TYPE_KEY = "type_key"
        const val ALBUM_SONG = 0
        const val ALBUM_INFORMATION = 1
        fun newInstance(type : Int, id : String, publicTime : String): Fragment {
            val fragment = AlbumSongFragment()
            val bundle = Bundle()
            bundle.putInt(TYPE_KEY, type)
            bundle.putString(Constant.ALBUM_ID_KEY, id)
            bundle.putString(Constant.PUBLIC_TIME_KEY, publicTime)
            fragment.arguments = bundle
            return fragment
        }
    }
    private val mPresenters by lazy {  AlbumSongPresenter() }
    private var mId : String? = null
    private var mType: Int? = null
    private var mPublicTime : String? = null
    private var mAdapter : AlbumSongAdapter? = null
    private var playIntent = Intent()
    private var mPlayStatusBinder : PlayerService.PlayStatusBinder? = null
    private val connection = object : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayStatusBinder = service as PlayerService.PlayStatusBinder
        }
    }
    override fun loadData() {
        mPresenters.attachView(this)
        mPresenters.getAlbumDetail(mId!!, mType!!)
    }

    override fun getLayoutId(): Int {
        return 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getBundle()
        EventBus.getDefault().register(this)
        val view : View
        when (mType) {
            ALBUM_SONG -> {
                view = inflater.inflate(R.layout.fragment_album_recycler, container, false)
                LitePal.getDatabase()
            }
            else -> {
                view = inflater.inflate(R.layout.fragment_album_song, container, false)
            }
        }
        return view
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onSongAlbumEvent(event : SongAlbumEvent){
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        when (mType) {
            ALBUM_SONG -> {
                playIntent = Intent(attachActivity, PlayerService::class.java)
                mActivity.bindService(playIntent, connection, Context.BIND_AUTO_CREATE)
            }
            else -> {
                MaterialViewPagerHelper.registerScrollView(attachActivity, scrollView)
            }
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        attachActivity.unbindService(connection)
        super.onDestroyView()
    }

    private fun getBundle(){
        if (arguments != null){
            mType = arguments!!.getInt(TYPE_KEY)
            mId = arguments!!.getString(Constant.ALBUM_ID_KEY)
            mPublicTime = arguments!!.getString(Constant.PUBLIC_TIME_KEY)
        }
    }

    override fun getPresenter(): AlbumSongPresenter? {
        return null
    }

    override fun setAlbumSongList(songList: MutableList<AlbumSong.DataBean.ListBean>) {
        normalView.layoutManager = LinearLayoutManager(attachActivity)
        mAdapter = AlbumSongAdapter(songList)
        normalView.addItemDecoration(MaterialViewPagerHeaderDecorator())
        normalView.adapter = mAdapter
        mAdapter!!.setSongClick{
            tag: Int ->
            val dataBean = songList[tag]
            val song = Song()
            song.songId = dataBean.songmid
            song.singer = getSinger(dataBean)
            song.songName = dataBean.songname
            song.position = tag
            song.duration = dataBean.interval.toLong()
            song.isOnline = true
            song.listType = Constant.LIST_TYPE_ONLINE
            song.imgUrl = Api.ALBUM_PIC + dataBean.albummid + Api.JPG
            song.url = null
            song.mediaId = dataBean.strMediaMid
            // 判断是否下载
            song.isDownload = (LitePal.where("songId=?",dataBean.songmid).find(DownloadSong::class.java).size != 0)
            FileUtil.saveSong(song)
            mPlayStatusBinder!!.play(Constant.LIST_TYPE_ONLINE)
        }
    }

    override fun initOtherView() {

    }

    override fun showAlbumSongError() {
        CommonUtil.showToast(attachActivity, "获取专辑消息失败")
    }

    override fun showAlbumMessage(name: String, language: String, company: String, albumType: String, desc: String) {
        tv_album_name.text = name
        tv_language.text = language
        tv_company.text = company
        tv_desc.text = desc
        tv_public_time.text = mPublicTime
        tv_album_type.text = albumType
    }

    override fun showLoading() {
        avi.show()
    }

    override fun hideLoading() {
        avi.hide()
        tv_loading.visibility = View.GONE
        if (mType == ALBUM_SONG){
            normalView.visibility = View.VISIBLE
        } else {
            scrollView.visibility = View.VISIBLE
        }
        iv_network_error.visibility = View.GONE
    }

    override fun showNetError() {
        tv_loading.visibility = View.GONE
        avi.hide()
        iv_network_error.visibility = View.VISIBLE
    }

    private fun getSinger(dataBean : AlbumSong.DataBean.ListBean) : String {
        val singer = StringBuilder(dataBean.singer[0].name)
        for (i in 1..dataBean.singer.size){
            singer.append("、").append(dataBean.singer[i].name)
        }
        return singer.toString()
    }

}