package com.music.kotlinqq.presenter

import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.presenter.BasePresenter
import com.music.kotlinqq.bean.LocalSong
import com.music.kotlinqq.contract.ILocalContract
import com.music.kotlinqq.event.SongListNumEvent
import org.greenrobot.eventbus.EventBus

/**
 * @author cyl
 * @date 2020/9/22
 */
class LocalPresenter : BasePresenter<ILocalContract.View>(), ILocalContract.Presenter {

    override fun getLocalMp3Info() {
        val localSongList = mModel!!.getLocalMp3Info()
        if (localSongList.isEmpty()){
            mView!!.showErrorView()
        } else {
            saveSong(localSongList.toMutableList())
        }
    }

    override fun saveSong(localSongs: MutableList<LocalSong>) {
        if (mModel!!.saveSong(localSongs)){
            EventBus.getDefault().post(SongListNumEvent(Constant.LIST_TYPE_LOCAL))
            mView!!.showToast("成功导入本地音乐")
            mView!!.showMusic(localSongs)
        }
    }

}
