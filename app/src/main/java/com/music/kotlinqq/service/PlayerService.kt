package com.music.kotlinqq.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.apkfuns.logutils.LogUtils
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.app.Constant.*
import com.music.kotlinqq.bean.*
import com.music.kotlinqq.event.*
import com.music.kotlinqq.model.https.RetrofitFactory
import com.music.kotlinqq.util.CommonUtil
import com.music.kotlinqq.util.DownloadUtil
import com.music.kotlinqq.util.FileUtil
import com.music.kotlinqq.view.MainActivity
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal
import java.io.IOException
import java.lang.Exception

/**
 * @author cyl
 * @date 2020/9/18
 */
class PlayerService : Service(){

    companion object{
        const val TAG = "PlayerService"
    }
    private val NOTIFICATION_ID = 98
    private val mPlayerStatusBinder = PlayStatusBinder()
    private val mediaPlayer = MediaPlayer()
    private var isPause :Boolean = false
    private var isPlaying : Boolean = false
    private var mLocalSongList: MutableList<LocalSong> = ArrayList() // 本地音乐列表
    private var mSongList: MutableList<OnlineSong> = ArrayList()      // 歌曲列表
    private var mLoveList : MutableList<Love> = ArrayList()            //喜欢列表
    private var mHistoryList : MutableList<HistorySong> = ArrayList()   // 历史列表
    private var mDownloadList : MutableList<DownloadSong> = ArrayList()  // 下载列表
    private var mCurrent : Int = 0
    private var mListType : Int = 0
    private var mPlayMode = PLAY_ORDER // 播放模式 默认为顺序播放

    override fun onCreate() {
        if (FileUtil.getSong() != null){
            mListType = FileUtil.getSong()!!.listType
        }
        when (mListType){
            LIST_TYPE_ONLINE -> mSongList = LitePal.findAll(OnlineSong::class.java)
            LIST_TYPE_LOCAL -> mLocalSongList = LitePal.findAll(LocalSong::class.java)
            LIST_TYPE_LOVE -> mLoveList = LitePal.findAll(Love::class.java)
            LIST_TYPE_HISTORY -> {
                mHistoryList = orderHistoryList(LitePal.findAll(HistorySong::class.java))
                val song = FileUtil.getSong()!!
                song.position = 0
                FileUtil.saveSong(song)
            }
            LIST_TYPE_DOWNLOAD -> orderDownloadList(DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE).toMutableList())
        }
        startForeground(NOTIFICATION_ID, getNotification("快来听一首歌吧!!!!!"))
    }
    override fun onBind(intent: Intent): IBinder {
        mediaPlayer.setOnCompletionListener {
            EventBus.getDefault().post(SongStatusEvent(SONG_PAUSE)) // 暂停广播
            mCurrent = FileUtil.getSong()!!.position
            when (mListType){
                LIST_TYPE_LOCAL -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mLocalSongList.size) // 播放下一首
                    saveLocalSongInfo(mCurrent)
                }
                LIST_TYPE_ONLINE -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mSongList.size) // 播放下一首
                    saveOnlineSongInfo(mCurrent)
                }
                LIST_TYPE_LOVE -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mLoveList.size) // 播放下一首
                    saveLoveInfo(mCurrent)
                }
                LIST_TYPE_HISTORY -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mHistoryList.size) // 播放下一首
                    saveHistoryInfo(mCurrent)
                }
                LIST_TYPE_DOWNLOAD -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mDownloadList.size) // 播放下一首
                    saveDownloadInfo(mCurrent)
                }
            }
            if (mListType != 0){
                mPlayerStatusBinder.play(mListType)
            } else {
                mPlayerStatusBinder.stop()
            }
        }
        /**
         * MediaPlayer切歌进入setOnCompletionListener的问题
         * 因为直接切歌会发生错误，所以增加错误监听器。返回true。就不会回调onCompletion方法了。
         */
        mediaPlayer.setOnErrorListener{
                _, _, _ -> true
        }
        return mPlayerStatusBinder
    }

    open inner class PlayStatusBinder : Binder() {

        fun setPlayMode(mode : Int){
            mPlayMode = mode
        }
        // 获取播放历史
        fun getHistoryList(){
            mHistoryList = orderHistoryList(LitePal.findAll(HistorySong::class.java))
            val song = FileUtil.getSong()!!
            song.position = 0
            FileUtil.saveSong(song)
        }

        fun play(listType : Int){
            try {
                mListType = listType
                when (mListType){
                    LIST_TYPE_ONLINE -> {
                        mSongList = LitePal.findAll(OnlineSong::class.java)
                        EventBus.getDefault().post(SongAlbumEvent())
                    }
                    LIST_TYPE_LOCAL -> {
                        mLocalSongList = LitePal.findAll(LocalSong::class.java)
                        EventBus.getDefault().post(SongLocalEvent()) // 发送本地歌曲改变事件
                    }
                    LIST_TYPE_LOVE -> {
                        mLoveList = orderList(LitePal.findAll(Love::class.java))
                        EventBus.getDefault().post(SongCollectionEvent(true))
                    }
                    LIST_TYPE_HISTORY -> {
                        EventBus.getDefault().post(SongHistoryEvent()) // 发送随机歌曲改变事件
                    }
                    TYPE_DOWNLOADED -> {
                        mDownloadList = orderDownloadList(DownloadUtil.getSongFromFile(Api.STORAGE_SONG_FILE).toMutableList())
                        EventBus.getDefault().post(SongDownloadedEvent()) // 发送下载歌曲改变事件
                    }
                }
                mCurrent = FileUtil.getSong()!!.position
                mediaPlayer.reset() // 重置 恢复初始状态
                when (mListType) {
                    LIST_TYPE_LOCAL -> {
                        mediaPlayer.setDataSource(mLocalSongList[mCurrent].url)
                        startPlay()
                    }
                    LIST_TYPE_ONLINE -> {
                        getSongUrl(mSongList[mCurrent].songId)
                    }
                    LIST_TYPE_LOVE -> {
                        mediaPlayer.setDataSource(mLoveList[mCurrent].url)
                        startPlay()
                    }
                    LIST_TYPE_HISTORY -> {
                        Log.i(TAG + "11", mCurrent.toString())
                        Log.i(TAG, mHistoryList[mCurrent].url)
                        mediaPlayer.setDataSource(mHistoryList[mCurrent].url)
                        startPlay()
                    }
                    LIST_TYPE_DOWNLOAD ->{
//                        Log.i(TAG + "11", mCurrent.toString())
//                        Log.i(TAG, mDownloadList[mCurrent].url)
                        mediaPlayer.setDataSource(mDownloadList[mCurrent].url)
                        startPlay()
                    }
                }
            } catch (e : Exception){
                e.printStackTrace()
            }
        }

        // 播放搜索歌曲
        fun playOnline(){
            try {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(FileUtil.getSong()!!.url)
                mediaPlayer.prepare()
                isPlaying = true
                saveToHistoryTable()
                mediaPlayer.start()
                EventBus.getDefault().post(OnlineSongChangeEvent()) // 发送网络歌曲改变事件
                EventBus.getDefault().post(SongStatusEvent(SONG_CHANGE))
                // 改变通知栏歌曲
                val song = FileUtil.getSong()!!
                getNotificationManager().notify(NOTIFICATION_ID, getNotification(song.songName + " - " + song.singer))
            } catch (e : Exception){
                EventBus.getDefault().post(OnlineSongErrorEvent())
                e.printStackTrace()
            }
        }
        // 暂停
        fun pause(){
            if (mediaPlayer != null && mediaPlayer.isPlaying){
                isPlaying = false
                mediaPlayer.pause()
                isPause = true
                EventBus.getDefault().post(SongStatusEvent(SONG_PAUSE)) //发送暂停的广播
            }
        }

        fun resume(){
            if (isPause){
                mediaPlayer.start()
                isPlaying = true
                isPause = false
                EventBus.getDefault().post(SongStatusEvent(SONG_RESUME))
            }
        }
        // 下一首
        fun next(){
            EventBus.getDefault().post(SongStatusEvent(SONG_RESUME))
            mCurrent = FileUtil.getSong()!!.position
            when (mListType) {
                LIST_TYPE_LOCAL -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mLocalSongList.size) // 根据播放模式播放下一首
                    saveLocalSongInfo(mCurrent)
                }
                LIST_TYPE_ONLINE -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mSongList.size)
                    saveOnlineSongInfo(mCurrent)
                }
                LIST_TYPE_LOVE -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mLoveList.size)
                    saveLoveInfo(mCurrent)
                }
                LIST_TYPE_HISTORY -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mHistoryList.size)
                    saveHistoryInfo(mCurrent)
                }
                LIST_TYPE_DOWNLOAD -> {
                    mCurrent = getNextCurrent(mCurrent, mPlayMode, mDownloadList.size)
                    saveDownloadInfo(mCurrent)
                }
            }
            if (mListType != 0) mPlayerStatusBinder.play(mListType)
        }

        // 上一首
        fun last(){
            EventBus.getDefault().post(SongStatusEvent(SONG_RESUME)) // 发送暂停广播
            mCurrent = FileUtil.getSong()!!.position
            when (mListType){
                LIST_TYPE_LOCAL -> {
                    mCurrent = getLastCurrent(mCurrent, mPlayMode, mLocalSongList.size)
                    saveLocalSongInfo(mCurrent)
                }
                LIST_TYPE_ONLINE -> {
                    mCurrent = getLastCurrent(mCurrent, mPlayMode, mSongList.size)
                    saveOnlineSongInfo(mCurrent)
                }
                LIST_TYPE_LOVE -> {
                    mCurrent = getLastCurrent(mCurrent, mPlayMode, mLoveList.size)
                    saveLoveInfo(mCurrent)
                }
                LIST_TYPE_HISTORY -> {
                    mCurrent = getLastCurrent(mCurrent, mPlayMode, mHistoryList.size)
                    saveHistoryInfo(mCurrent)
                }
                LIST_TYPE_DOWNLOAD -> {
                    mCurrent = getLastCurrent(mCurrent, mPlayMode, mDownloadList.size)
                    saveDownloadInfo(mCurrent)
                }
            }
            if (mListType != 0) mPlayerStatusBinder.play(mListType)
        }

        fun stop(){
            isPlaying = false
            mediaPlayer.stop()
            try {
                mediaPlayer.prepare() // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (e : Exception){
                e.printStackTrace()
            }
        }

        fun isPlaying() : Boolean {
            return isPlaying
        }

        fun getMediaPlayer() : MediaPlayer {
            return mediaPlayer
        }

        fun getCurrentTime() : Long {
            Log.i("PlayCurrent", mediaPlayer.currentPosition.toString())
            return mediaPlayer.currentPosition.toLong() / 1000
        }

    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        stopForeground(true)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    /**
     * 保存本地歌曲列表信息
     */
    private fun saveLocalSongInfo(current: Int){
        mLocalSongList = LitePal.findAll(LocalSong::class.java)
        val song = Song()
        val localSong = mLocalSongList[current]
        song.position = current
        song.songName = localSong.name
        song.singer = localSong.singer
        song.duration = localSong.duration
        song.url = localSong.url
        song.imgUrl = localSong.pic
        song.songId = localSong.songId
        song.qqId = localSong.qqId
        song.isOnline = false
        song.listType = LIST_TYPE_LOCAL
        FileUtil.saveSong(song)
    }
    /**
     * 保存网络专辑列表的信息
     */
    private fun saveOnlineSongInfo(current: Int){
        mSongList = LitePal.findAll(OnlineSong::class.java)
        val song = Song()
        val onlineSong = mSongList[current]
        song.position = current
        song.songId = onlineSong.songId
        song.songName = onlineSong.name
        song.singer = onlineSong.singer
        song.duration = onlineSong.duration
        song.url = onlineSong.url
        song.imgUrl = onlineSong.pic
        song.isOnline = true
        song.listType = LIST_TYPE_ONLINE
        song.mediaId = onlineSong.mediaId
        FileUtil.saveSong(song)
    }
    /**
     * 保存我的收藏的列表的信息
     */
    private fun saveLoveInfo(current: Int){
        mLoveList = orderList(LitePal.findAll(Love::class.java))
        val love = mLoveList[current]
        val song = Song()
        song.position = current
        song.songId = love.songId
        song.qqId = love.qqId
        song.songName = love.name
        song.singer = love.singer
        song.url = love.url
        song.imgUrl = love.pic
        song.listType = LIST_TYPE_LOVE
        song.isOnline = love.isOnline
        song.duration = love.duration
        song.mediaId = love.mediaId
        song.isDownload = love.isDownload
        FileUtil.saveSong(song)
    }

    /**
     * 保存下载列表的信息
     */
    private fun saveDownloadInfo(current: Int){
        val downloadSong = mDownloadList[current]
        val song = Song()
        song.position = current
        song.songId = downloadSong.songId
        song.songName = downloadSong.name
        song.singer = downloadSong.singer
        song.url = downloadSong.url
        song.imgUrl = downloadSong.pic
        song.listType = LIST_TYPE_DOWNLOAD
        song.isOnline = false
        song.duration = downloadSong.duration
        song.mediaId = downloadSong.mediaId
        song.isDownload = true
        FileUtil.saveSong(song)
    }
    /**
     * 保存我的收藏列表的信息
     */
    private fun saveHistoryInfo(current: Int){
        val historySong = mHistoryList[current]
        val song = Song()
        song.position = current
        song.songId = historySong.songId
        song.qqId = historySong.qqId
        song.songName = historySong.name
        song.singer = historySong.singer
        song.url = historySong.url
        song.imgUrl = historySong.pic
        song.listType = LIST_TYPE_HISTORY
        song.isOnline = historySong.isOnline
        song.duration = historySong.duration
        song.mediaId = historySong.mediaId
        song.isDownload = historySong.isDownload
        FileUtil.saveSong(song)
    }

    /**
     * 将歌曲保存到最近的播放的数据库中
     */
    private fun saveToHistoryTable(){
        val song = FileUtil.getSong()!!
        LitePal.where("songId=?", song.songId).findAsync(HistorySong::class.java)
            .listen {
                if (it.size == 1){
                    LitePal.deleteAll(HistorySong::class.java, "songId=?", song.songId)
                }
                val history = HistorySong()
                history.songId = song.songId
                history.qqId = song.qqId
                history.name = song.songName
                history.singer = song.singer
                Log.i(TAG + "22", song.url)
                history.url = song.url
                history.pic = song.imgUrl
                history.isOnline = song.isOnline
                history.duration = song.duration
                history.mediaId = song.mediaId
                history.isDownload = song.isDownload
                history.saveAsync().listen { success ->
                    if (success){
                        EventBus.getDefault().post(SongListNumEvent(LIST_TYPE_HISTORY))
                        if (LitePal.findAll(HistorySong::class.java).size > HISTORY_MAX_SIZE){
                            LitePal.delete(HistorySong::class.java, LitePal.findFirst(HistorySong::class.java).id.toLong())
                        }
                    }
                }
            }
    }
    // 对数据库进行倒叙排序
    private fun orderList(tempList: MutableList<Love>) : MutableList<Love> {
        val loveList = ArrayList<Love>()
        loveList.clear()
        for (i in tempList.indices.reversed()){
            loveList.add(tempList[i])
        }
        return loveList
    }

    private fun orderDownloadList(tempList : MutableList<DownloadSong>) : MutableList<DownloadSong>{
        val downloadSongList = ArrayList<DownloadSong>()
        downloadSongList.clear()
        for (i in tempList.indices.reversed()){
            downloadSongList.add(tempList[i])
        }
        return downloadSongList
    }

    fun orderHistoryList(tempList: MutableList<HistorySong>) : MutableList<HistorySong>{
        val historySongList = ArrayList<HistorySong>()
        historySongList.clear()
        for (i in tempList.indices.reversed()){
            historySongList.add(tempList[i])
        }
        return historySongList
    }

    /**
     * 网络请求获取播放地址
     */
    fun getSongUrl(songId : String){
        RetrofitFactory.createRequestOfSongUrl().getSongUrl(Api.SONG_URL_DATA_LEFT + songId + Api.SONG_URL_DATA_RIGHT)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : Observer<SongUrl>{
                override fun onComplete() {

                }

                override fun onSubscribe(d: Disposable?) {

                }

                override fun onNext(songUrl : SongUrl) {
                    if (songUrl.code == 0){
                        val sip = songUrl.req_0.data.sip[0]
                        val purl = songUrl.req_0.data.midurlinfo[0].purl
                        if (purl == ""){
                            CommonUtil.showToast(this@PlayerService, "该歌曲暂时没有版权，试试搜索其它歌曲吧")
                            return
                        }
                        val song = FileUtil.getSong()!!
                        song.url = sip + purl
                        FileUtil.saveSong(song)
                        try {
                            mediaPlayer.setDataSource(sip + purl)
                            Log.d(TAG, "onNext:yearning=${sip + purl}")
                            startPlay()
                        } catch (e : IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Log.d(TAG,"onNext: ${songUrl.code} + 获取不到歌曲的播放地址")
                    }
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, e.message)
                }

            })
    }

    /**
     * 开始播放
     */
    @Throws(IOException::class)
    private fun startPlay(){
        mediaPlayer.prepare() // 缓冲
        isPlaying = true
        mediaPlayer.start()
        saveToHistoryTable()
        EventBus.getDefault().post(SongStatusEvent(SONG_CHANGE)) // 发送歌曲改变事件
        EventBus.getDefault().post(OnlineSongChangeEvent()) // 发送网络歌曲改变事件
        val song = FileUtil.getSong()!!
        getNotificationManager().notify(NOTIFICATION_ID, getNotification(song.songName + " - " + song.singer))
    }

    /**
     * 根据播放模式得到下一首歌曲的位置
     */
    private fun getNextCurrent(current: Int,playMode: Int,len: Int) : Int{
        return when(playMode){
            PLAY_ORDER  -> (current + 1) % len
            PLAY_RANDOM -> (current + (Math.random() * len)).toInt() % len
            else -> current
        }
    }

    /**
     * 根据播放模式得到上一首歌曲的位置
     */
    private fun getLastCurrent(current : Int, playMode : Int, len : Int) :Int{
        return when (playMode) {
            PLAY_ORDER -> if (current - 1 == -1) len - 1 else current - 1
            PLAY_RANDOM -> (current + (Math.random() * len).toInt()) % len
            else -> current
        }
    }

    private fun getNotificationManager() : NotificationManager {
        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * 设置通知栏标题
     */
   private fun getNotification(title : String) : Notification{
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val id = "play"
            val name = "播放歌曲"
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            getNotificationManager().createNotificationChannel(mChannel)
            val builder = Notification.Builder(this, id)
            builder.setSmallIcon(R.mipmap.icon)
            builder.setContentIntent(pi)
            builder.setContentTitle(title)
            return builder.build()
        } else {
            val builder = NotificationCompat.Builder(this,"play")
            builder.setSmallIcon(R.mipmap.icon)
            builder.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.icon))
            builder.setContentIntent(pi)
            builder.setContentTitle(title)
            return builder.build()
        }
    }

}