package com.music.kotlinqq.presenter

import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.observer.BaseObserver
import com.music.kotlinqq.base.presenter.BasePresenter
import com.music.kotlinqq.bean.OnlineSongLrc
import com.music.kotlinqq.bean.SearchSong
import com.music.kotlinqq.bean.SingerImg
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.contract.IPlayContract
import com.music.kotlinqq.model.https.RetrofitFactory
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers

/**
 * @author cyl
 * @date 2020/9/22
 */
class PlayPresenter : BasePresenter<IPlayContract.View>(), IPlayContract.Presenter {


    override fun getSingerImg(singer: String, song: String, duration: Long) {
        addRxSubscribe(
            RetrofitFactory.createRequestOfSinger()
                .getSingerImg(singer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext{
                    mView!!.setSingerImg(it.result.artists[0].img1v1Url)
                }
                .doOnError {
                    mView!!.showToast("获取不到歌手图片")
                }
                .observeOn(Schedulers.io())
                .flatMap(Function<SingerImg, ObservableSource<SearchSong>>{
                    RetrofitFactory.createRequest().search(song, 1)
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : BaseObserver<SearchSong>(mView!!){
                    override fun onNext(value: SearchSong) {
                        super.onNext(value)
                        if (value.code == 0){
                            matchLrc(value.data.song.list.toMutableList(), duration)
                        } else {
                            mView!!.getLrcError(null)
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        mView!!.getLrcError(null)
                    }
                })
        )
    }

    override fun getLrc(songId: String, type: Int) {
        mModel!!.getOnlineSongLrc(songId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : BaseObserver<OnlineSongLrc>(mView!!, false, false){
                override fun onNext(value: OnlineSongLrc) {
                    super.onNext(value)
                    if (value.code == 0){
                        val lrc = value.lyric
                        // 如果是本地音乐  就将歌词保存起来
                        if (type == Constant.SONG_LOCAL) mView!!.saveLrc(lrc)
                        mView!!.showLrc(lrc)
                    } else {
                        mView!!.getLrcError(null)
                    }
                }
            })
    }

    override fun getSongId(song: String, duration: Long) {
        addRxSubscribe(
            mModel!!.search(song, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : BaseObserver<SearchSong>(mView!!, true, true){
                    override fun onNext(value: SearchSong) {
                        super.onNext(value)
                        if (value.code == 0){
                            matchSong(value.data.song.list.toMutableList(), duration)
                        } else {
                            mView!!.getLrcError(null)
                        }
                    }
                })
        )
    }

    override fun setPlayMode(mode: Int) {
        mModel!!.setPlayMode(mode)
    }

    override fun getPlayMode() : Int{
        return mModel!!.getPlayMode()
    }


    override fun queryLove(songId: String) {
        mView!!.showLove(mModel!!.queryLove(songId))
    }

    override fun saveToLove(song: Song) {
        if (mModel!!.saveToLove(song)){
            mView!!.saveToLoveSuccess()
        }
    }

    override fun deleteFromLove(songId: String) {
        if (mModel!!.deleteFromLove(songId)){
            mView!!.sendUpdateCollection()
        }
    }

    /**
     * 匹配歌词
     */
    fun matchLrc(listBeans : MutableList<SearchSong.DataBean.SongBean.ListBean>, duration : Long){
        var isFind = false
        for (listBean in listBeans){
            isFind = true
            mView!!.setLocalSongId(listBean.songmid)
        }
        // 如果找不到歌词id 就传输找不到歌曲的消息
        if (!isFind){
            mView!!.getLrcError(Constant.SONG_ID_UNFIND)
        }
    }

    fun matchSong(listBeans : MutableList<SearchSong.DataBean.SongBean.ListBean>, duration: Long){
        var isFind = false
        for (listBean in listBeans){
            if (duration == listBean.interval.toLong()){
                isFind = true
                mView!!.getSongIdSuccess(listBean.songmid)
            }
        }
        // 如果找不到歌词id 就传输找不到歌曲的消息
        if (!isFind){
            mView!!.getLrcError(Constant.SONG_ID_UNFIND)
        }
    }

}