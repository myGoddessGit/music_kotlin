package com.music.kotlinqq.presenter

import com.apkfuns.logutils.LogUtils
import com.music.kotlinqq.base.observer.BaseObserver
import com.music.kotlinqq.base.presenter.BasePresenter
import com.music.kotlinqq.bean.AlbumSong
import com.music.kotlinqq.contract.IAlbumSongContract
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.music.kotlinqq.view.search.AlbumSongFragment.Companion.ALBUM_SONG
import java.net.UnknownHostException

/**
 * @author cyl
 * @date 2020/9/21
 */
class AlbumSongPresenter : BasePresenter<IAlbumSongContract.View>(), IAlbumSongContract.Presenter {

    companion object {
        const val TAG = "AlbumSongPresenter"
    }
    override fun getAlbumDetail(id: String, type: Int) {
        addRxSubscribe(
            mModel!!.getAlbumSong(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : BaseObserver<AlbumSong>(mView!!){
                    override fun onStart() {
                        mView!!.showLoading()
                    }

                    override fun onNext(albumSong: AlbumSong) {
                        super.onNext(albumSong)
                        mView!!.hideLoading()
                        if (albumSong.code == 0){
                            if (type == ALBUM_SONG){
                                insertAllAlbumSong(albumSong.data.list.toMutableList())
                            } else {
                                mView!!.showAlbumMessage(
                                    albumSong.data.name,
                                    albumSong.data.lan,
                                    albumSong.data.company,
                                    albumSong.data.genre,
                                    albumSong.data.desc)
                            }
                        } else {
                            mView!!.showAlbumSongError()
                        }
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                        LogUtils.d(AlbumSongPresenter.TAG, "onError:  ${e.message}")
                        mView!!.hideLoading()
                        if (e is UnknownHostException && type == ALBUM_SONG){
                            mView!!.showNetError()
                        } else {
                            mView!!.showAlbumSongError()
                        }
                    }
                })
        )
    }

    override fun insertAllAlbumSong(dataBean: MutableList<AlbumSong.DataBean.ListBean>) {
        mModel!!.insertAllAlbumSong(dataBean)
        mView!!.setAlbumSongList(dataBean)
    }

}
