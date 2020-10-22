package com.music.kotlinqq.view.search

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import com.apkfuns.logutils.LogUtils
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.SearchContentAdapter
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.fragment.BaseLoadingFragment
import com.music.kotlinqq.bean.Album
import com.music.kotlinqq.bean.SearchSong
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.callback.OnAlbumItemClickListener
import com.music.kotlinqq.callback.OnItemClickListener
import com.music.kotlinqq.contract.ISearchContentContract
import com.music.kotlinqq.event.OnlineSongChangeEvent
import com.music.kotlinqq.event.OnlineSongErrorEvent
import com.music.kotlinqq.presenter.SearchContentPresenter
import com.music.kotlinqq.service.PlayerService
import com.music.kotlinqq.util.DownloadUtil
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.fragment_search_content.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayList

/**
 * @author cyl
 * @date 2020/9/25
 */
class SearchContentFragment : BaseLoadingFragment<SearchContentPresenter>(), ISearchContentContract.View {

    companion object {
        private const val TAG = "SearchContentFragment"
        public const val TYPE_KEY = "type"
        public const val SEEK_KEY = "seek"
        public const val IS_ONLINE = "online"
        fun newInstance(seek : String, type : String) : Fragment{
            val fragment = SearchContentFragment()
            val bundle = Bundle()
            bundle.putString(TYPE_KEY, type)
            bundle.putString(SEEK_KEY, seek)
            fragment.arguments = bundle
            return fragment
        }
    }
    private var mSeek : String? = null
    private var mType : String? = null
    private var mOffset = 1 // 用于翻页搜索
    private var mSongList : ArrayList<SearchSong.DataBean.SongBean.ListBean> = ArrayList()
    private var mAlbumList : MutableList<Album.DataBean.AlbumBean.ListBean> = ArrayList()
    private val mPresenters by lazy { SearchContentPresenter() }
    private var mAdapter : SearchContentAdapter? = null
    private val mLRecyclerViewAdapter by lazy { LRecyclerViewAdapter(mAdapter!!) }

    private var mPlayStatusBinder : PlayerService.PlayStatusBinder? = null
    private val connection = object  : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayStatusBinder = service as PlayerService.PlayStatusBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun loadData() {
        Log.i("thisType2", mType)
        if (mType == "song"){
            Log.i("thisType", mType)
            mPresenters.search(mSeek!!, 1)
        } else if (mType == "album"){
            Log.i("thisType1", mType)
            mPresenters.searchAlbum(mSeek!!, 1)
        }
        searchMore()
    }

    override fun reload() {
        super.reload()
        if (mType == "song"){
            mPresenters.search(mSeek!!, 1)
        } else if (mType == "album"){
            mPresenters.searchAlbum(mSeek!!, 1)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_search_content
    }

    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        if (arguments != null){
            mSeek = arguments?.getString(SEEK_KEY)
            mType = arguments?.getString(TYPE_KEY)
            Log.i("SearchTEST", mType)
        }
        val playIntent = Intent(attachActivity, PlayerService::class.java)
        attachActivity.bindService(playIntent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun initOtherView() {
        loadData()
    }

    override fun getPresenter(): SearchContentPresenter {
       return mPresenters
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onOnlineSongChangeEvent(event : OnlineSongChangeEvent){
        if (mAdapter != null) mAdapter?.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onlineSongErrorEvent(event : OnlineSongErrorEvent){
        showToast("该歌曲没有版权")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        attachActivity.unbindService(connection)
    }

    override fun setSongsList(songListBeans: ArrayList<SearchSong.DataBean.SongBean.ListBean>) {
        mSongList.addAll(songListBeans)
        mAdapter = SearchContentAdapter(mSongList, mSeek!!, attachActivity, Constant.TYPE_SONG)
        normalView.layoutManager = layoutManager
        normalView.adapter = mLRecyclerViewAdapter
        SearchContentAdapter.setItemClick{
            tag: Int ->
            val dataBean = mSongList[tag]
            val song = Song()
            song.songId = dataBean.songmid
            song.singer = getSinger(dataBean)
            song.songName = dataBean.songname
            song.imgUrl = Api.ALBUM_PIC + dataBean.albummid + Api.JPG
            song.duration = dataBean.interval.toLong()
            song.isOnline = true
            song.mediaId = dataBean.strMediaMid
            song.isDownload = DownloadUtil.isExistOfDownloadSong(dataBean.songmid)
            mPresenters.getSongUrl(song)
        }
    }

    override fun searchMoreSuccess(songListBeans: ArrayList<SearchSong.DataBean.SongBean.ListBean>) {
        mSongList.addAll(songListBeans)
        mAdapter?.notifyDataSetChanged()
        normalView.refreshComplete(Constant.OFFSET)
    }

    override fun searchMoreError() {
        normalView.setNoMore(true)
    }

    override fun searchMore() {
        normalView.setPullRefreshEnabled(false)
        normalView.setOnLoadMoreListener {
            mOffset += 1
            LogUtils.d(TAG, "onLoadMore:  mOffset=$mOffset")
            if (mType == "song"){
                mPresenters.searchMore(mSeek!!, mOffset)
            } else if (mType == "album"){
                mPresenters.searchAlbumMore(mSeek!!, mOffset)
            }
        }
        normalView.setFooterViewColor(R.color.colorAccent, R.color.musicStyle_low, R.color.transparent)
        // 设置底部文字提示
        normalView.setFooterViewHint("拼命加载中", "已经全部呈现了", "网络不给力，再点击一次试试")
    }

    override fun showSearcherMoreNetworkError() {
        normalView.setOnNetWorkErrorListener {
            mOffset += 1
            mPresenters.searchMore(mSeek!!, mOffset)
        }
    }

    override fun searchAlbumSuccess(albumList: List<Album.DataBean.AlbumBean.ListBean>) {
        Log.i("thisSea", "thisSea")
        LogUtils.i(TAG, albumList)
        mAlbumList.addAll(albumList)
        mAdapter = SearchContentAdapter(mAlbumList, mSeek!!, attachActivity, Constant.TYPE_ALBUM)
        normalView.layoutManager = layoutManager
        normalView.adapter = mLRecyclerViewAdapter
        SearchContentAdapter.setAlbumClick{
            tag: Int ->
            toAlbumContentFragment(mAlbumList[tag])
        }
    }

    override fun searchAlbumMoreSuccess(songListBeans: List<Album.DataBean.AlbumBean.ListBean>) {
        mAlbumList.addAll(songListBeans)
        mAdapter?.notifyDataSetChanged()
        normalView.refreshComplete(Constant.OFFSET)
    }

    override fun searchAlbumError() {
        showToast("获取专辑信息失败")
    }

    override fun getSongUrlSuccess(song: Song, url: String) {
        song.url = url
        FileUtil.saveSong(song)
        mPlayStatusBinder?.playOnline()
    }

    private fun toAlbumContentFragment(album : Album.DataBean.AlbumBean.ListBean){
        val manager = attachActivity.supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.setCustomAnimations(R.anim.fragment_in, R.anim.fragment_out, R.anim.slide_in_right, R.anim.slide_out_right)
        transaction.add(R.id.fragment_container, AlbumContentFragment.newInstance(album.albumMID, album.albumName, album.albumPic, album.singerName, album.publicTime))
        transaction.hide(this)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getSinger(dataBean : SearchSong.DataBean.SongBean.ListBean) : String {
        val singer = StringBuilder(dataBean.singer[0].name)
        for (i in 1 until dataBean.singer.size){
            singer.append("、").append(dataBean.singer[i].name)
        }
        return singer.toString()
    }
}