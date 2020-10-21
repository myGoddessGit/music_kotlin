package com.music.kotlinqq.presenter

import android.content.SharedPreferences
import androidx.core.content.edit
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.base.observer.BaseObserver
import com.music.kotlinqq.base.presenter.BasePresenter
import com.music.kotlinqq.bean.Album
import com.music.kotlinqq.bean.SearchSong
import com.music.kotlinqq.bean.Song
import com.music.kotlinqq.bean.SongUrl
import com.music.kotlinqq.contract.ISearchContentContract
import com.music.kotlinqq.model.https.RetrofitFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList

/**
 * @author cyl
 * @date 2020/9/22
 */
class SearchContentPresenter : BasePresenter<ISearchContentContract.View>(), ISearchContentContract.Presenter {

    companion object {
        const val TAG = "SearchContentPresenter"
    }

    override fun search(seek: String, offset: Int) {
        addRxSubscribe(
            mModel!!.search(seek, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : BaseObserver<SearchSong>(mView!!, true, true){
                    override fun onNext(value: SearchSong) {
                        super.onNext(value)
                        if (value.code == 0){
                            mView!!.setSongsList(value.data.song.list as ArrayList<SearchSong.DataBean.SongBean.ListBean>)
                        }
                    }
                })
        )
    }

    override fun searchMore(seek: String, offset: Int) {
        addRxSubscribe(
            mModel!!.search(seek, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : BaseObserver<SearchSong>(mView!!, false, true){
                    override fun onNext(value: SearchSong) {
                        super.onNext(value)
                        if (value.code == 0){
                            val songListBeans = value.data.song.list as ArrayList<SearchSong.DataBean.SongBean.ListBean>
                            if (songListBeans.size == 0){
                                mView!!.searchMoreError()
                            } else {
                                mView!!.searchMoreSuccess(songListBeans)
                            }
                        } else {
                            mView!!.searchMoreError()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        mView!!.showSearcherMoreNetworkError()
                    }
                })
        )
    }

    override fun searchAlbum(seek: String, offset: Int) {
        addRxSubscribe(
            mModel!!.searchAlbum(seek, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : BaseObserver<Album>(mView!!, true, true){
                    override fun onNext(value: Album) {
                        super.onNext(value)
                        if (value.code == 0) {
                            mView!!.searchAlbumSuccess(value.data.album.list.toMutableList())
                        }
                        else {
                            mView!!.searchAlbumError()
                        }
                    }

                })
        )
    }

    override fun searchAlbumMore(seek: String, offset: Int) {
        addRxSubscribe(
            mModel!!.searchAlbum(seek, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : BaseObserver<Album>(mView!!, false, true){
                    override fun onNext(value: Album) {
                        super.onNext(value)
                        if (value.code == 0){
                            mView!!.searchAlbumMoreSuccess(value.data.album.list)
                        } else {
                            mView!!.searchMoreError()
                        }
                    }

                    override fun onError(e: Throwable) {
                        super.onError(e)
                        mView!!.showSearcherMoreNetworkError()
                    }
                })
        )
    }

    override fun getSongUrl(song: Song) {
        addRxSubscribe(
            RetrofitFactory.createRequestOfSongUrl().getSongUrl(Api.SONG_URL_DATA_LEFT + song.songId + Api.SONG_URL_DATA_RIGHT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : BaseObserver<SongUrl>(mView!!, false, false){
                    override fun onNext(value: SongUrl) {
                        super.onNext(value)
                        if (value.code == 0){
                            val sip = value.req_0.data.sip[0]
                            val purl = value.req_0.data.midurlinfo[0].purl
                            if (purl == ""){
                                mView!!.showToast("该歌曲没有版权")
                            } else {
                                mView!!.getSongUrlSuccess(song, sip + purl)
                            }
                        } else {
                            mView!!.showToast("${value.code} : 获取不到歌曲的播放地址")
                        }
                    }
                })
        )
    }
}