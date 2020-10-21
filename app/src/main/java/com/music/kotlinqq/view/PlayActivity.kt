package com.music.kotlinqq.view

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.transition.Slide
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.apkfuns.logutils.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.activity.BaseMvpActivity
import com.music.kotlinqq.bean.DownloadInfo
import com.music.kotlinqq.bean.DownloadSong
import com.music.kotlinqq.bean.LocalSong
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.contract.IPlayContract
import com.music.kotlinqq.event.DownloadEvent
import com.music.kotlinqq.event.SongCollectionEvent
import com.music.kotlinqq.event.SongStatusEvent
import com.music.kotlinqq.presenter.PlayPresenter
import com.music.kotlinqq.service.DownloadService
import com.music.kotlinqq.service.PlayerService
import com.music.kotlinqq.util.*
import com.music.kotlinqq.widget.DiscViewS
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.layout_disc_view.*
import kotlinx.android.synthetic.main.play_mode.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal

/**
 * @author cyl
 * @date 2020/9/26
 */
class PlayActivity : BaseMvpActivity<PlayPresenter>(), IPlayContract.View {

    companion object {
        private const val TAG = "PlayActivity"
    }
    private val mPresenters by lazy { PlayPresenter() }
    private var isOnline = false // 判断是否为网络歌曲
    private var mListType = 0 // 列表类型
    private var mPlayStatus = 0
    private var mPlayMode = 0 // 播放模式
    private var isChange = false // 拖动滚动条
    private var isSeek = false // 标记是否在暂停的时候拖动进度条
    private var flag = false // 用做暂停的标记
    private var time = 0 // 记录暂停时间
    private var mSong = Song()
    private var mMediaPlayer = MediaPlayer()
    private var mDiscViewS : DiscViewS? = null

    private var isLove = false // 是否存在我喜欢的列表
    private var mImgBmp : Bitmap? = null
    // 服务
    private var mPlayStatusBinder : PlayerService.PlayStatusBinder? = null
    private var mDownloadBinder : DownloadService.DownloadBinder? = null
    // 播放
    private val mPlayConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mPlayStatusBinder = service as PlayerService.PlayStatusBinder
            // 播放模式
            mPlayMode = mPresenters.getPlayMode() // get播放模式
            mPlayStatusBinder!!.setPlayMode(mPlayMode)
            isOnline = FileUtil.getSong()!!.isOnline
            if (isOnline){
                btn_get_img_lrc.visibility = View.GONE
                setSingerImg(FileUtil.getSong()!!.imgUrl)
                if (mPlayStatus == Constant.SONG_PLAY){
                    mDiscViewS!!.play()
                    btn_player.isSelected = true
                    startUpdateSeekBarProgress()
                }
            } else {
                setLocalImg(mSong.singer)
                seek.secondaryProgress = mSong.duration.toInt()
            }
            tv_duration_time.text = MediaUtil.formatTime(mSong.duration)
            // 缓存进度条
            mPlayStatusBinder!!.getMediaPlayer().setOnBufferingUpdateListener { _, percent ->
                seek.secondaryProgress = percent * seek.progress
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }
    // 下载
    private val mDownloadConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mDownloadBinder = service as DownloadService.DownloadBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    private val mMusicHandler = @SuppressLint("HandlerLeak")
    object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (!isChange){
                seek.progress = mPlayStatusBinder!!.getCurrentTime().toInt()
                tv_current_time.text = MediaUtil.formatTime(seek.progress.toLong())
                startUpdateSeekBarProgress()
            }
        }
    }

    override fun initView() {
        super.initView()
        EventBus.getDefault().register(this)
        CommonUtil.hideStatusBar(this, true)
        window.enterTransition = Slide()
        window.exitTransition = Slide()
        mDiscViewS = findViewById(R.id.disc_view)
        // 判断播放状态
        mPlayStatus = intent.getIntExtra(Constant.PLAYER_STATUS, 2)
        // 绑定服务
        bindService(Intent(this, PlayerService::class.java), mPlayConnection, Context.BIND_AUTO_CREATE)
        bindService(Intent(this, DownloadService::class.java), mDownloadConnection, Context.BIND_AUTO_CREATE)
        // 界面填充
        mSong = FileUtil.getSong()!!
        mListType = mSong.listType
        tv_singer.text = mSong.singer
        tv_song.text = mSong.songName
        tv_current_time.text = MediaUtil.formatTime(mSong.currentTime)
        seek.max = mSong.duration.toInt()
        seek.progress = mSong.currentTime.toInt()
        downloadIv.visibility = if(mSong.isOnline) View.VISIBLE else View.GONE // 下载按钮是否隐藏
        downloadIv.setImageDrawable(if (mSong.isDownload) getDrawable(R.drawable.downloaded) else getDrawable(R.drawable.download_song))
        mPlayMode = mPresenters.getPlayMode() // 得到播放模式
        when (mPlayMode){
            Constant.PLAY_ORDER -> {
                btn_order.background = getDrawable(R.drawable.play_mode_order)
            }
            Constant.PLAY_RANDOM -> {
                btn_order.background = getDrawable(R.drawable.play_mode_random)
            }
            else -> {
                btn_order.background = getDrawable(R.drawable.play_mode_single)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onDownloadSuccessEvent(event : DownloadEvent){
        if (event.getDownloadStatus() == Constant.TYPE_DOWNLOAD_SUCCESS){
            downloadIv.setImageDrawable(
                if (LitePal.where("songId=?", mSong.songId).find(DownloadSong::class.java).size != 0)
                    getDrawable(R.drawable.downloaded)
                else getDrawable(R.drawable.download_song)
            )
        }
    }

    override fun getPresenter(): PlayPresenter {
       return mPresenters
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_play
    }

    override fun initData() {
        mPresenters.queryLove(mSong.songId) // 查找歌曲是否为我喜欢的歌曲
        if (mPlayStatus == Constant.SONG_PLAY){
            mDiscViewS!!.play()
            btn_player.isSelected = true
            startUpdateSeekBarProgress()
        }
    }

    private fun try2UpdateMusicPicBackground(bitmap : Bitmap){
        Thread {
            val drawable = getForegroundDrawable(bitmap)
            runOnUiThread {
                relative_root.foreground = drawable
                relative_root.beginAnimation()
            }
        }.start()
    }

    private fun getForegroundDrawable(bitmap : Bitmap) : Drawable{
        val widthHeightSize = (DisplayUtil.getScreenWidth(this) * 1.0f / DisplayUtil.getScreenHeight(this) * 1.0f)
        val cropBitmapWidth = (widthHeightSize * bitmap.height).toInt()
        val cropBitmapWidthX = ((bitmap.width - cropBitmapWidth) / 2.0).toInt()
        val cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth, bitmap.height)
        val scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.width / 50, bitmap.height / 50, false)
        val blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 8, true)
        val foregroundDrawable = BitmapDrawable(blurBitmap)
        foregroundDrawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
        return foregroundDrawable
    }

    override fun onClick() {
        iv_back.setOnClickListener { finish() } // 返回按钮
        // 获取本地音乐的图片和歌词
        btn_get_img_lrc.setOnClickListener { getSingerAndLrc() }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isChange = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (mPlayStatusBinder!!.isPlaying()){
                   mMediaPlayer = mPlayStatusBinder!!.getMediaPlayer()
                   mMediaPlayer.seekTo(seek.progress * 1000)
                   startUpdateSeekBarProgress()
                } else {
                   time = seek.progress
                   isSeek = true
                }
                isChange = false
                tv_current_time.text = MediaUtil.formatTime(seek.progress.toLong())
            }
        })
        btn_order.setOnClickListener { changePlayMode() }
        // 播放暂停的实现
        btn_player.setOnClickListener {
            mMediaPlayer = mPlayStatusBinder!!.getMediaPlayer()
            when {
                mPlayStatusBinder!!.isPlaying() -> {
                    mPlayStatusBinder!!.pause()
                    stopUpdateSeekBarProgress()
                    flag = true
                    btn_player.isSelected = false
                    mDiscViewS!!.pause()
                }
                flag -> {
                    mPlayStatusBinder!!.resume()
                    flag = true
                    if (isSeek){
                        LogUtils.d(TAG, "onClick$time")
                        mMediaPlayer.seekTo(time * 1000)
                        isSeek = false
                    }
                    mDiscViewS!!.play()
                    btn_player.isSelected = true
                    startUpdateSeekBarProgress()
                }
                else -> {
                    if (isOnline){
                        mPlayStatusBinder!!.playOnline()
                    } else {
                        mPlayStatusBinder!!.play(mListType)
                    }
                    mMediaPlayer.seekTo((mSong.currentTime * 1000).toInt())
                    mDiscViewS!!.play()
                    btn_player.isSelected = true
                    startUpdateSeekBarProgress()
                }
            }
        }
        next.setOnClickListener {
            mPlayStatusBinder!!.next()
            btn_player.isSelected = mPlayStatusBinder!!.isPlaying()
            mDiscViewS!!.next()
        }
        btn_last.setOnClickListener {
            mPlayStatusBinder!!.last()
            btn_player.isSelected = true
            mDiscViewS!!.last()
        }

        btn_love.setOnClickListener {
            showLoveAnim()
            if (isLove){
                btn_love.isSelected = false
                mPresenters.deleteFromLove(FileUtil.getSong()!!.songId)
            } else {
                btn_love.isSelected = true
                mPresenters.saveToLove(FileUtil.getSong()!!)
            }
            isLove = !isLove
        }
        // 唱碟点击效果
        mDiscViewS!!.setOnClickListener{
            if (!isLove){
                val lrc = FileUtil.getLrcFromNative(mSong.songName)
                if (null == lrc){
                    when (val qqId = mSong.qqId) {
                        Constant.SONG_ID_UNFIND -> // 匹配不到歌词
                            getLrcError(null)
                        null -> // 歌词的id还未匹配
                            mPresenters.getSongId(mSong.songName, mSong.duration)
                        else -> // 歌词还未匹配
                            mPresenters.getLrc(qqId, Constant.SONG_LOCAL)
                    }
                } else {
                    showLrc(lrc)
                }
            } else {
                mPresenters.getLrc(mSong.songId, Constant.SONG_ONLINE)
            }
        }
        // 歌词点击效果
        lrcView.setOnClickListener {
            lrcView.visibility = View.GONE
            mDiscViewS!!.visibility = View.VISIBLE
        }
        // 歌曲下载
        downloadIv.setOnClickListener {
            if (mSong.isDownload){
                showToast("下载")
            } else {
                mDownloadBinder!!.startDownload(getDownloadInfoFromSong(mSong))
            }
        }
    }

    override fun getSingerName(): String {
        val song = FileUtil.getSong()!!
        return if (song.singer.contains("/")){
            val s = song.singer.split("/")
            s[0].trim()
        } else {
            song.singer.trim()
        }
    }

    private fun getSongName() : String {
        return FileUtil.getSong()!!.songName.trim()
    }

    override fun getSingerAndLrc() {
        btn_get_img_lrc.text = "正在获取..."
        mPresenters.getSingerImg(getSingerName(), getSongName(), mSong.duration)
    }

    override fun setSingerImg(imgUrl: String) {
        Glide.with(this)
            .load(imgUrl)
            .apply(RequestOptions.placeholderOf(R.drawable.welcome))
            .apply(RequestOptions.errorOf(R.drawable.welcome))
            .into(object : SimpleTarget<Drawable>(){
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    mImgBmp = (resource as BitmapDrawable).bitmap
                    // 如果是本地音乐
                    if (!isOnline){
                        // 保存图片到本地
                        FileUtil.saveImgToNative(mImgBmp!!, getSingerName())
                        val localSong = LocalSong()
                        localSong.pic = Api.STORAGE_IMG_FILE + FileUtil.getSong()!!.singer + ".jpg"
                        localSong.updateAll("songId=?", FileUtil.getSong()!!.songId)
                    }
                    try2UpdateMusicPicBackground(mImgBmp!!)
                    setDiscImg(mImgBmp!!)
                    btn_get_img_lrc.visibility = View.GONE
                }
            })
    }


    override fun showLove(love: Boolean) {
        isLove = love
        runOnUiThread {
            btn_love.isSelected = love
        }
    }

    override fun showLoveAnim() {
        val animatorSet = AnimatorInflater.loadAnimator(this, R.animator.favorites_anim) as AnimatorSet
        animatorSet.setTarget(btn_love)
        animatorSet.start()
    }

    override fun saveToLoveSuccess() {
        EventBus.getDefault().post(SongCollectionEvent(true))
        showToast("收藏成功")
    }

    override fun sendUpdateCollection() {
       EventBus.getDefault().post(SongCollectionEvent(false))
    }

    // 设置唱碟中歌手头像
    private fun setDiscImg(bitmap : Bitmap){
        iv_disc_background.setImageDrawable(mDiscViewS!!.getDiscDrawable(bitmap))
        val marginTop = (DisplayUtil.SCALE_DISC_MARGIN_TOP * CommonUtil.getScreenHeight(this)).toInt()
        val layoutParams = iv_disc_background.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(0, marginTop, 0, 0)
        iv_disc_background.layoutParams = layoutParams
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onSongChangeEvent(event : SongStatusEvent){
        if (event.songStatus == Constant.SONG_CHANGE){
            mDiscViewS?.visibility = View.VISIBLE
            lrcView.visibility = View.GONE
            mSong = FileUtil.getSong()!!
            tv_song.text = mSong.songName
            tv_singer.text = mSong.singer
            tv_duration_time.text = MediaUtil.formatTime(mSong.duration)
            btn_player.isSelected = true
            seek.max = mSong.duration.toInt()
            startUpdateSeekBarProgress()
            // 缓存进度条
            mPlayStatusBinder!!.getMediaPlayer().setOnBufferingUpdateListener{
                _, percent -> seek.secondaryProgress = percent * seek.progress
            }
            mPresenters.queryLove(mSong.songId)
            if (mSong.isOnline){
                setSingerImg(mSong.imgUrl)
            } else {
                setLocalImg(mSong.singer) // 显示图片
            }
        }
    }

    private fun startUpdateSeekBarProgress(){
        stopUpdateSeekBarProgress()
        mMusicHandler.sendEmptyMessageDelayed(0, 1000)
    }

    private fun stopUpdateSeekBarProgress(){
        mMusicHandler.removeMessages(0)
    }

    private fun setLocalImg(singer : String){
        val imgUrl = Api.STORAGE_IMG_FILE + MediaUtil.formatSinger(singer) + ".jpg"
        Glide.with(this)
            .load(imgUrl)
            .listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    btn_get_img_lrc.visibility = View.VISIBLE
                    btn_get_img_lrc.text = "获取封面歌词"
                    setDiscImg(BitmapFactory.decodeResource(resources, R.drawable.default_disc))
                    relative_root.setBackgroundResource(R.drawable.background)
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

            })
            .apply(RequestOptions.placeholderOf(R.drawable.background))
            .apply(RequestOptions.errorOf(R.drawable.background))
            .into(object : SimpleTarget<Drawable>(){
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    btn_get_img_lrc.visibility = View.GONE
                    mImgBmp = (resource as BitmapDrawable).bitmap
                    try2UpdateMusicPicBackground(mImgBmp!!)
                    setDiscImg(mImgBmp!!)
                }
            })
    }

    override fun showLrc(lrc: String) {
        mDiscViewS!!.visibility = View.GONE
        lrcView.visibility = View.VISIBLE
        LogUtils.d(TAG, "showLrc${mPlayStatusBinder!!.getMediaPlayer().currentPosition}")
        lrcView.setLrc(lrc).setPlayer(mPlayStatusBinder!!.getMediaPlayer()).draw()
    }

    override fun getLrcError(content: String?) {
        showToast(getString(R.string.get_lrc_fail))
        mSong.qqId = content
        FileUtil.saveSong(mSong)
    }

    override fun setLocalSongId(songId: String) {
        mSong.qqId = songId
        FileUtil.saveSong(mSong) // save
    }

    override fun getSongIdSuccess(songId: String) {
        setLocalSongId(songId) // 保存音乐信息
        mPresenters.getLrc(songId, Constant.SONG_LOCAL) // 获取歌词
    }

    override fun saveLrc(lrc: String) {
       FileUtil.saveLrcToNative(lrc, mSong.songName)
    }

    private fun changePlayMode(){
        val playModeView = LayoutInflater.from(this).inflate(R.layout.play_mode, null)
        val orderLayout = playModeView.orderLayout
        val randomLayout = playModeView.randomLayout
        val singleLayout = playModeView.singleLayout
        val orderTv = playModeView.orderTv
        val randomTv = playModeView.randomTv
        val singleTv = playModeView.singleTv
        // 显示弹窗
        val popupWindow = PopupWindow(playModeView, ScreenUtil.dip2px(this, 130F), ScreenUtil.dip2px(this, 150F))
        // 设置背景色
        popupWindow.setBackgroundDrawable(getDrawable(R.color.transparent))
        // 设置焦点
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.update()
        popupWindow.showAsDropDown(btn_order, 0, -50)

        // 显示播放模式
        when (mPresenters.getPlayMode()){
            Constant.PLAY_ORDER -> {
                orderTv.isSelected = true
                randomTv.isSelected = false
                singleTv.isSelected = false
            }
            Constant.PLAY_RANDOM ->{
                orderTv.isSelected = false
                randomTv.isSelected = true
                singleTv.isSelected = false
            }
            else ->{
                orderTv.isSelected = false
                randomTv.isSelected= false
                singleTv.isSelected = true
            }
        }
        // 顺序播放
        orderLayout.setOnClickListener {
            mPlayStatusBinder?.setPlayMode(Constant.PLAY_ORDER) // 通知服务
            mPresenters.setPlayMode(Constant.PLAY_ORDER)
            popupWindow.dismiss()
            btn_order.background = getDrawable(R.drawable.play_mode_order)
        }
        // 随机播放
        randomLayout.setOnClickListener {
            mPlayStatusBinder?.setPlayMode(Constant.PLAY_RANDOM) // 通知服务
            mPresenters.setPlayMode(Constant.PLAY_RANDOM)
            popupWindow.dismiss()
            btn_order.background = getDrawable(R.drawable.play_mode_random)
        }
        // 单曲循环
        singleLayout.setOnClickListener {
            mPlayStatusBinder?.setPlayMode(Constant.PLAY_SINGLE) // 通知服务
            mPresenters.setPlayMode(Constant.PLAY_SINGLE)
            popupWindow.dismiss()
            btn_order.background = getDrawable(R.drawable.play_mode_single)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mPlayConnection)
        unbindService(mDownloadConnection)
        EventBus.getDefault().unregister(this)
        stopUpdateSeekBarProgress()
        mMusicHandler.removeCallbacksAndMessages(null)
    }

    private fun getDownloadInfoFromSong(song : Song) : DownloadInfo{
        val downloadInfo = DownloadInfo()
        downloadInfo.singer = song.singer
        downloadInfo.progress = 0
        downloadInfo.songId = song.songId
        downloadInfo.url = song.url
        downloadInfo.songName = song.songName
        downloadInfo.duration = song.duration
        return downloadInfo
    }
}